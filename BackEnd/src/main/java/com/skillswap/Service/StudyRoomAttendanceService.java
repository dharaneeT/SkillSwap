package com.skillSwap.Service;

import com.skillSwap.Dto.studyroom.StudyRoomDtos.*;
import com.skillSwap.Entity.*;
import com.skillSwap.Entity.StudyRoomEnums.RoomStatus;
import com.skillSwap.Exception.ResourceNotFoundException;
import com.skillSwap.Repository.StudyRoomAttendanceRepository;
import com.skillSwap.Repository.StudyRoomRepository;
import com.skillSwap.Repository.UserRepository;
import com.skillSwap.websocket.StudyRoomPresenceRegistry;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Attendance is measured by the SERVER: a heartbeat arrives every ~15s over the websocket, and time only counts while
 * the room has a live session, the user's tab is visible, and at least minParticipants people are present.
 * A browser can never prove someone is really paying attention - this only proves "connected, tab in front, with others".
 */
@Service
public class StudyRoomAttendanceService {

	private static final int MAX_GAP_SECONDS = 30; // a longer gap between heartbeats is not counted

	private final StudyRoomAttendanceRepository attendance;
	private final StudyRoomRepository rooms;
	private final UserRepository users;
	private final StudyRewardService rewards;
	private final StudyRoomPresenceRegistry registry;
	private final int minMinutes;
	private final int rewardCredits;
	private final int minParticipants;

	public StudyRoomAttendanceService(
		StudyRoomAttendanceRepository attendance,
		StudyRoomRepository rooms,
		UserRepository users,
		StudyRewardService rewards,
		StudyRoomPresenceRegistry registry,
		@Value("${skillswap.study.min-attendance-minutes:15}") int minMinutes,
		@Value("${skillswap.study.attendance-reward:1}") int rewardCredits,
		@Value("${skillswap.study.min-participants-for-reward:2}") int minParticipants
	) {
		this.attendance = attendance;
		this.rooms = rooms;
		this.users = users;
		this.rewards = rewards;
		this.registry = registry;
		this.minMinutes = minMinutes;
		this.rewardCredits = rewardCredits;
		this.minParticipants = minParticipants;
	}

	public int requiredSeconds() {
		return minMinutes * 60;
	}

	/** Called for every heartbeat of a connected browser. */
	@Transactional
	public void onHeartbeat(Integer roomId, Integer userId, boolean visible, int presentCount) {
		StudyRoom room = rooms.findById(roomId).orElse(null);
		if (room == null || room.getStatus() != RoomStatus.LIVE) return;

		Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
		StudyRoomAttendance a = attendance
			.findByRoom_IdAndUser_IdAndSessionNo(roomId, userId, room.getSessionNo())
			.orElse(null);
		if (a == null) {
			a = new StudyRoomAttendance();
			a.setRoom(room);
			a.setUser(users.getReferenceById(userId));
			a.setSessionNo(room.getSessionNo());
			a.setFirstJoinedAt(now);
			a.setLastActiveAt(now);
			attendance.save(a); // a duplicate (two tabs at the same millisecond) hits the unique key; the next heartbeat succeeds
			return;
		}

		Instant prev = a.getLastActiveAt();
		boolean counts = visible && presentCount >= minParticipants && prev != null;
		if (counts) {
			long gap = Math.max(0, Math.min(Duration.between(prev, now).getSeconds(), MAX_GAP_SECONDS));
			if (gap > 0) attendance.addActive(a.getId(), (int) gap, now, prev); else return;
		} else {
			attendance.touch(a.getId(), now);
			return;
		}

		StudyRoomAttendance fresh = attendance.findById(a.getId()).orElse(null);
		if (fresh != null && !fresh.isRewardIssued() && fresh.getActiveSeconds() >= requiredSeconds()) {
			giveReward(fresh, room);
		}
	}

	private void giveReward(StudyRoomAttendance a, StudyRoom room) {
		if (rewardCredits <= 0) return;
		// only one caller in the whole system can win this update, so the reward can never be paid twice
		if (attendance.claimReward(a.getId()) != 1) return;
		int paid = rewards.award(
			a.getUser().getId(),
			rewardCredits,
			CreditTxType.STUDY_ATTENDANCE,
			"Study room attendance: " + room.getName()
		);
		attendance.setRewardCredits(a.getId(), paid);
		registry.sendToUserAfterCommit(
			room.getId(),
			a.getUser().getId(),
			"reward",
			new RewardEvent(paid, paid > 0 ? "Attendance reward" : "Attendance complete (daily credit cap reached)")
		);
	}

	@Transactional
	public void noteLeave(Integer roomId, Integer userId) {
		rooms.findById(roomId).ifPresent(r -> attendance.markLeft(roomId, userId, r.getSessionNo(), Instant.now()));
	}

	@Transactional
	public void finalizeSession(Integer roomId, Integer sessionNo) {
		attendance.completeQualified(roomId, sessionNo, requiredSeconds());
	}

	@Transactional
	public void incrementFocusCycle(Integer roomId, Integer sessionNo, Collection<Integer> presentUserIds) {
		if (!presentUserIds.isEmpty()) attendance.incrementFocus(roomId, sessionNo, presentUserIds);
	}

	@Transactional(readOnly = true)
	public AttendanceSummary mine(Integer roomId, Integer userId) {
		StudyRoom room = rooms
			.findById(roomId)
			.orElseThrow(() -> new ResourceNotFoundException("Study room", "id", roomId));
		List<AttendanceView> history = attendance
			.findByRoom_IdAndUser_IdOrderBySessionNoDesc(roomId, userId, PageRequest.of(0, 20))
			.stream()
			.map(this::toView)
			.toList();
		AttendanceView current = history
			.stream()
			.filter(h -> room.getStatus() == RoomStatus.LIVE && h.sessionNo() == room.getSessionNo())
			.findFirst()
			.orElse(null);
		return new AttendanceSummary(current, history);
	}

	private AttendanceView toView(StudyRoomAttendance a) {
		return new AttendanceView(
			a.getSessionNo(),
			a.getFirstJoinedAt(),
			a.getLastLeftAt(),
			a.getActiveSeconds(),
			requiredSeconds(),
			a.getFocusCycles(),
			a.isCompleted(),
			a.isRewardIssued(),
			a.getRewardCredits()
		);
	}
}
