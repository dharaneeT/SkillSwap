package com.skillSwap.skillswap.Dto.user;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {

	private Integer id;
	private String name;
	private String email;
	private Integer credits;
}
