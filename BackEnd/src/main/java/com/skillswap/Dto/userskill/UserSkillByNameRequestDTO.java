package com.skillSwap.Dto.userskill;

import com.skillSwap.Entity.SkillType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSkillByNameRequestDTO {

	@NotBlank(message = "Skill name should not be empty")
	@Size(max = 50, message = "Skill name too long (max 50)")
	@Schema(example = "Guitar")
	private String name;

	@NotNull(message = "TYPE should not be empty")
	@Schema(example = "OFFERED")
	private SkillType type;
}
