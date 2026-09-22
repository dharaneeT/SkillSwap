package com.skillSwap.skillswap.Exception;

import com.skillSwap.skillswap.Dto.errorResponse.ErrorResponseDTO;
import java.time.LocalDateTime;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

	//VALIDATION HANDLER
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
		String errorMessage = ex
			.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(err -> err.getField() + " : " + err.getDefaultMessage())
			.findFirst()
			.orElse("Validation error");

		ErrorResponseDTO error = new ErrorResponseDTO(
			false,
			errorMessage,
			HttpStatus.BAD_REQUEST.value(),
			LocalDateTime.now()
		);

		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	//ALL NOT FOUND
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException ex) {
		ErrorResponseDTO error = new ErrorResponseDTO(
			false,
			ex.getMessage(),
			HttpStatus.NOT_FOUND.value(),
			LocalDateTime.now()
		);

		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	// CREDIT ERROR
	@ExceptionHandler(CreditException.class)
	public ResponseEntity<ErrorResponseDTO> handleCredit(CreditException ex) {
		ErrorResponseDTO error = new ErrorResponseDTO(
			false,
			ex.getMessage(),
			HttpStatus.BAD_REQUEST.value(),
			LocalDateTime.now()
		);

		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	// USER SKILL ERROR
	@ExceptionHandler(UserSkillException.class)
	public ResponseEntity<ErrorResponseDTO> handleUserSkill(UserSkillException ex) {
		ErrorResponseDTO error = new ErrorResponseDTO(
			false,
			ex.getMessage(),
			HttpStatus.BAD_REQUEST.value(),
			LocalDateTime.now()
		);

		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	// BAD REQUEST
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponseDTO> handleBadRequest(IllegalArgumentException ex) {
		ErrorResponseDTO error = new ErrorResponseDTO(
			false,
			ex.getMessage(),
			HttpStatus.BAD_REQUEST.value(),
			LocalDateTime.now()
		);

		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	// FALLBACK
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDTO> handleGlobal(Exception ex) {
		ErrorResponseDTO error = new ErrorResponseDTO(
			false,
			ex.getMessage(),
			HttpStatus.INTERNAL_SERVER_ERROR.value(),
			LocalDateTime.now()
		);

		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	//    // VALIDATION ERROR
	//    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
	//    public ResponseEntity<ErrorResponseDTO> handleValidation(Exception ex) {
	//
	//        ErrorResponseDTO error = new ErrorResponseDTO(
	//                false,
	//                "Validation failed",
	//                HttpStatus.BAD_REQUEST.value(),
	//                LocalDateTime.now()
	//        );
	//
	//        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	//    }
}
