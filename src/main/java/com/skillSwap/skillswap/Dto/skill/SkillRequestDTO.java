package com.skillSwap.skillswap.Dto.skill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillRequestDTO {

	@NotBlank(message = "NAME should not be Empty")
	private String name;
}
