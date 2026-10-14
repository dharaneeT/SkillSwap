package com.skillSwap.Entity;

import com.skillSwap.Entity.RoadmapEnums.RoadmapTaskType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roadmap_task")
@Getter
@Setter
@NoArgsConstructor
public class RoadmapTask {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "milestone_id", nullable = false)
	private RoadmapMilestone milestone;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RoadmapTaskType type = RoadmapTaskType.LEARN;

	@Column(nullable = false)
	private int sequenceNumber;

	private Integer estimatedMinutes;

	@Column(nullable = false)
	private boolean completed;

	private LocalDateTime completedAt;
}
