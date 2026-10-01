package com.skillSwap.Controllers;

import com.skillSwap.Dto.response.ApiResponse;
import com.skillSwap.Dto.session.SessionActionDTO;
import com.skillSwap.Dto.session.SessionRequestDTO;
import com.skillSwap.Dto.session.SessionResponseDTO;
import com.skillSwap.Service.SessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/session")
@Tag(name = "Session API", description = "Session booking APIs")
public class SessionController {

	private final SessionService sessionService;

	public SessionController(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	@PostMapping("/book-session")
	public ResponseEntity<ApiResponse<SessionResponseDTO>> bookSession(@Valid @RequestBody SessionRequestDTO dto) {
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(new ApiResponse<>(true, "Session booked", sessionService.bookSession(dto)));
	}

	@PutMapping("/{sessionId}/accept")
	public ResponseEntity<ApiResponse<SessionResponseDTO>> accept(@PathVariable Integer sessionId) {
		return ok("Session accepted", sessionService.accept(sessionId));
	}

	@PutMapping("/{sessionId}/reject")
	public ResponseEntity<ApiResponse<SessionResponseDTO>> reject(@PathVariable Integer sessionId) {
		return ok("Session rejected", sessionService.reject(sessionId));
	}

	@PutMapping("/{sessionId}/cancel")
	public ResponseEntity<ApiResponse<SessionResponseDTO>> cancel(@PathVariable Integer sessionId) {
		return ok("Session cancelled", sessionService.cancel(sessionId));
	}

	@PutMapping("/{sessionId}/complete")
	public ResponseEntity<ApiResponse<SessionResponseDTO>> complete(@PathVariable Integer sessionId) {
		return ok("Session completed", sessionService.complete(sessionId));
	}

	// kept for the existing frontend: PUT /update/{id} with {"status": "..."}
	@PutMapping("/update/{sessionId}")
	public ResponseEntity<ApiResponse<SessionResponseDTO>> update(
		@PathVariable Integer sessionId,
		@Valid @RequestBody SessionActionDTO dto
	) {
		return switch (dto.getStatus()) {
			case ACCEPTED -> ok("Session accepted", sessionService.accept(sessionId));
			case REJECTED -> ok("Session rejected", sessionService.reject(sessionId));
			case CANCELLED -> ok("Session cancelled", sessionService.cancel(sessionId));
			case COMPLETED -> ok("Session completed", sessionService.complete(sessionId));
			default -> throw new IllegalArgumentException("Status " + dto.getStatus() + " cannot be set manually");
		};
	}

	@GetMapping("/my")
	public ResponseEntity<ApiResponse<List<SessionResponseDTO>>> mySessions() {
		return ok("My sessions", sessionService.mySessions());
	}

	private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
		return ResponseEntity.ok(new ApiResponse<>(true, message, data));
	}
}
