package com.skillSwap.Repository;

import com.skillSwap.Entity.CreditTransaction;
import com.skillSwap.Entity.CreditTxType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Integer> {
	List<CreditTransaction> findByUser_IdOrderByCreatedAtDescIdDesc(Integer userId);

	// total credits a user earned from the given kinds of transaction since a moment (used for the study-room daily cap)
	@Query(
		"select coalesce(sum(t.amount), 0) from CreditTransaction t where t.user.id = :userId and t.type in :types and t.createdAt >= :since"
	)
	long sumSince(
		@Param("userId") Integer userId,
		@Param("types") Collection<CreditTxType> types,
		@Param("since") LocalDateTime since
	);
}
