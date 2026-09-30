package com.skillSwap.Repository;

import com.skillSwap.Entity.Message;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
	@Query(
		"""
		SELECT m FROM Message m
		WHERE (m.sender.id = :a AND m.receiver.id = :b)
		   OR (m.sender.id = :b AND m.receiver.id = :a)
		ORDER BY m.sentAt ASC
	"""
	)
	List<Message> findConversation(@Param("a") Integer a, @Param("b") Integer b);

	@Query("SELECT m FROM Message m WHERE m.sender.id = :u OR m.receiver.id = :u ORDER BY m.sentAt DESC")
	List<Message> findAllFor(@Param("u") Integer u);
}
