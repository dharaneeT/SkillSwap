package com.skillSwap.skillswap.Dto.credit;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditResponseDTO {

	private Integer userId;
	private String name;
	private int credits;
}
