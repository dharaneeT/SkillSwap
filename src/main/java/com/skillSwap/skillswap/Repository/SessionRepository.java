package com.skillSwap.skillswap.Repository;

import com.skillSwap.skillswap.Entity.Session;
import com.skillSwap.skillswap.Entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {
    List<Session> findByStatus(SessionStatus status);

    @Query("SELECT s FROM Session s WHERE s.provider.id = :userId OR s.learner.id = :userId")
    List<Session> findUserSessions(int userId);
}
