package com.skillSwap.skillswap.Dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {

	@NotBlank(message = "NAME should not be Empty")
	private String name;

	@NotBlank(message = "EMAIL should not be Empty")
	@Email(message = "Invalid Email Format")
	private String email;
}
