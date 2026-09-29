package com.skillswap.Dto.userskill;

import com.skillswap.Entity.SkillType;
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
