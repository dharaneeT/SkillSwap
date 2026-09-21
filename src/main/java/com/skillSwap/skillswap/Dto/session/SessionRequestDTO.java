package com.skillSwap.skillswap.Dto.session;

import java.time.LocalDateTime;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionRequestDTO {

	private Integer providerId;
	private Integer learnerId;
	private Integer skillId;
	private LocalDateTime sessionTime;
}
