package com.skillSwap.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "credit_transactions", indexes = @Index(name = "idx_credit_tx_user", columnList = "user_id, created_at"))
@Getter
@Setter
@NoArgsConstructor
public class CreditTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private CreditTxType type;

	//+ for credit
	//- for debit
	@Column(nullable = false)
	private Integer amount;

	@Column(nullable = false)
	private Integer balanceAfter;

	//loose reference to the session that caused it (nullable)
	private Integer sessionId;

	private String description;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void onCreate() {
		createdAt = LocalDateTime.now();
	}
}
