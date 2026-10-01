package com.skillSwap.Repository;

import com.skillSwap.Entity.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
	Optional<User> findByName(String name);

	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);

	@Lock(LockModeType.PESSIMISTIC_WRITE) // used by SessionService to serialize accepts
	@Query("SELECT u FROM User u WHERE u.id = :id")
	Optional<User> findByIdForUpdate(@Param("id") Integer id);

	@Query("SELECT u.credits FROM User u WHERE u.id = :id")
	Integer findCredits(@Param("id") Integer id);

	// atomic: returns 0 rows if the user doesn't exist OR the balance would go negative
	@Modifying
	@Query(
		"UPDATE User u SET u.credits = COALESCE(u.credits, 0) + :delta " +
		"WHERE u.id = :id AND COALESCE(u.credits, 0) + :delta >= 0"
	)
	int applyCreditDelta(@Param("id") Integer id, @Param("delta") int delta);

    @Modifying
    @Query("UPDATE User u SET u.averageRating = :avg, u.reviewCount = :cnt WHERE u.id = :id")
    int updateRating(@Param("id") Integer id, @Param("avg") Double avg, @Param("cnt") Integer cnt);
}
