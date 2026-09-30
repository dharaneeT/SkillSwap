package com.skillSwap.Dto.user;

import com.skillSwap.Entity.Role;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {

	private Integer id;
	private String name;
	private String email;
	private Integer credits;
	private Role role;
	private Boolean active;
}
