package com.skillSwap.skillswap.Controllers;

import com.skillSwap.skillswap.Dto.response.ApiResponse;
import com.skillSwap.skillswap.Dto.session.SessionActionDTO;
import com.skillSwap.skillswap.Dto.session.SessionRequestDTO;
import com.skillSwap.skillswap.Dto.session.SessionResponseDTO;
import com.skillSwap.skillswap.Service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/session")
public class SessionController {

	@Autowired
	private SessionService sessionService;

	public SessionController(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	//booking session
	@PostMapping("/book-session")
	public ApiResponse<SessionResponseDTO> bookSession(@RequestBody SessionRequestDTO dto) {
		return new ApiResponse<>(true, "Session Booked", sessionService.bookSession(dto));
	}

	//Update Session
	@PutMapping("/update/{sessionId}")
	public ApiResponse<SessionResponseDTO> updateSession(
		@PathVariable Integer sessionId,
		@RequestBody SessionActionDTO dto
	) {
		return new ApiResponse<>(true, "Session Updated", sessionService.updateSession(sessionId, dto));
	}
}
