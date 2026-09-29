package com.skillSwap.skillswap.Repository;

import com.skillSwap.skillswap.Entity.Credit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditRepository extends JpaRepository<Credit,Integer> {}
