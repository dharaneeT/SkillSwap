package com.skillSwap.Service;

import com.skillSwap.Dto.roadmap.RoadmapDtos.*;
import com.skillSwap.Entity.LearningRoadmap;
import com.skillSwap.Entity.RoadmapEnums.LearningPreference;
import com.skillSwap.Entity.RoadmapMilestone;
import com.skillSwap.Entity.RoadmapTask;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RoadmapMapper {

	public RoadmapView toSummary(LearningRoadmap r) {
		return build(r, false);
	}

	public RoadmapView toDetail(LearningRoadmap r) {
		return build(r, true);
	}

	private RoadmapView build(LearningRoadmap r, boolean withMilestones) {
		int totalMilestones = r.getMilestones().size();
		int doneMilestones = 0;
		int totalTasks = 0;
		int doneTasks = 0;
		double hoursTotal = 0;
		double hoursDone = 0;
		List<MilestoneView> milestoneViews = new ArrayList<>();

		for (RoadmapMilestone m : r.getMilestones()) {
			int mTotal = m.getTasks().size();
			int mDone = 0;
			List<TaskView> taskViews = new ArrayList<>();
			for (RoadmapTask t : m.getTasks()) {
				if (t.isCompleted()) mDone++;
				if (withMilestones) taskViews.add(toTask(t));
			}
			totalTasks += mTotal;
			doneTasks += mDone;
			if (m.isCompleted()) doneMilestones++;

			int hours = m.getEstimatedHours() == null ? 0 : m.getEstimatedHours();
			hoursTotal += hours;
			if (mTotal > 0) hoursDone += hours * (mDone / (double) mTotal); else if (m.isCompleted()) hoursDone +=
				hours;

			if (withMilestones) {
				int mPercent = mTotal > 0 ? mDone * 100 / mTotal : (m.isCompleted() ? 100 : 0);
				milestoneViews.add(
					MilestoneView
						.builder()
						.id(m.getId())
						.title(m.getTitle())
						.description(m.getDescription())
						.sequenceNumber(m.getSequenceNumber())
						.estimatedHours(m.getEstimatedHours())
						.learningObjectives(lines(m.getLearningObjectives()))
						.completionCriteria(m.getCompletionCriteria())
						.dependsOn(ints(m.getDependsOn()))
						.completed(m.isCompleted())
						.completedAt(m.getCompletedAt())
						.totalTasks(mTotal)
						.completedTasks(mDone)
						.progressPercent(mPercent)
						.tasks(taskViews)
						.build()
				);
			}
		}

		// THE progress rule (integer division = rounds down, so 100 only when truly everything is done)
		int percent = totalTasks > 0
			? doneTasks * 100 / totalTasks
			: (totalMilestones > 0 ? doneMilestones * 100 / totalMilestones : 0);

		Integer daysLeft = r.getTargetDate() == null
			? null
			: (int) ChronoUnit.DAYS.between(LocalDate.now(), r.getTargetDate());

		return RoadmapView
			.builder()
			.id(r.getId())
			.title(r.getTitle())
			.description(r.getDescription())
			.learningGoal(r.getLearningGoal())
			.targetSkill(r.getTargetSkill())
			.currentLevel(r.getCurrentLevel())
			.targetLevel(r.getTargetLevel())
			.hoursPerWeek(r.getHoursPerWeek())
			.targetDate(r.getTargetDate())
			.daysLeft(daysLeft)
			.preferences(preferences(r.getPreferences()))
			.prerequisites(lines(r.getPrerequisites()))
			.status(r.getStatus())
			.source(r.getSource())
			.progressPercent(percent)
			.totalMilestones(totalMilestones)
			.completedMilestones(doneMilestones)
			.totalTasks(totalTasks)
			.completedTasks(doneTasks)
			.estimatedHoursTotal((int) Math.round(hoursTotal))
			.estimatedHoursCompleted((int) Math.round(hoursDone))
			.next(nextStep(r))
			.createdAt(r.getCreatedAt())
			.updatedAt(r.getUpdatedAt())
			.milestones(withMilestones ? milestoneViews : null)
			.build();
	}

	private TaskView toTask(RoadmapTask t) {
		return TaskView
			.builder()
			.id(t.getId())
			.title(t.getTitle())
			.description(t.getDescription())
			.type(t.getType())
			.sequenceNumber(t.getSequenceNumber())
			.estimatedMinutes(t.getEstimatedMinutes())
			.completed(t.isCompleted())
			.completedAt(t.getCompletedAt())
			.build();
	}

	/** First unfinished milestone, and inside it the first unfinished task. */
	private NextStep nextStep(LearningRoadmap r) {
		for (RoadmapMilestone m : r.getMilestones()) {
			if (m.isCompleted()) continue;
			for (RoadmapTask t : m.getTasks()) {
				if (!t.isCompleted()) {
					return NextStep
						.builder()
						.milestoneId(m.getId())
						.milestoneTitle(m.getTitle())
						.taskId(t.getId())
						.taskTitle(t.getTitle())
						.taskType(t.getType())
						.estimatedMinutes(t.getEstimatedMinutes())
						.build();
				}
			}
			return NextStep.builder().milestoneId(m.getId()).milestoneTitle(m.getTitle()).build();
		}
		return null;
	}

	static List<String> lines(String text) {
		if (text == null || text.isBlank()) return List.of();
		return Arrays.stream(text.split("\\R")).map(String::strip).filter(s -> !s.isEmpty()).toList();
	}

	static List<Integer> ints(String csv) {
		if (csv == null || csv.isBlank()) return List.of();
		List<Integer> out = new ArrayList<>();
		for (String p : csv.split(",")) {
			try {
				out.add(Integer.parseInt(p.strip()));
			} catch (NumberFormatException ignored) {
				// skip bad value
			}
		}
		return out;
	}

	static List<LearningPreference> preferences(String csv) {
		if (csv == null || csv.isBlank()) return List.of();
		List<LearningPreference> out = new ArrayList<>();
		for (String p : csv.split(",")) {
			try {
				out.add(LearningPreference.valueOf(p.strip()));
			} catch (IllegalArgumentException ignored) {
				// skip unknown value
			}
		}
		return out;
	}
}
