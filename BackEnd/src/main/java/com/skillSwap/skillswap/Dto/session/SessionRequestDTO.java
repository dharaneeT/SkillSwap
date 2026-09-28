package com.skillSwap.skillswap.Dto.session;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionRequestDTO {

	@NotNull(message = "PROVIDER_ID should not be Empty")
	@Schema(example = "1")
	private Integer providerId;

	@NotNull(message = "LEARNER_ID should not be Empty")
	@Schema(example = "2")
	private Integer learnerId;

	@NotNull(message = "SKILL_ID should not be Empty")
	@Schema(example = "1")
	private Integer skillId;

	@NotNull(message = "SESSION_TIME should not be Empty")
	@Schema(example = "2026-09-25T11:00:00")
	private LocalDateTime sessionTime;
}
