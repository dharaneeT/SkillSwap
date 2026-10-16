package com.skillSwap.Exception;

import org.springframework.http.HttpStatus;

/** A study-room error that carries its own HTTP status (409 conflict, 429 too many attempts, ...). */
public class StudyRoomException extends RuntimeException {

	private final HttpStatus status;

	public StudyRoomException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
