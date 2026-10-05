package com.skillSwap.Service;

import com.skillSwap.Event.*;
import java.time.format.DateTimeFormatter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

//Spring registers this as event listener bean
@Component
public class NotificationListener {

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy 'at' HH:mm");
	private final EmailService email;

	public NotificationListener(EmailService email) {
		this.email = email;
	}

	//@Async
	//Runs email sending in background thread
	//Signup API does NOT wait for email
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
	public void onUserRegistered(UserRegisteredEvent e) {
		email.send(
			e.email(),
			"Welcome to SkillSwap, " + e.name() + "!",
			"Hi " +
			e.name() +
			",\n\nYour account is ready and we've added " +
			e.bonusCredits() +
			" credits to your wallet.\nStart by adding the skills you offer and want to learn.\n\n- SkillSwap"
		);
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
	public void onBookingConfirmed(BookingConfirmedEvent e) {
		String when = e.sessionTime().format(FMT);
		email.send(
			e.learnerEmail(),
			"Session confirmed: " + e.skillName(),
			"Hi " +
			e.learnerName() +
			",\n\n" +
			e.providerName() +
			" accepted your " +
			e.skillName() +
			" session on " +
			when +
			".\n\n- SkillSwap"
		);
		email.send(
			e.providerEmail(),
			"Session confirmed: " + e.skillName(),
			"Hi " +
			e.providerName() +
			",\n\nYou will teach " +
			e.skillName() +
			" to " +
			e.learnerName() +
			" on " +
			when +
			".\n\n- SkillSwap"
		);
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
	public void onSessionReminder(SessionReminderEvent e) {
		String when = e.sessionTime().format(FMT);
		email.send(
			e.learnerEmail(),
			"Reminder: " + e.skillName() + " session starts soon",
			"Hi " +
			e.learnerName() +
			",\n\nYour session with " +
			e.providerName() +
			" is at " +
			when +
			".\n\n- SkillSwap"
		);
		email.send(
			e.providerEmail(),
			"Reminder: " + e.skillName() + " session starts soon",
			"Hi " +
			e.providerName() +
			",\n\nYour session with " +
			e.learnerName() +
			" is at " +
			when +
			".\n\n- SkillSwap"
		);
	}
}
//Mail is send for 3 Events
//1. New user register
//2. Session Booking Confirmation
//3. Session reminder
