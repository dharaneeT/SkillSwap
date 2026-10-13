package com.skillSwap.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One row per user: when we last heard from them. userId = User.id (no foreign key, so User is untouched). */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPresence {

	@Id
	private Integer userId;

	private Instant lastSeenAt;
}
