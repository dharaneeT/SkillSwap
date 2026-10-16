package com.skillSwap.Exception;

import com.skillSwap.Controllers.StudyRoomActivityController;
import com.skillSwap.Controllers.StudyRoomController;
import com.skillSwap.Dto.errorResponse.ErrorResponseDTO;
import java.time.LocalDateTime;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Same idea as RoadmapExceptionHandler: only study-room controllers, runs before the catch-all.
@RestControllerAdvice(assignableTypes = { StudyRoomController.class, StudyRoomActivityController.class })
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StudyRoomExceptionHandler {

	@ExceptionHandler(StudyRoomException.class)
	public ResponseEntity<ErrorResponseDTO> handle(StudyRoomException ex) {
		return ResponseEntity
			.status(ex.getStatus())
			.body(new ErrorResponseDTO(false, ex.getMessage(), ex.getStatus().value(), LocalDateTime.now()));
	}
}
