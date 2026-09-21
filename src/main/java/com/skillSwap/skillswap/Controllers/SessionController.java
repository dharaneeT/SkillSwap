package com.skillSwap.skillswap.Controllers;

import com.skillSwap.skillswap.Entity.Session;
import com.skillSwap.skillswap.Service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/session")
public class SessionController {

	@Autowired
	private SessionService sessionService;

	public SessionController(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	//booking session
	@PostMapping("/book-session")
	public Session bookSession(
		@RequestParam Integer providerId,
		@RequestParam Integer learnerId,
		@RequestParam Integer skillId
	) {
		return sessionService.bookSession(providerId, learnerId, skillId);
	}

	//accepting session
	@PutMapping("/accept/{sessionId}")
	public Session acceptSession(@PathVariable Integer sessionId) {
		return sessionService.acceptSession(sessionId);
	}

	//complete session
	@PutMapping("/complete/{sessionId}")
	public Session completeSession(@PathVariable Integer sessionId) {
		return sessionService.completeSession(sessionId);
	}
}
