package com.skillSwap.skillswap.Dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillRequestDTO {

	@NotBlank(message = "NAME should not be Empty")
	@Schema(example = "JAVA")
	private String name;
}
