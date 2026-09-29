package com.skillSwap.skillswap.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Session {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	// Who teaches
	@ManyToOne
	@JoinColumn(name = "provider_id")
	private User provider;

	// Who learns
	@ManyToOne
	@JoinColumn(name = "learner_id")
	private User learner;

	@ManyToOne
	@JoinColumn(name = "skill_id")
	private Skill skill;

	private LocalDateTime sessionTime;

	@Enumerated(EnumType.STRING)
	private SessionStatus status;
}
