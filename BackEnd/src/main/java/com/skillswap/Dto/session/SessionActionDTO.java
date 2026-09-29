package com.skillswap.Dto.session;

import com.skillswap.Entity.SessionStatus;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionActionDTO {

	private SessionStatus status;
}
