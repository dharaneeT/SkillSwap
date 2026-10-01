package com.skillSwap.Repository;

import com.skillSwap.Entity.CreditTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Integer> {
    List<CreditTransaction> findByUser_IdOrderByCreatedAtDescIdDesc(Integer userId);
}