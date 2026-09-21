package com.skillSwap.skillswap.Service;

import com.skillSwap.skillswap.Entity.Session;
import com.skillSwap.skillswap.Entity.SessionStatus;
import com.skillSwap.skillswap.Entity.Skill;
import com.skillSwap.skillswap.Entity.User;
import com.skillSwap.skillswap.Repository.SessionRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private SkillService skillService;

	public SessionService(SessionRepository sessionRepository, UserService userService, SkillService skillService) {
		this.sessionRepository = sessionRepository;
		this.userService = userService;
		this.skillService = skillService;
	}

	//Book Session
	public Session bookSession(int providerId, int learnerId, int skillId) {
		User provider = userService.getUserById(providerId);
		User learner = userService.getUserById(learnerId);
		Skill skill = skillService.getSkillById(skillId);

		Session session = new Session();
		session.setProvider(provider);
		session.setLearner(learner);
		session.setSkill(skill);
		session.setSessionTime(LocalDateTime.now());
		session.setStatus(SessionStatus.REQUESTED);

		return sessionRepository.save(session);
	}

	//Accept Session
	public Session acceptSession(int sessionId) {
		Session session = sessionRepository
			.findById(sessionId)
			.orElseThrow(() -> new RuntimeException("Session not found"));

		session.setStatus(SessionStatus.ACCEPTED);
		return sessionRepository.save(session);
	}

	//Complete Session
	public Session completeSession(int sessionId) {
		Session session = sessionRepository
			.findById(sessionId)
			.orElseThrow(() -> new RuntimeException("Session is Not Found"));
		User provider = session.getProvider();
		User learner = session.getLearner();

		session.setStatus(SessionStatus.COMPLETED);
		provider.setCredits(provider.getCredits() + 10);
		learner.setCredits(learner.getCredits() - 10);

		return sessionRepository.save(session);
	}
}
