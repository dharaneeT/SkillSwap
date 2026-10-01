package com.skillSwap.Dto.credit;

import com.skillSwap.Entity.CreditTxType;
import java.time.LocalDateTime;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditTransactionDTO {

	private Integer id;
	private CreditTxType type;
	private Integer amount;
	private Integer balanceAfter;
	private Integer sessionId;
	private String description;
	private LocalDateTime createdAt;
}
