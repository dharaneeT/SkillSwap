package com.skillSwap.Exception;

import org.springframework.http.HttpStatus;

/** Thrown when AI generation cannot produce a roadmap. The HTTP status tells the UI what happened. */
public class RoadmapAiException extends RuntimeException {

	private final HttpStatus status;
	private final boolean tryNextModel;

	public RoadmapAiException(HttpStatus status, String message, boolean tryNextModel) {
		super(message);
		this.status = status;
		this.tryNextModel = tryNextModel;
	}

	public HttpStatus getStatus() {
		return status;
	}

	/** True if a different model might still succeed (quota, outage, model removed). */
	public boolean isTryNextModel() {
		return tryNextModel;
	}
}
