package com.skillSwap.skillswap.Exception;

public class ReviewNotFoundException extends ResourceNotFoundException {

	public ReviewNotFoundException(Object id) {
		super("Review", "id", id);
	}
}
