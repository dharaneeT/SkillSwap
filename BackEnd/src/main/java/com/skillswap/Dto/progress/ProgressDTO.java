package com.skillSwap.Dto.progress;

public record ProgressDTO(
	long sessionsLearned,
	long skillsLearned,
	int currentStreakWeeks,
	int longestStreakWeeks,
	boolean activeThisWeek,
	int certificates
) {}
