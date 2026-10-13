package com.skillSwap.Service;

import com.skillSwap.Dto.progress.ProgressDTO;
import com.skillSwap.Entity.Session;
import com.skillSwap.Entity.SessionStatus;
import com.skillSwap.Entity.User;
import com.skillSwap.Repository.SessionRepository;
import com.skillSwap.Security.CurrentUserService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressService {

	private final SessionRepository sessionRepository;
	private final CertificateService certificateService;
	private final CurrentUserService currentUserService;

	public ProgressService(
		SessionRepository sessionRepository,
		CertificateService certificateService,
		CurrentUserService currentUserService
	) {
		this.sessionRepository = sessionRepository;
		this.certificateService = certificateService;
		this.currentUserService = currentUserService;
	}

	@Transactional(readOnly = true)
	public ProgressDTO getMyProgress() {
		User me = currentUserService.getCurrentUser();
		List<Session> done = sessionRepository
			.findUserSessions(me.getId())
			.stream()
			.filter(s -> s.getStatus() == SessionStatus.COMPLETED && s.getSessionTime() != null)
			.toList();

		List<Session> asLearner = done
			.stream()
			.filter(s -> s.getLearner() != null && me.getId().equals(s.getLearner().getId()))
			.toList();
		long skillsLearned = asLearner
			.stream()
			.filter(s -> s.getSkill() != null)
			.map(s -> s.getSkill().getId())
			.distinct()
			.count();

		// a week counts if you finished a session in it, as teacher OR learner
		LocalDate thisWeek = weekStart(LocalDate.now());
		TreeSet<LocalDate> weeks = done
			.stream()
			.map(s -> weekStart(s.getSessionTime().toLocalDate()))
			.filter(w -> !w.isAfter(thisWeek))
			.collect(Collectors.toCollection(TreeSet::new));

		boolean activeThisWeek = weeks.contains(thisWeek);
		// the streak stays alive until the week ends: start from last week if nothing yet this week
		LocalDate cursor = activeThisWeek ? thisWeek : thisWeek.minusWeeks(1);
		int current = 0;
		while (weeks.contains(cursor)) {
			current++;
			cursor = cursor.minusWeeks(1);
		}

		int longest = 0;
		int run = 0;
		LocalDate prev = null;
		for (LocalDate w : weeks) {
			run = (prev != null && prev.plusWeeks(1).equals(w)) ? run + 1 : 1;
			longest = Math.max(longest, run);
			prev = w;
		}

		return new ProgressDTO(
			asLearner.size(),
			skillsLearned,
			current,
			longest,
			activeThisWeek,
			certificateService.countFor(me.getId())
		);
	}

	private static LocalDate weekStart(LocalDate d) {
		return d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
	}
}
