package com.skillSwap.Entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
	name = "study_room_message",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_study_msg_client",
		columnNames = { "room_id", "sender_id", "client_id" }
	),
	indexes = @Index(name = "idx_study_msg_room", columnList = "room_id, id")
)
@Getter
@Setter
@NoArgsConstructor
public class StudyRoomMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "room_id")
	private StudyRoom room;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_id")
	private User sender;

	@Column(nullable = false, length = 1000)
	private String content;

	/** Random id made by the sender's browser. Makes a retry safe: the same clientId is never stored twice. */
	@Column(name = "client_id", length = 36)
	private String clientId;

	@Column(nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	/** Soft delete used by host moderation. */
	@Column(nullable = false)
	private boolean deleted = false;
}
