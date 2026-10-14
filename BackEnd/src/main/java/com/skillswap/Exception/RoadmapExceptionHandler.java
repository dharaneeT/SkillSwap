package com.skillSwap.Exception;

import com.skillSwap.Controllers.RoadmapController;
import com.skillSwap.Dto.errorResponse.ErrorResponseDTO;
import java.time.LocalDateTime;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Only applies to RoadmapController and runs before GlobalExceptionHandler's catch-all.
@RestControllerAdvice(assignableTypes = RoadmapController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RoadmapExceptionHandler {

	@ExceptionHandler(RoadmapAiException.class)
	public ResponseEntity<ErrorResponseDTO> handleAi(RoadmapAiException ex) {
		return ResponseEntity
			.status(ex.getStatus())
			.body(new ErrorResponseDTO(false, ex.getMessage(), ex.getStatus().value(), LocalDateTime.now()));
	}
}
