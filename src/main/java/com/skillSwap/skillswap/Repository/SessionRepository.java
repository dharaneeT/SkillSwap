package com.skillSwap.skillswap.Repository;

import com.skillSwap.skillswap.Entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {}
