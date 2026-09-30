package com.skillSwap.Dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequestDTO {

	@NotNull
	@Schema(example = "1")
	private Integer sessionId;

	@Min(1)
	@Max(5)
	@Schema(example = "5")
	private Integer rating;

	@Schema(example = "VERY GOOD CLASS")
	private String comment;
}
