package com.skillSwap.skillswap.Dto.userskill;

import com.skillSwap.skillswap.Entity.SkillType;
import java.util.List;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSkillResponseDTO {

	private Integer id;
	private String userName;
	private String skillName;
	private SkillType type;
}
