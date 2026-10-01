package com.skillSwap.Dto.review;

import java.time.LocalDateTime;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponseDTO {

	private Integer id;
	private Integer sessionId;
	private String name; // reviewer (the learner)
	private int rating;
	private String comment;
	private LocalDateTime createdAt;
}
