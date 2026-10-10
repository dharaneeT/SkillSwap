package com.skillSwap.Repository;

import com.skillSwap.Entity.Notification;
import java.util.List;
import com.skillSwap.Entity.NotificationType;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
	List<Notification> findByUser_IdOrderByIdDesc(Integer userId, Pageable pageable);

	long countByUser_IdAndSeenFalse(Integer userId);

	Optional<Notification> findByIdAndUser_Id(Integer id, Integer userId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE Notification n SET n.seen = true WHERE n.user.id = :userId AND n.seen = false")
	int markAllSeen(@Param("userId") Integer userId);
		Optional<Notification> findFirstByUser_IdAndTypeAndFromUserIdAndSeenFalse(
		Integer userId,
		NotificationType type,
		Integer fromUserId
	);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(
		"UPDATE Notification n SET n.seen = true WHERE n.user.id = :userId AND n.type = com.skillSwap.Entity.NotificationType.CHAT_MESSAGE AND n.fromUserId = :fromUserId AND n.seen = false"
	)
	int markChatSeen(@Param("userId") Integer userId, @Param("fromUserId") Integer fromUserId);
}
