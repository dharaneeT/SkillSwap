package com.skillSwap.Event;

import java.time.LocalDateTime;

public record SessionReminderEvent(
	Integer sessionId,
	String skillName,
	LocalDateTime sessionTime,
	String learnerName,
	String learnerEmail,
	String providerName,
	String providerEmail
) {}
