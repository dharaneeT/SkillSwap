package com.skillSwap.Service;

import com.skillSwap.Dto.session.SessionRequestDTO;
import com.skillSwap.Dto.session.SessionResponseDTO;
import com.skillSwap.Entity.*;
import com.skillSwap.Event.BookingConfirmedEvent;
import com.skillSwap.Exception.CreditException;
import com.skillSwap.Exception.InvalidSessionStateException;
import com.skillSwap.Exception.SessionConflictException;
import com.skillSwap.Exception.SessionNotFoundException;
import com.skillSwap.Repository.ReviewRepository;
import com.skillSwap.Repository.SessionRepository;
import com.skillSwap.Repository.UserRepository;
import com.skillSwap.Security.CurrentUserService;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionService {

	private final SessionRepository sessionRepository;
	private final UserRepository userRepository;
	private final UserService userService;
	private final SkillService skillService;
	private final CurrentUserService currentUserService;
	private final CreditWalletService wallet;
	private final ApplicationEventPublisher events;
	private final int sessionCost;
	private final int durationMinutes;
	private final ReviewRepository reviewRepository;

	public SessionService(
		SessionRepository sessionRepository,
		UserRepository userRepository,
		UserService userService,
		SkillService skillService,
		CurrentUserService currentUserService,
		CreditWalletService wallet,
		ApplicationEventPublisher events,
		@Value("${skillswap.credits.session-cost}") int sessionCost,
		@Value("${skillswap.session.duration-minutes:60}") int durationMinutes,
		ReviewRepository reviewRepository
	) {
		this.sessionRepository = sessionRepository;
		this.userRepository = userRepository;
		this.userService = userService;
		this.skillService = skillService;
		this.currentUserService = currentUserService;
		this.wallet = wallet;
		this.events = events;
		this.sessionCost = sessionCost;
		this.durationMinutes = durationMinutes;
		this.reviewRepository = reviewRepository;
	}

	// ---------- BOOK (learner = logged-in user) ----------
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public SessionResponseDTO bookSession(SessionRequestDTO dto) {
		User me = currentUserService.getCurrentUser();

		if (dto.getProviderId().equals(me.getId())) throw new IllegalArgumentException(
			"You cannot book your own session"
		);
		if (!dto.getSessionTime().isAfter(LocalDateTime.now())) throw new IllegalArgumentException(
			"Session time must be in the future"
		);

		User provider = userService.getUserEntityById(dto.getProviderId());
		if (Boolean.FALSE.equals(provider.getActive())) throw new IllegalArgumentException(
			"This provider is not available"
		);
		Skill skill = skillService.getSkillEntityById(dto.getSkillId());

		if (wallet.getBalance(me.getId()) < sessionCost) throw new CreditException(
			"Not enough credits. A session costs " + sessionCost + " credits"
		);

		if (
			sessionRepository.existsByProvider_IdAndLearner_IdAndSessionTimeAndStatus(
				provider.getId(),
				me.getId(),
				dto.getSessionTime(),
				SessionStatus.PENDING
			)
		) throw new SessionConflictException("You already requested this time slot");
		assertFree(
			provider.getId(),
			dto.getSessionTime(),
			-1,
			"The provider already has an accepted session at that time"
		);
		assertFree(me.getId(), dto.getSessionTime(), -1, "You already have an accepted session at that time");

		Session s = new Session();
		s.setProvider(provider);
		s.setLearner(me);
		s.setSkill(skill);
		s.setSessionTime(dto.getSessionTime());
		s.setStatus(SessionStatus.PENDING);
		return toDto(sessionRepository.save(s));
	}

	// ---------- ACCEPT (provider) ----------
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public SessionResponseDTO accept(Integer sessionId) {
		User me = currentUserService.getCurrentUser();
		Session s = loadForUpdate(sessionId);
		requireProvider(s, me, "Only the provider can accept a session");
		requireStatus(s, SessionStatus.PENDING, "Only pending sessions can be accepted");
		if (!s.getSessionTime().isAfter(LocalDateTime.now())) throw new InvalidSessionStateException(
			"This session time has already passed"
		);

		Integer providerId = s.getProvider().getId();
		Integer learnerId = s.getLearner().getId();
		lockUsers(providerId, learnerId); // serializes concurrent accepts for the same people
		assertFree(providerId, s.getSessionTime(), s.getId(), "You already have an accepted session at that time");
		assertFree(
			learnerId,
			s.getSessionTime(),
			s.getId(),
			"The learner already has an accepted session at that time"
		);

		wallet.debit(
			learnerId,
			sessionCost,
			CreditTxType.SESSION_PAYMENT,
			s.getId(),
			"Payment for session #" + s.getId()
		); // throws CreditException -> whole accept rolls back

		s.setStatus(SessionStatus.ACCEPTED);
		Session saved = sessionRepository.save(s);
		events.publishEvent(
			new BookingConfirmedEvent(
				saved.getId(),
				saved.getSkill() != null ? saved.getSkill().getName() : "your skill",
				saved.getSessionTime(),
				saved.getLearner().getName(),
				saved.getLearner().getEmail(),
				saved.getProvider().getName(),
				saved.getProvider().getEmail()
			)
		);
		return toDto(saved);
	}

	// ---------- REJECT (provider) ----------
	@Transactional
	public SessionResponseDTO reject(Integer sessionId) {
		User me = currentUserService.getCurrentUser();
		Session s = loadForUpdate(sessionId);
		requireProvider(s, me, "Only the provider can reject a session");
		requireStatus(s, SessionStatus.PENDING, "Only pending sessions can be rejected");
		s.setStatus(SessionStatus.REJECTED);
		return toDto(sessionRepository.save(s));
	}

	// ---------- CANCEL ----------
	@Transactional
	public SessionResponseDTO cancel(Integer sessionId) {
		User me = currentUserService.getCurrentUser();
		Session s = loadForUpdate(sessionId);
		boolean isLearner = s.getLearner().getId().equals(me.getId());
		boolean isProvider = s.getProvider().getId().equals(me.getId());
		if (!isLearner && !isProvider) throw new AccessDeniedException("Not your session");

		if (s.getStatus() == SessionStatus.PENDING) {
			if (!isLearner) throw new InvalidSessionStateException(
				"A pending request can only be cancelled by the learner; use reject instead"
			);
		} else if (s.getStatus() == SessionStatus.ACCEPTED) {
			// learner was already charged on accept -> refund
			wallet.credit(
				s.getLearner().getId(),
				sessionCost,
				CreditTxType.SESSION_REFUND,
				s.getId(),
				"Refund for cancelled session #" + s.getId()
			);
		} else {
			throw new InvalidSessionStateException("Only pending or accepted sessions can be cancelled");
		}
		s.setStatus(SessionStatus.CANCELLED);
		return toDto(sessionRepository.save(s));
	}

	// ---------- COMPLETE (provider) ----------
	@Transactional
	public SessionResponseDTO complete(Integer sessionId) {
		User me = currentUserService.getCurrentUser();
		Session s = loadForUpdate(sessionId);
		requireProvider(s, me, "Only the provider can mark a session as completed");
		requireStatus(s, SessionStatus.ACCEPTED, "Only accepted sessions can be completed");
		if (s.getSessionTime().isAfter(LocalDateTime.now())) throw new InvalidSessionStateException(
			"The session has not started yet"
		);

		wallet.credit(
			s.getProvider().getId(),
			sessionCost,
			CreditTxType.SESSION_EARNING,
			s.getId(),
			"Earnings for session #" + s.getId()
		);
		s.setStatus(SessionStatus.COMPLETED);
		return toDto(sessionRepository.save(s));
	}

	@Transactional(readOnly = true)
	public List<SessionResponseDTO> mySessions() {
		User me = currentUserService.getCurrentUser();
		List<Session> list = sessionRepository.findUserSessions(me.getId());
		Set<Integer> reviewed = list.isEmpty()
			? Set.of()
			: new HashSet<>(reviewRepository.findReviewedSessionIds(list.stream().map(Session::getId).toList()));
		return list
			.stream()
			.map(s -> {
				SessionResponseDTO d = toDto(s);
				d.setReviewed(reviewed.contains(s.getId()));
				return d;
			})
			.toList();
	}

	// ---------- helpers ----------
	private Session loadForUpdate(Integer id) {
		return sessionRepository.findByIdForUpdate(id).orElseThrow(() -> new SessionNotFoundException(id));
	}

	private void requireProvider(Session s, User me, String message) {
		if (!s.getProvider().getId().equals(me.getId())) throw new AccessDeniedException(message);
	}

	private void requireStatus(Session s, SessionStatus expected, String message) {
		if (s.getStatus() != expected) throw new InvalidSessionStateException(
			message + " (current status: " + s.getStatus() + ")"
		);
	}

	private void assertFree(Integer userId, LocalDateTime start, int excludeId, String message) {
		LocalDateTime from = start.minusMinutes(durationMinutes);
		LocalDateTime to = start.plusMinutes(durationMinutes);
		if (
			sessionRepository.countOverlaps(userId, SessionStatus.ACCEPTED, from, to, excludeId) > 0
		) throw new SessionConflictException(message);
	}

	private void lockUsers(Integer a, Integer b) { // always lowest id first -> no deadlocks
		userRepository.findByIdForUpdate(Math.min(a, b));
		userRepository.findByIdForUpdate(Math.max(a, b));
	}

	private SessionResponseDTO toDto(Session s) {
		SessionResponseDTO d = new SessionResponseDTO();
		d.setId(s.getId());
		d.setProviderId(s.getProvider().getId());
		d.setLearnerId(s.getLearner().getId());
		d.setProviderName(s.getProvider().getName());
		d.setLearnerName(s.getLearner().getName());
		d.setSkillName(s.getSkill() != null ? s.getSkill().getName() : null);
		d.setStatus(s.getStatus());
		d.setSessionTime(s.getSessionTime());
		return d;
	}
}
