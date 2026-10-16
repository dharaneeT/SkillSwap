package com.skillSwap.Entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "study_room_note")
@Getter
@Setter
@NoArgsConstructor
public class StudyRoomNote {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "room_id", unique = true)
	private StudyRoom room;

	@Column(nullable = false, columnDefinition = "MEDIUMTEXT")
	private String content = "";

	/** Goes up by one on every successful save. Used to detect conflicting edits. */
	@Column(nullable = false)
	private long revision = 0;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by")
	private User updatedBy;

	private Instant updatedAt;
}
