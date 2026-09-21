package com.skillSwap.skillswap.Dto.user;

import java.util.List;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {

	private Integer id;
	private String name;
	private Integer credits;

	private List<String> offeredSkills;
	private List<String> wantedSkills;
}
