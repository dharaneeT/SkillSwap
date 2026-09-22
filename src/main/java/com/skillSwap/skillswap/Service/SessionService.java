package com.skillSwap.skillswap.Service;

import com.skillSwap.skillswap.Dto.session.SessionActionDTO;
import com.skillSwap.skillswap.Dto.session.SessionRequestDTO;
import com.skillSwap.skillswap.Dto.session.SessionResponseDTO;
import com.skillSwap.skillswap.Entity.Session;
import com.skillSwap.skillswap.Entity.SessionStatus;
import com.skillSwap.skillswap.Entity.Skill;
import com.skillSwap.skillswap.Entity.User;
import com.skillSwap.skillswap.Repository.SessionRepository;
import org.modelmapper.ModelMapper;
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

	@Autowired
	private ModelMapper modelMapper;

	public SessionService(
		SessionRepository sessionRepository,
		ModelMapper modelMapper,
		SkillService skillService,
		UserService userService
	) {
		this.sessionRepository = sessionRepository;
		this.modelMapper = modelMapper;
		this.skillService = skillService;
		this.userService = userService;
	}

	//Book Session
	public SessionResponseDTO bookSession(SessionRequestDTO dto) {
		User provider = userService.getUserEntityById(dto.getProviderId());
		User learner = userService.getUserEntityById(dto.getLearnerId());
		Skill skill = skillService.getSkillEntityById(dto.getSkillId());
		Session session = new Session();
		session.setProvider(provider);
		session.setLearner(learner);
		session.setSkill(skill);
		session.setStatus(SessionStatus.PENDING);
		session.setSessionTime(dto.getSessionTime());

		Session saved = sessionRepository.save(session);

		SessionResponseDTO responseDTO = new SessionResponseDTO();
		responseDTO.setProviderName(provider.getName());
		responseDTO.setLearnerName(learner.getName());
		responseDTO.setStatus(saved.getStatus());
		responseDTO.setId(saved.getId());
		return responseDTO;
	}

	//update session
	public SessionResponseDTO updateSession(Integer sessionId, SessionActionDTO dto) {
		Session session = sessionRepository
			.findById(sessionId)
			.orElseThrow(() -> new RuntimeException("Session not found"));

		session.setStatus(dto.getStatus());
		Session updated = sessionRepository.save(session);

		return modelMapper.map(updated, SessionResponseDTO.class);
	}
	//Book Session
	//	public Session bookSession(Integer providerId, Integer learnerId, Integer skillId) {
	//		User provider = userService.getUserById(providerId);
	//		User learner = userService.getUserById(learnerId);
	//		Skill skill = skillService.getSkillById(skillId);
	//
	//		Session session = new Session();
	//		session.setProvider(provider);
	//		session.setLearner(learner);
	//		session.setSkill(skill);
	//		session.setSessionTime(LocalDateTime.now());
	//		session.setStatus(SessionStatus.REQUESTED);
	//
	//		return sessionRepository.save(session);
	//	}

	//Accept Session
	//	public Session acceptSession(Integer sessionId) {
	//		Session session = sessionRepository
	//			.findById(sessionId)
	//			.orElseThrow(() -> new RuntimeException("Session not found"));
	//
	//		session.setStatus(SessionStatus.ACCEPTED);
	//		return sessionRepository.save(session);
	//	}
	//
	//	//Complete Session
	//	public Session completeSession(Integer sessionId) {
	//		Session session = sessionRepository
	//			.findById(sessionId)
	//			.orElseThrow(() -> new RuntimeException("Session is Not Found"));
	//		User provider = session.getProvider();
	//		User learner = session.getLearner();
	//
	//		session.setStatus(SessionStatus.COMPLETED);
	//		provider.setCredits(provider.getCredits() + 10);
	//		learner.setCredits(learner.getCredits() - 10);
	//
	//		return sessionRepository.save(session);
	//	}
}
