package com.skillSwap.Entity;

public final class RoadmapEnums {

	private RoadmapEnums() {}

	public enum Proficiency {
		BEGINNER,
		INTERMEDIATE,
		ADVANCED
	}

	public enum RoadmapStatus {
		ACTIVE,
		COMPLETED
	}

	public enum RoadmapTaskType {
		LEARN,
		PRACTICE,
		PROJECT,
		REVIEW,
		MENTOR_SESSION
	}

	public enum LearningPreference {
		THEORY,
		CODING_EXERCISES,
		PROJECTS,
		MENTOR_LED
	}
}
