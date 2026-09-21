package com.skillSwap.skillswap.Dto.review;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponseDTO {

	private Integer id;
	private String name;
	private int rating;
	private String comment;
}
