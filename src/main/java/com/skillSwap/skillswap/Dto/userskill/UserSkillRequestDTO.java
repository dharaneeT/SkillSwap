package com.skillSwap.skillswap.Dto.userskill;

import com.skillSwap.skillswap.Entity.SkillType;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSkillRequestDTO {

	@NotNull(message = "USERID should not be Empty")
	private Integer userId;

	@NotNull(message = "SKILLID should not be Empty")
	private Integer skillId;

	@NotNull(message = "TYPE should not be Empty")
	private SkillType type;
}
