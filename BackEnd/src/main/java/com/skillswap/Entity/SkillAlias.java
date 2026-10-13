package com.skillSwap.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/** "JS" -> JavaScript. Created automatically when an admin merges a duplicate skill. */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillAlias {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, unique = true)
	private String aliasKey; // normalized: "js"

	private String aliasName; // as typed: "JS"

	@ManyToOne(optional = false)
	@JoinColumn(name = "skill_id")
	@OnDelete(action = OnDeleteAction.CASCADE) // deleting a skill also deletes its aliases
	private Skill skill;
}
