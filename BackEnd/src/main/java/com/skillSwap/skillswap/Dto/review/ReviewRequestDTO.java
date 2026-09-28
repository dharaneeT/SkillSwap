package com.skillSwap.skillswap.Dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequestDTO {

	@Schema(example = "1")
	private Integer sessionId;

	@Schema(example = "5")
	private Integer rating;

	@Schema(example = "VERY GOOD CLASS")
	private String comment;
}
