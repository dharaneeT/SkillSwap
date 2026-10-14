package com.skillSwap.Dto.roadmap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.skillSwap.Entity.RoadmapEnums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

public final class RoadmapDtos {

	private RoadmapDtos() {}

	// ======================= REQUESTS =======================

	/** What the user types in the "create roadmap" form. */
	@Data
	@NoArgsConstructor
	public static class GenerateRequest {

		@NotBlank(message = "Learning goal is required")
		@Size(min = 5, max = 500, message = "Learning goal must be 5 to 500 characters")
		private String goal;

		@Size(max = 100, message = "Target skill must be at most 100 characters")
		private String targetSkill;

		@NotNull(message = "Current level is required")
		private Proficiency currentLevel;

		private Proficiency targetLevel;

		@Size(max = 500, message = "Desired outcome must be at most 500 characters")
		private String desiredOutcome;

		@Min(value = 1, message = "Hours per week must be between 1 and 80")
		@Max(value = 80, message = "Hours per week must be between 1 and 80")
		private Integer hoursPerWeek;

		@FutureOrPresent(message = "Target date cannot be in the past")
		private LocalDate targetDate;

		@Min(value = 1, message = "Duration must be between 1 and 104 weeks")
		@Max(value = 104, message = "Duration must be between 1 and 104 weeks")
		private Integer durationWeeks;

		@Size(max = 4, message = "Too many learning preferences")
		private List<LearningPreference> preferences;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TaskDraft {

		@NotBlank(message = "Every task needs a title")
		@Size(max = 200, message = "Task title must be at most 200 characters")
		private String title;

		@Size(max = 2000, message = "Task description is too long")
		private String description;

		private RoadmapTaskType type;

		@Min(value = 5, message = "Task time must be between 5 and 2400 minutes")
		@Max(value = 2400, message = "Task time must be between 5 and 2400 minutes")
		private Integer estimatedMinutes;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MilestoneDraft {

		@NotBlank(message = "Every milestone needs a title")
		@Size(max = 150, message = "Milestone title must be at most 150 characters")
		private String title;

		@Size(max = 2000, message = "Milestone description is too long")
		private String description;

		@Min(value = 1, message = "Milestone hours must be between 1 and 300")
		@Max(value = 300, message = "Milestone hours must be between 1 and 300")
		private Integer estimatedHours;

		@Size(max = 10, message = "Too many learning objectives")
		private List<@NotBlank @Size(
			max = 300,
			message = "A learning objective is too long"
		) String> learningObjectives;

		@Size(max = 1000, message = "Completion criteria is too long")
		private String completionCriteria;

		/** 1-based numbers of EARLIER milestones that should be finished first. */
		@Size(max = 14)
		private List<Integer> dependsOn;

		@NotEmpty(message = "Every milestone needs at least one task")
		@Size(max = 15, message = "A milestone can have at most 15 tasks")
		@Valid
		private List<TaskDraft> tasks;
	}

	/** The roadmap before it is saved. Used for the AI result, the review screen and the save request. */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Draft {

		@NotBlank(message = "Roadmap title is required")
		@Size(max = 150, message = "Roadmap title must be at most 150 characters")
		private String title;

		@Size(max = 4000, message = "Description is too long")
		private String description;

		@NotBlank(message = "Learning goal is required")
		@Size(max = 500, message = "Learning goal must be at most 500 characters")
		private String learningGoal;

		@Size(max = 100, message = "Target skill must be at most 100 characters")
		private String targetSkill;

		@NotNull(message = "Current level is required")
		private Proficiency currentLevel;

		private Proficiency targetLevel;

		@Min(value = 1, message = "Hours per week must be between 1 and 80")
		@Max(value = 80, message = "Hours per week must be between 1 and 80")
		private Integer hoursPerWeek;

		@FutureOrPresent(message = "Target date cannot be in the past")
		private LocalDate targetDate;

		@Size(max = 4, message = "Too many learning preferences")
		private List<LearningPreference> preferences;

		@Size(max = 12, message = "Too many prerequisites")
		private List<@NotBlank @Size(max = 300, message = "A prerequisite is too long") String> prerequisites;

		@NotEmpty(message = "A roadmap needs at least one milestone")
		@Size(max = 20, message = "A roadmap can have at most 20 milestones")
		@Valid
		private List<MilestoneDraft> milestones;

		/** GEMINI, TEMPLATE or MANUAL. Only a label. */
		private String source;
	}

	@Data
	@NoArgsConstructor
	public static class UpdateRequest {

		@NotBlank(message = "Title is required")
		@Size(max = 150, message = "Title must be at most 150 characters")
		private String title;

		@Size(max = 4000, message = "Description is too long")
		private String description;

		private LocalDate targetDate;

		@Min(value = 1, message = "Hours per week must be between 1 and 80")
		@Max(value = 80, message = "Hours per week must be between 1 and 80")
		private Integer hoursPerWeek;
	}

	@Data
	@NoArgsConstructor
	public static class MilestoneUpdateRequest {

		@NotBlank(message = "Milestone title is required")
		@Size(max = 150, message = "Milestone title must be at most 150 characters")
		private String title;

		@Size(max = 2000, message = "Milestone description is too long")
		private String description;

		@Min(value = 1, message = "Milestone hours must be between 1 and 300")
		@Max(value = 300, message = "Milestone hours must be between 1 and 300")
		private Integer estimatedHours;
	}

	@Data
	@NoArgsConstructor
	public static class CompletionRequest {

		@NotNull(message = "completed is required")
		private Boolean completed;
	}

	// ======================= RESPONSES =======================

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TaskView {

		private Long id;
		private String title;
		private String description;
		private RoadmapTaskType type;
		private int sequenceNumber;
		private Integer estimatedMinutes;
		private boolean completed;
		private LocalDateTime completedAt;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MilestoneView {

		private Long id;
		private String title;
		private String description;
		private int sequenceNumber;
		private Integer estimatedHours;
		private List<String> learningObjectives;
		private String completionCriteria;
		private List<Integer> dependsOn;
		private boolean completed;
		private LocalDateTime completedAt;
		private int totalTasks;
		private int completedTasks;
		private int progressPercent;
		private List<TaskView> tasks;
	}

	/** The recommended next step. taskId/taskTitle are null if the milestone has no tasks. */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class NextStep {

		private Long milestoneId;
		private String milestoneTitle;
		private Long taskId;
		private String taskTitle;
		private RoadmapTaskType taskType;
		private Integer estimatedMinutes;
	}

	/** Used for both list items (milestones == null) and the detail page (milestones filled). */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RoadmapView {

		private Long id;
		private String title;
		private String description;
		private String learningGoal;
		private String targetSkill;
		private Proficiency currentLevel;
		private Proficiency targetLevel;
		private Integer hoursPerWeek;
		private LocalDate targetDate;
		/** Negative means overdue. Null when there is no target date. */
		private Integer daysLeft;
		private List<LearningPreference> preferences;
		private List<String> prerequisites;
		private RoadmapStatus status;
		private String source;
		private int progressPercent;
		private int totalMilestones;
		private int completedMilestones;
		private int totalTasks;
		private int completedTasks;
		private int estimatedHoursTotal;
		private int estimatedHoursCompleted;
		private NextStep next;
		private LocalDateTime createdAt;
		private LocalDateTime updatedAt;

		@JsonInclude(JsonInclude.Include.NON_NULL)
		private List<MilestoneView> milestones;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class DashboardView {

		private int totalRoadmaps;
		private int activeRoadmaps;
		private int completedRoadmaps;
		private int overallProgressPercent;
		private int completedMilestones;
		private int remainingMilestones;
		private int completedTasks;
		private int totalTasks;
		private int hoursCompleted;
		private int hoursRemaining;
		/** Active roadmaps with a target date, soonest first (max 5). */
		private List<RoadmapView> upcomingDeadlines;
		/** Active roadmaps with a next step, most recently touched first (max 3). */
		private List<RoadmapView> inProgress;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TemplateInfo {

		private String id;
		private String title;
		private String description;
		private int milestoneCount;
	}
}
