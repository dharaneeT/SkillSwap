package com.skillswap.Exception;

public class SessionNotFoundException extends ResourceNotFoundException {

	public SessionNotFoundException(Object id) {
		super("Session", "id", id);
	}
}
