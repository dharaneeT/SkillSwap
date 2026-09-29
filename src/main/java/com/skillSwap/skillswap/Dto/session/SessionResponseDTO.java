package com.skillSwap.skillswap.Dto.session;

import com.skillSwap.skillswap.Entity.SessionStatus;
import java.time.LocalDateTime;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionResponseDTO {

	private Integer id;
	private String providerName;
	private String learnerName;
	private String skillName;
	private SessionStatus status;
	private LocalDateTime sessionTime;
}
