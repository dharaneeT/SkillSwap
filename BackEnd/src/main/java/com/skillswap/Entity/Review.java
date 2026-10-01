package com.skillSwap.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer rating;

	@Column(length = 500)
	private String comment;

	// unique = one review per session, enforced by the database too
	@OneToOne
	@JoinColumn(name = "session_id", unique = true)
	private Session session;

	private LocalDateTime createdAt;
}
