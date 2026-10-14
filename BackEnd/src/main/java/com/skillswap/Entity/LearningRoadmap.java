package com.skillSwap.Entity;

import com.skillSwap.Entity.RoadmapEnums.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "learning_roadmap")
@Getter
@Setter
@NoArgsConstructor
public class LearningRoadmap {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;

	@Column(nullable = false, length = 150)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false, length = 500)
	private String learningGoal;

	@Column(length = 100)
	private String targetSkill;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Proficiency currentLevel;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private Proficiency targetLevel;

	private Integer hoursPerWeek;

	private LocalDate targetDate;

	// comma-separated LearningPreference names, e.g. "THEORY,PROJECTS"
	@Column(length = 200)
	private String preferences;

	// one prerequisite per line
	@Column(columnDefinition = "TEXT")
	private String prerequisites;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RoadmapStatus status = RoadmapStatus.ACTIVE;

	// GEMINI, TEMPLATE or MANUAL (a label only, shown in the UI)
	@Column(nullable = false, length = 20)
	private String source = "MANUAL";

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime completedAt;

	@OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sequenceNumber ASC")
	@BatchSize(size = 50)
	private List<RoadmapMilestone> milestones = new ArrayList<>();

	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
		if (status == null) status = RoadmapStatus.ACTIVE;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	/** Marks the roadmap as changed even when only a child row changed. */
	public void touch() {
		updatedAt = LocalDateTime.now();
	}
}
