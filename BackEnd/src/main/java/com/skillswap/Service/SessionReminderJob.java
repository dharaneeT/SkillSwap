package com.skillSwap.Service;

import com.skillSwap.Entity.Session;
import com.skillSwap.Entity.SessionStatus;
import com.skillSwap.Event.SessionReminderEvent;
import com.skillSwap.Repository.SessionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SessionReminderJob {

	private static final Logger log = LoggerFactory.getLogger(SessionReminderJob.class);

	private final SessionRepository sessionRepository;
	private final ApplicationEventPublisher events;
	private final int leadMinutes;

	public SessionReminderJob(
		SessionRepository sessionRepository,
		ApplicationEventPublisher events,
		@Value("${skillswap.reminder.lead-minutes:60}") int leadMinutes
	) {
		this.sessionRepository = sessionRepository;
		this.events = events;
		this.leadMinutes = leadMinutes;
	}

	@Scheduled(fixedDelay = 60_000)
	@Transactional
	public void sendReminders() {
		LocalDateTime now = LocalDateTime.now();
		List<Session> due = sessionRepository.findDueForReminder(
			SessionStatus.ACCEPTED,
			now,
			now.plusMinutes(leadMinutes)
		);

		for (Session s : due) {
			events.publishEvent(
				new SessionReminderEvent(
					s.getId(),
					s.getSkill() != null ? s.getSkill().getName() : "your skill",
					s.getSessionTime(),
					s.getLearner().getName(),
					s.getLearner().getEmail(),
					s.getProvider().getName(),
					s.getProvider().getEmail()
				)
			);
			s.setReminderSent(true);
		}
		if (!due.isEmpty()) log.info("Queued {} session reminder(s)", due.size());
	}
}
