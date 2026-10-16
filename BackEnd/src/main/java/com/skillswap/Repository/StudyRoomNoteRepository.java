package com.skillSwap.Repository;

import com.skillSwap.Entity.StudyRoomNote;
import com.skillSwap.Entity.User;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyRoomNoteRepository extends JpaRepository<StudyRoomNote, Integer> {
	@Query("select n from StudyRoomNote n left join fetch n.updatedBy where n.room.id = :roomId")
	Optional<StudyRoomNote> findByRoomId(@Param("roomId") Integer roomId);

	/** Compare-and-set: only saves if nobody else saved since the version the client started from. Returns 0 on conflict. */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(
		"update StudyRoomNote n set n.content = :content, n.revision = n.revision + 1, n.updatedBy = :by, n.updatedAt = :now " +
		"where n.room.id = :roomId and n.revision = :base"
	)
	int saveIfVersionMatches(
		@Param("roomId") Integer roomId,
		@Param("content") String content,
		@Param("by") User by,
		@Param("now") Instant now,
		@Param("base") long base
	);
}
