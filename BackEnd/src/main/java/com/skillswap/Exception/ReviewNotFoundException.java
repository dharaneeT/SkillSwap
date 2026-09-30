package com.skillSwap.Exception;

public class ReviewNotFoundException extends ResourceNotFoundException {

	public ReviewNotFoundException(Object id) {
		super("Review", "id", id);
	}
}
