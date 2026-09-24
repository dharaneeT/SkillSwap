package com.skillSwap.skillswap.Controllers;

import com.skillSwap.skillswap.Dto.response.ApiResponse;
import com.skillSwap.skillswap.Dto.session.SessionActionDTO;
import com.skillSwap.skillswap.Dto.session.SessionRequestDTO;
import com.skillSwap.skillswap.Dto.session.SessionResponseDTO;
import com.skillSwap.skillswap.Service.SessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/session")
@Tag(name = "Session API", description = "Session management APIs")
public class SessionController {

	@Autowired
	private SessionService sessionService;

	public SessionController(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	//booking session
	@PostMapping("/book-session")
	public ResponseEntity<ApiResponse<SessionResponseDTO>> bookSession(@RequestBody SessionRequestDTO dto) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(new ApiResponse<>(true, "Session Booked", sessionService.bookSession(dto)));
	}

	//Update Session
	@PutMapping("/update/{sessionId}")
	public ResponseEntity<ApiResponse<SessionResponseDTO>> updateSession(
		@PathVariable Integer sessionId,
		@Valid @RequestBody SessionActionDTO dto
	) {
		return ResponseEntity
			.status(HttpStatus.ACCEPTED)
			.body(new ApiResponse<>(true, "Session Updated", sessionService.updateSession(sessionId, dto)));
	}
}
