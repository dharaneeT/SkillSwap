package com.skillSwap.Exception;

public class SessionNotFoundException extends ResourceNotFoundException {

	public SessionNotFoundException(Object id) {
		super("Session", "id", id);
	}
}
