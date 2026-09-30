package com.skillSwap.skillswap.Exception;

public class UserNotFoundException extends ResourceNotFoundException {

	public UserNotFoundException(Object id) {
		super("User", "id", id);
	}
}
