package com.skillSwap.Exception;

import com.skillSwap.Dto.errorResponse.ErrorResponseDTO;
import java.time.LocalDateTime;
import org.springframework.http.*;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String message) {
		return new ResponseEntity<>(new ErrorResponseDTO(false, message, status.value(), LocalDateTime.now()), status);
	}

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class) // race / DB unique
    public ResponseEntity<ErrorResponseDTO> handleIntegrity(Exception ex) {
        return build(HttpStatus.CONFLICT, "Operation violates a data constraint (e.g. email already exists)");
    }

    @ExceptionHandler(IllegalArgumentException.class)                  // AdminService throws this -> 400
    public ResponseEntity<ErrorResponseDTO> handleIllegalArg(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)                                 // fallback, keep LAST
    public ResponseEntity<ErrorResponseDTO> handleAny(Exception ex) {
        ex.printStackTrace(); // or a logger
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
    }
	// wrong password / unknown email  → 401 (generic message: don't reveal which one was wrong)
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErrorResponseDTO> handleAuth(AuthenticationException ex) {
		return build(HttpStatus.UNAUTHORIZED, "Invalid email or password");
	}

	// deactivated account (DisabledException is an AuthenticationException, so it must be more specific → Spring picks the closest type)
	@ExceptionHandler(DisabledException.class)
	public ResponseEntity<ErrorResponseDTO> handleDisabled(DisabledException ex) {
		return build(HttpStatus.FORBIDDEN, "Account is deactivated");
	}

	// @PreAuthorize failures and manual ownership checks → 403 (WITHOUT this, your Exception.class fallback returns 500)
	@ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
	public ResponseEntity<ErrorResponseDTO> handleDenied(org.springframework.security.access.AccessDeniedException ex) {
		return build(HttpStatus.FORBIDDEN, "You do not have permission to do this");
	}

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ErrorResponseDTO> handleDuplicate(DuplicateResourceException ex) {
		return build(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
		String msg = ex
			.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(e -> e.getDefaultMessage())
			.findFirst()
			.orElse("Validation failed");
		return build(HttpStatus.BAD_REQUEST, msg);
	}

	//    // duplicates / time conflicts / illegal state changes → 409  (classes added in Steps 1 and 4)
	//    @ExceptionHandler({DuplicateResourceException.class, SessionConflictException.class,
	//            InvalidSessionStateException.class,
	//            org.springframework.dao.DataIntegrityViolationException.class})
	//    public ResponseEntity<ErrorResponseDTO> handleConflict(RuntimeException ex) {
	//        return build(HttpStatus.CONFLICT, ex instanceof org.springframework.dao.DataIntegrityViolationException
	//                ? "Operation violates a data constraint (item may be in use)" : ex.getMessage());
	//    }

	// bad enum / bad JSON / bad path param → 400 instead of 500
	@ExceptionHandler(
		{
			org.springframework.http.converter.HttpMessageNotReadableException.class,
			org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
		}
	)
	public ResponseEntity<ErrorResponseDTO> handleBadInput(Exception ex) {
		return build(HttpStatus.BAD_REQUEST, "Malformed request body or parameter");
	}

	//	//VALIDATION HANDLER
	//	@ExceptionHandler(MethodArgumentNotValidException.class)
	//	public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
	//		String errorMessage = ex
	//			.getBindingResult()
	//			.getFieldErrors()
	//			.stream()
	//			.map(err -> err.getField() + " : " + err.getDefaultMessage())
	//			.findFirst()
	//			.orElse("Validation error");
	//
	//		ErrorResponseDTO error = new ErrorResponseDTO(
	//			false,
	//			errorMessage,
	//			HttpStatus.BAD_REQUEST.value(),
	//			LocalDateTime.now()
	//		);
	//
	//		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	//	}

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
	//	// BAD REQUEST
	//	@ExceptionHandler(IllegalArgumentException.class)
	//	public ResponseEntity<ErrorResponseDTO> handleBadRequest(IllegalArgumentException ex) {
	//		ErrorResponseDTO error = new ErrorResponseDTO(
	//			false,
	//			ex.getMessage(),
	//			HttpStatus.BAD_REQUEST.value(),
	//			LocalDateTime.now()
	//		);
	//
	//		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	//	}
	//
	//	// FALLBACK
	//	@ExceptionHandler(Exception.class)
	//	public ResponseEntity<ErrorResponseDTO> handleGlobal(Exception ex) {
	//		ErrorResponseDTO error = new ErrorResponseDTO(
	//			false,
	//			ex.getMessage(),
	//			HttpStatus.INTERNAL_SERVER_ERROR.value(),
	//			LocalDateTime.now()
	//		);
	//
	//		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	//	}
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
