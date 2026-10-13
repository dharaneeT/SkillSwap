package com.skillSwap.Dto.session;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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


	@NotNull(message = "SKILL_ID should not be Empty")
	@Schema(example = "1")
	private Integer skillId;

	@NotNull(message = "SESSION_TIME should not be Empty")
	@Schema(example = "2026-09-25T11:00:00")
	private LocalDateTime sessionTime;

    // NEW (optional in the request, defaults to 30)
    @Min(value = 15, message = "DURATION must be at least 15 minutes")
    @Max(value = 180, message = "DURATION must be at most 180 minutes")
    @Schema(example = "30")
    private Integer durationMinutes;
}
