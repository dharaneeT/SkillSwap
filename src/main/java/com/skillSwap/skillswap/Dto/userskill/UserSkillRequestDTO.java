package com.skillSwap.skillswap.Dto.userskill;

import com.skillSwap.skillswap.Entity.SkillType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSkillRequestDTO {

	@NotNull(message = "USERID should not be Empty")
	@Schema(example = "1")
	private Integer userId;

	@NotNull(message = "SKILLID should not be Empty")
	@Schema(example = "2")
	private Integer skillId;

	@NotNull(message = "TYPE should not be Empty")
	@Schema(example = "OFFERED")
	private SkillType type;
}
