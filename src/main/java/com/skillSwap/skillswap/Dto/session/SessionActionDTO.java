package com.skillSwap.skillswap.Dto.session;

import com.skillSwap.skillswap.Entity.SessionStatus;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionActionDTO {

	private SessionStatus status;
}
