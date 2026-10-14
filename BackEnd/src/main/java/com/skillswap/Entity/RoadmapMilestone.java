package com.skillSwap.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "roadmap_milestone")
@Getter
@Setter
@NoArgsConstructor
public class RoadmapMilestone {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "roadmap_id", nullable = false)
	private LearningRoadmap roadmap;

	@Column(nullable = false, length = 150)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false)
	private int sequenceNumber;

	private Integer estimatedHours;

	// one objective per line
	@Column(columnDefinition = "TEXT")
	private String learningObjectives;

	@Column(columnDefinition = "TEXT")
	private String completionCriteria;

	// comma-separated sequence numbers of earlier milestones, e.g. "1,3"
	@Column(length = 100)
	private String dependsOn;

	@Column(nullable = false)
	private boolean completed;

	private LocalDateTime completedAt;

	@OneToMany(mappedBy = "milestone", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sequenceNumber ASC")
	@BatchSize(size = 50)
	private List<RoadmapTask> tasks = new ArrayList<>();
}
