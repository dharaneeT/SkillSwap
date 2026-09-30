package com.skillSwap.Dto.session;

import com.skillSwap.Entity.SessionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionActionDTO {

	@NotNull
	private SessionStatus status;
}
