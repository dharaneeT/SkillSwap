package com.skillSwap.Service;

import com.skillSwap.Dto.roadmap.RoadmapDtos.*;
import com.skillSwap.Entity.LearningRoadmap;
import com.skillSwap.Entity.RoadmapEnums.RoadmapStatus;
import com.skillSwap.Entity.RoadmapEnums.RoadmapTaskType;
import com.skillSwap.Entity.RoadmapMilestone;
import com.skillSwap.Entity.RoadmapTask;
import com.skillSwap.Entity.User;
import com.skillSwap.Exception.ResourceNotFoundException;
import com.skillSwap.Repository.LearningRoadmapRepository;
import com.skillSwap.Security.CurrentUserService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoadmapService {

	private static final int MAX_ROADMAPS_PER_USER = 20;
	private static final Set<String> SOURCES = Set.of("GEMINI", "TEMPLATE", "MANUAL");

	private final LearningRoadmapRepository repo;
	private final CurrentUserService currentUserService;
	private final RoadmapMapper mapper;

	public RoadmapService(LearningRoadmapRepository repo, CurrentUserService currentUserService, RoadmapMapper mapper) {
		this.repo = repo;
		this.currentUserService = currentUserService;
		this.mapper = mapper;
	}

	// ------------------------------------------------------------ reads

	@Transactional(readOnly = true)
	public List<RoadmapView> list() {
		return repo.findByOwner_IdOrderByUpdatedAtDesc(me().getId()).stream().map(mapper::toSummary).toList();
	}

	@Transactional(readOnly = true)
	public RoadmapView get(Long id) {
		return mapper.toDetail(findOwned(id));
	}

	@Transactional(readOnly = true)
	public DashboardView dashboard() {
		List<RoadmapView> all = repo
			.findByOwner_IdOrderByUpdatedAtDesc(me().getId())
			.stream()
			.map(mapper::toSummary)
			.toList();

		int active = 0, completed = 0, cm = 0, tm = 0, ct = 0, tt = 0, hDone = 0, hTotal = 0;
		for (RoadmapView v : all) {
			if (v.getStatus() == RoadmapStatus.COMPLETED) completed++; else active++;
			cm += v.getCompletedMilestones();
			tm += v.getTotalMilestones();
			ct += v.getCompletedTasks();
			tt += v.getTotalTasks();
			hDone += v.getEstimatedHoursCompleted();
			hTotal += v.getEstimatedHoursTotal();
		}
		// same rule as a single roadmap, applied to all roadmaps together
		int overall = tt > 0 ? ct * 100 / tt : (tm > 0 ? cm * 100 / tm : 0);

		List<RoadmapView> activeOnes = all.stream().filter(v -> v.getStatus() == RoadmapStatus.ACTIVE).toList();

		return DashboardView
			.builder()
			.totalRoadmaps(all.size())
			.activeRoadmaps(active)
			.completedRoadmaps(completed)
			.overallProgressPercent(overall)
			.completedMilestones(cm)
			.remainingMilestones(tm - cm)
			.completedTasks(ct)
			.totalTasks(tt)
			.hoursCompleted(hDone)
			.hoursRemaining(Math.max(0, hTotal - hDone))
			.upcomingDeadlines(
				activeOnes
					.stream()
					.filter(v -> v.getTargetDate() != null)
					.sorted(Comparator.comparing(RoadmapView::getTargetDate))
					.limit(5)
					.toList()
			)
			.inProgress(activeOnes.stream().filter(v -> v.getNext() != null).limit(3).toList())
			.build();
	}

	// ------------------------------------------------------------ create / edit / delete

	@Transactional
	public RoadmapView create(Draft draft) {
		User me = me();
		if (repo.countByOwner_Id(me.getId()) >= MAX_ROADMAPS_PER_USER) {
			throw new IllegalArgumentException(
				"You can keep up to " + MAX_ROADMAPS_PER_USER + " roadmaps. Delete one to create another."
			);
		}

		LearningRoadmap r = new LearningRoadmap();
		r.setOwner(me); // owner always comes from the JWT, never from the request body
		r.setTitle(draft.getTitle().strip());
		r.setDescription(blankToNull(draft.getDescription()));
		r.setLearningGoal(draft.getLearningGoal().strip());
		r.setTargetSkill(blankToNull(draft.getTargetSkill()));
		r.setCurrentLevel(draft.getCurrentLevel());
		r.setTargetLevel(draft.getTargetLevel());
		r.setHoursPerWeek(draft.getHoursPerWeek());
		r.setTargetDate(draft.getTargetDate());
		r.setPreferences(
			draft.getPreferences() == null
				? null
				: draft.getPreferences().stream().distinct().map(Enum::name).collect(Collectors.joining(","))
		);
		r.setPrerequisites(draft.getPrerequisites() == null ? null : String.join("\n", draft.getPrerequisites()));
		r.setSource(
			draft.getSource() != null && SOURCES.contains(draft.getSource().toUpperCase())
				? draft.getSource().toUpperCase()
				: "MANUAL"
		);
		r.setStatus(RoadmapStatus.ACTIVE);

		int seq = 1;
		for (MilestoneDraft md : draft.getMilestones()) {
			RoadmapMilestone m = new RoadmapMilestone();
			m.setRoadmap(r);
			m.setSequenceNumber(seq);
			m.setTitle(md.getTitle().strip());
			m.setDescription(blankToNull(md.getDescription()));
			m.setEstimatedHours(md.getEstimatedHours());
			m.setLearningObjectives(
				md.getLearningObjectives() == null ? null : String.join("\n", md.getLearningObjectives())
			);
			m.setCompletionCriteria(blankToNull(md.getCompletionCriteria()));
			m.setDependsOn(joinDependencies(md.getDependsOn(), seq));

			int taskSeq = 1;
			for (TaskDraft td : md.getTasks()) {
				RoadmapTask t = new RoadmapTask();
				t.setMilestone(m);
				t.setSequenceNumber(taskSeq++);
				t.setTitle(td.getTitle().strip());
				t.setDescription(blankToNull(td.getDescription()));
				t.setType(td.getType() == null ? RoadmapTaskType.LEARN : td.getType());
				t.setEstimatedMinutes(td.getEstimatedMinutes());
				m.getTasks().add(t);
			}
			r.getMilestones().add(m);
			seq++;
		}
		return mapper.toDetail(repo.save(r));
	}

	@Transactional
	public RoadmapView update(Long id, UpdateRequest req) {
		LearningRoadmap r = findOwned(id);
		r.setTitle(req.getTitle().strip());
		r.setDescription(blankToNull(req.getDescription()));
		r.setTargetDate(req.getTargetDate());
		r.setHoursPerWeek(req.getHoursPerWeek());
		r.touch();
		return mapper.toDetail(r);
	}

	@Transactional
	public void delete(Long id) {
		repo.delete(findOwned(id)); // cascade removes milestones and tasks
	}

	@Transactional
	public RoadmapView updateMilestone(Long roadmapId, Long milestoneId, MilestoneUpdateRequest req) {
		LearningRoadmap r = findOwned(roadmapId);
		RoadmapMilestone m = findMilestone(r, milestoneId);
		m.setTitle(req.getTitle().strip());
		m.setDescription(blankToNull(req.getDescription()));
		m.setEstimatedHours(req.getEstimatedHours());
		r.touch();
		return mapper.toDetail(r);
	}

	// ------------------------------------------------------------ progress

	@Transactional
	public RoadmapView setTaskCompleted(Long roadmapId, Long taskId, boolean completed) {
		LearningRoadmap r = findOwned(roadmapId);
		RoadmapTask task = null;
		for (RoadmapMilestone m : r.getMilestones()) {
			for (RoadmapTask t : m.getTasks()) {
				if (t.getId().equals(taskId)) task = t;
			}
		}
		if (task == null) throw new ResourceNotFoundException("Task", "id", taskId);

		applyTask(task, completed);
		syncMilestone(task.getMilestone());
		syncRoadmap(r);
		return mapper.toDetail(r);
	}

	@Transactional
	public RoadmapView setMilestoneCompleted(Long roadmapId, Long milestoneId, boolean completed) {
		LearningRoadmap r = findOwned(roadmapId);
		RoadmapMilestone m = findMilestone(r, milestoneId);

		if (m.getTasks().isEmpty()) {
			m.setCompleted(completed);
			m.setCompletedAt(completed ? LocalDateTime.now() : null);
		} else {
			for (RoadmapTask t : m.getTasks()) applyTask(t, completed);
			syncMilestone(m);
		}
		syncRoadmap(r);
		return mapper.toDetail(r);
	}

	// ------------------------------------------------------------ helpers

	private void applyTask(RoadmapTask t, boolean completed) {
		if (t.isCompleted() == completed) return;
		t.setCompleted(completed);
		t.setCompletedAt(completed ? LocalDateTime.now() : null);
	}

	/** A milestone is complete exactly when all of its tasks are complete. */
	private void syncMilestone(RoadmapMilestone m) {
		if (m.getTasks().isEmpty()) return;
		boolean allDone = m.getTasks().stream().allMatch(RoadmapTask::isCompleted);
		if (allDone != m.isCompleted()) {
			m.setCompleted(allDone);
			m.setCompletedAt(allDone ? LocalDateTime.now() : null);
		}
	}

	/** A roadmap is COMPLETED exactly when all of its milestones are complete. */
	private void syncRoadmap(LearningRoadmap r) {
		boolean allDone =
			!r.getMilestones().isEmpty() && r.getMilestones().stream().allMatch(RoadmapMilestone::isCompleted);
		if (allDone && r.getStatus() != RoadmapStatus.COMPLETED) {
			r.setStatus(RoadmapStatus.COMPLETED);
			r.setCompletedAt(LocalDateTime.now());
		} else if (!allDone && r.getStatus() != RoadmapStatus.ACTIVE) {
			r.setStatus(RoadmapStatus.ACTIVE);
			r.setCompletedAt(null);
		}
		r.touch();
	}

	private LearningRoadmap findOwned(Long id) {
		return repo
			.findByIdAndOwner_Id(id, me().getId())
			.orElseThrow(() -> new ResourceNotFoundException("Roadmap", "id", id)); // 404 for other people's roadmaps too
	}

	private RoadmapMilestone findMilestone(LearningRoadmap r, Long milestoneId) {
		return r
			.getMilestones()
			.stream()
			.filter(m -> m.getId().equals(milestoneId))
			.findFirst()
			.orElseThrow(() -> new ResourceNotFoundException("Milestone", "id", milestoneId));
	}

	private User me() {
		return currentUserService.getCurrentUser();
	}

	private static String blankToNull(String s) {
		return s == null || s.isBlank() ? null : s.strip();
	}

	/** Keeps only valid, distinct references to EARLIER milestones. */
	private static String joinDependencies(List<Integer> deps, int ownSequence) {
		if (deps == null || deps.isEmpty()) return null;
		List<Integer> ok = new ArrayList<>();
		for (Integer d : deps) {
			if (d != null && d >= 1 && d < ownSequence && !ok.contains(d)) ok.add(d);
		}
		return ok.isEmpty() ? null : ok.stream().map(String::valueOf).collect(Collectors.joining(","));
	}
}
