package com.skillSwap.Dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {

	@NotBlank(message = "NAME should not be Empty")
	@Schema(example = "Dharanee")
	private String name;

	@NotBlank(message = "EMAIL should not be Empty")
	@Email(message = "Invalid Email Format")
	@Schema(example = "Dharanee@gmail.com")
	private String email;

	private String password;
}
