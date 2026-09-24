package com.skillSwap.skillswap.Repository;

import com.skillSwap.skillswap.Entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
	Optional<User> findByName(String name);

	Optional<User> findByEmail(String email);
}
