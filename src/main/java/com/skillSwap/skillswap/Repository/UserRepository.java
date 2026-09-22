package com.skillSwap.skillswap.Repository;

import com.skillSwap.skillswap.Entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
	@Query("SELECT u FROM User u WHERE u.name = :name")
	List<User> findByName(String name);

	@Query("SELECT u FROM User u WHERE u.credits >= :credits")
	List<User> findUsersWithCredits(Integer credits);
}
