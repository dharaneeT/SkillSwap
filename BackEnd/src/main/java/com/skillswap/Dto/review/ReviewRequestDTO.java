package com.skillSwap.Dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequestDTO {

	@NotNull(message = "SESSION_ID should not be Empty")
	@Schema(example = "1")
	private Integer sessionId;

	@NotNull(message = "RATING should not be Empty")
	@Min(value = 1, message = "Rating must be between 1 and 5")
	@Max(value = 5, message = "Rating must be between 1 and 5")
	@Schema(example = "5")
	private Integer rating;

	@Size(max = 500, message = "Comment too long (max 500)")
	@Schema(example = "VERY GOOD CLASS")
	private String comment;
}
