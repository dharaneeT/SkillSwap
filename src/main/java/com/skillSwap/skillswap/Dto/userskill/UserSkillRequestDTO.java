package com.skillSwap.skillswap.Dto.userskill;

import com.skillSwap.skillswap.Entity.SkillType;
import java.util.List;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSkillRequestDTO {

	private Integer userId;
	private Integer skillId;
	private SkillType type;
}
