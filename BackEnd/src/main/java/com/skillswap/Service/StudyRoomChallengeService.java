package com.skillSwap.Service;

import com.skillSwap.Dto.studyroom.StudyRoomDtos.*;
import com.skillSwap.Entity.*;
import com.skillSwap.Entity.StudyRoomEnums.*;
import com.skillSwap.Exception.ResourceNotFoundException;
import com.skillSwap.Exception.StudyRoomException;
import com.skillSwap.Repository.StudyRoomChallengeRepository;
import com.skillSwap.Repository.StudyRoomSubmissionRepository;
import com.skillSwap.Repository.UserRepository;
import com.skillSwap.websocket.StudyRoomPresenceRegistry;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Challenge flow: host creates -> members submit once -> host approves/rejects -> credits are paid ONCE on approval.
 * Duplicate rewards are blocked three ways: unique (challenge,user) row, PENDING->APPROVED update that only one caller can win,
 * and the daily credit cap inside StudyRewardService.
 */
@Service
public class StudyRoomChallengeService {

	private static final int MAX_OPEN_CHALLENGES = 20;

	private final StudyRoomChallengeRepository challenges;
	private final StudyRoomSubmissionRepository submissions;
	private final StudyRoomService roomService;
	private final StudyRewardService rewards;
	private final UserRepository users;
	private final StudyRoomPresenceRegistry registry;

	public StudyRoomChallengeService(
		StudyRoomChallengeRepository challenges,
		StudyRoomSubmissionRepository submissions,
		StudyRoomService roomService,
		StudyRewardService rewards,
		UserRepository users,
		StudyRoomPresenceRegistry registry
	) {
		this.challenges = challenges;
		this.submissions = submissions;
		this.roomService = roomService;
		this.rewards = rewards;
		this.users = users;
		this.registry = registry;
	}

	@Transactional
	public ChallengeView create(Integer hostId, Integer roomId, ChallengeRequest req) {
		StudyRoom room = roomService.requireHost(roomId, hostId);
		if (challenges.countByRoom_IdAndStatus(roomId, ChallengeStatus.OPEN) >= MAX_OPEN_CHALLENGES) {
			throw new StudyRoomException(
				HttpStatus.CONFLICT,
				"Close an old challenge first (max " + MAX_OPEN_CHALLENGES + " open)"
			);
		}
		if (req.deadline() != null && req.deadline().isBefore(Instant.now())) {
			throw new IllegalArgumentException("Deadline must be in the future");
		}
		StudyRoomChallenge c = new StudyRoomChallenge();
		c.setRoom(room);
		c.setTitle(req.title().strip());
		c.setDescription(req.description() == null || req.description().isBlank() ? null : req.description().strip());
		c.setCompletionCriteria(req.completionCriteria().strip());
		c.setDeadline(req.deadline());
		c.setRewardCredits(req.rewardCredits() == null ? 0 : req.rewardCredits());
		c.setEvidenceRequired(Boolean.TRUE.equals(req.evidenceRequired()));
		c.setCreatedBy(users.getReferenceById(hostId));
		c = challenges.save(c);
		registry.broadcastAfterCommit(roomId, "challenge", new ChallengeEvent("created", c.getId()));
		return toView(c, hostId, true);
	}

	@Transactional(readOnly = true)
	public List<ChallengeView> list(Integer userId, Integer roomId) {
		StudyRoom room = roomService.requireRoom(roomId);
		roomService.requireActiveMember(roomId, userId);
		boolean host = room.getHost().getId().equals(userId);
		return challenges
			.findByRoom_IdOrderByCreatedAtDesc(roomId, PageRequest.of(0, 50))
			.stream()
			.map(c -> toView(c, userId, host))
			.toList();
	}

	@Transactional
	public void close(Integer hostId, Integer roomId, Integer challengeId) {
		roomService.requireHost(roomId, hostId);
		StudyRoomChallenge c = find(roomId, challengeId);
		c.setStatus(ChallengeStatus.CLOSED);
		registry.broadcastAfterCommit(roomId, "challenge", new ChallengeEvent("closed", challengeId));
	}

	@Transactional
	public SubmissionView submit(Integer userId, Integer roomId, Integer challengeId, String evidence) {
		StudyRoom room = roomService.requireRoom(roomId);
		roomService.requireActiveMember(roomId, userId);
		if (room.getHost().getId().equals(userId)) {
			throw new AccessDeniedException("Hosts cannot submit to their own challenges");
		}
		StudyRoomChallenge c = find(roomId, challengeId);
		if (c.getStatus() != ChallengeStatus.OPEN) throw new StudyRoomException(
			HttpStatus.CONFLICT,
			"This challenge is closed"
		);
		if (c.getDeadline() != null && c.getDeadline().isBefore(Instant.now())) {
			throw new StudyRoomException(HttpStatus.CONFLICT, "The deadline has passed");
		}
		String text = evidence == null ? "" : evidence.strip();
		if (c.isEvidenceRequired() && text.isEmpty()) throw new IllegalArgumentException(
			"This challenge needs a short answer or a link as evidence"
		);

		StudyRoomSubmission s = submissions.findByChallenge_IdAndUser_Id(challengeId, userId).orElse(null);
		if (s == null) {
			s = new StudyRoomSubmission();
			s.setChallenge(c);
			s.setUser(users.getReferenceById(userId));
		} else if (s.getStatus() != SubmissionStatus.REJECTED) {
			throw new StudyRoomException(HttpStatus.CONFLICT, "You already submitted this challenge");
		}
		s.setEvidence(text.isEmpty() ? null : text);
		s.setStatus(SubmissionStatus.PENDING);
		s.setSubmittedAt(Instant.now());
		s = submissions.save(s); // the unique (challenge,user) key stops a double-click race
		registry.broadcastAfterCommit(roomId, "challenge", new ChallengeEvent("submitted", challengeId));
		return toSubmission(s, true);
	}

	@Transactional(readOnly = true)
	public List<SubmissionView> submissionsFor(Integer hostId, Integer roomId, Integer challengeId) {
		roomService.requireHost(roomId, hostId);
		find(roomId, challengeId);
		return submissions.findAllForChallenge(challengeId).stream().map(s -> toSubmission(s, true)).toList();
	}

	@Transactional
	public SubmissionView review(
		Integer hostId,
		Integer roomId,
		Integer challengeId,
		Integer submissionId,
		boolean approve
	) {
		roomService.requireHost(roomId, hostId);
		StudyRoomChallenge c = find(roomId, challengeId);
		StudyRoomSubmission s = submissions
			.findByIdAndChallenge_Id(submissionId, challengeId)
			.orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

		Integer targetUserId = s.getUser().getId(); // read before the bulk update detaches s
		int moved = submissions.review(
			submissionId,
			approve ? SubmissionStatus.APPROVED : SubmissionStatus.REJECTED,
			users.getReferenceById(hostId),
			Instant.now()
		);
		if (moved != 1) throw new StudyRoomException(HttpStatus.CONFLICT, "This submission was already reviewed");

		if (approve) {
			int paid = rewards.award(
				targetUserId,
				c.getRewardCredits(),
				CreditTxType.STUDY_CHALLENGE,
				"Study challenge: " + c.getTitle()
			);
			submissions.setRewardPaid(submissionId, paid);
			registry.sendToUserAfterCommit(
				roomId,
				targetUserId,
				"reward",
				new RewardEvent(paid, paid > 0 ? "Challenge approved" : "Challenge approved (daily credit cap reached)")
			);
		}
		registry.broadcastAfterCommit(roomId, "challenge", new ChallengeEvent("reviewed", challengeId));
		return toSubmission(submissions.findById(submissionId).orElseThrow(), true);
	}

	private StudyRoomChallenge find(Integer roomId, Integer challengeId) {
		return challenges
			.findByIdAndRoom_Id(challengeId, roomId)
			.orElseThrow(() -> new ResourceNotFoundException("Challenge", "id", challengeId));
	}

	private ChallengeView toView(StudyRoomChallenge c, Integer viewerId, boolean viewerIsHost) {
		SubmissionView mine = submissions
			.findByChallenge_IdAndUser_Id(c.getId(), viewerId)
			.map(s -> toSubmission(s, true))
			.orElse(null);
		return new ChallengeView(
			c.getId(),
			c.getTitle(),
			c.getDescription(),
			c.getCompletionCriteria(),
			c.getDeadline(),
			c.getRewardCredits(),
			c.isEvidenceRequired(),
			c.getStatus(),
			submissions.countByChallenge_IdAndStatus(c.getId(), SubmissionStatus.APPROVED),
			viewerIsHost ? submissions.countByChallenge_IdAndStatus(c.getId(), SubmissionStatus.PENDING) : 0,
			mine
		);
	}

	private SubmissionView toSubmission(StudyRoomSubmission s, boolean withEvidence) {
		return new SubmissionView(
			s.getId(),
			s.getUser().getId(),
			StudyRoomService.nameOf(s.getUser()),
			withEvidence ? s.getEvidence() : null,
			s.getStatus(),
			s.getSubmittedAt(),
			s.getRewardPaid()
		);
	}
}
