package com.skillSwap.Entity;

import com.skillSwap.Entity.StudyRoomEnums.*;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Who is ALLOWED in a room (permission). Who is online right now lives in memory (StudyRoomPresenceRegistry). */
@Entity
@Table(
	name = "study_room_member",
	uniqueConstraints = @UniqueConstraint(name = "uk_study_room_member", columnNames = { "room_id", "user_id" }),
	indexes = @Index(name = "idx_study_room_member_user", columnList = "user_id")
)
@Getter
@Setter
@NoArgsConstructor
public class StudyRoomMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "room_id")
	private StudyRoom room;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
	private MemberRole role = MemberRole.MEMBER;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
	private MemberStatus status = MemberStatus.ACTIVE;

	@Column(nullable = false)
	private Instant joinedAt = Instant.now();
}
