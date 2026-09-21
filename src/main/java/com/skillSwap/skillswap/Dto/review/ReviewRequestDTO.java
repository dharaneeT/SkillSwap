package com.skillSwap.skillswap.Dto.review;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequestDTO {

	private Integer sessionId;
	private Integer rating;
	private String comment;
}
