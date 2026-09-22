package com.skillSwap.skillswap.Dto.session;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionRequestDTO {

	@NotNull(message = "PROVIDER_ID should not be Empty")
	private Integer providerId;

	@NotNull(message = "LEARNER_ID should not be Empty")
	private Integer learnerId;

	@NotNull(message = "SKILL_ID should not be Empty")
	private Integer skillId;

	@NotNull(message = "SESSION_TIME should not be Empty")
	private LocalDateTime sessionTime;
}
