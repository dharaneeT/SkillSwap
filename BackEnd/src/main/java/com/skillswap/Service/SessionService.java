package com.skillSwap.Service;

import com.skillSwap.Dto.session.SessionActionDTO;
import com.skillSwap.Dto.session.SessionRequestDTO;
import com.skillSwap.Dto.session.SessionResponseDTO;
import com.skillSwap.Entity.Session;
import com.skillSwap.Entity.SessionStatus;
import com.skillSwap.Entity.Skill;
import com.skillSwap.Entity.User;
import com.skillSwap.Repository.SessionRepository;
import com.skillSwap.Security.CurrentUserService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

	private final SessionRepository sessionRepository;
	private final UserService userService;
	private final SkillService skillService;
	private final ModelMapper modelMapper;
	private final CurrentUserService currentUserService;

	public SessionService(
		SessionRepository sessionRepository,
		ModelMapper modelMapper,
		SkillService skillService,
		UserService userService,
		CurrentUserService currentUserService
	) {
		this.sessionRepository = sessionRepository;
		this.modelMapper = modelMapper;
		this.skillService = skillService;
		this.userService = userService;
		this.currentUserService = currentUserService;
	}

	//  BOOK SESSION (SECURE)
	public SessionResponseDTO bookSession(SessionRequestDTO dto) {
		//  Get logged-in user
		User me = currentUserService.getCurrentUser();

		//  Provider comes from request
		User provider = userService.getUserEntityById(dto.getProviderId());

		//  Prevent booking yourself
		if (provider.getId().equals(me.getId())) {
			throw new RuntimeException("You cannot book your own session");
		}

		//  Skill
		Skill skill = skillService.getSkillEntityById(dto.getSkillId());

		//  Create session
		Session session = new Session();
		session.setProvider(provider);
		session.setLearner(me); //  ALWAYS current user
		session.setSkill(skill);
		session.setStatus(SessionStatus.PENDING);
		session.setSessionTime(dto.getSessionTime());

		Session saved = sessionRepository.save(session);

		//  Response
		SessionResponseDTO responseDTO = new SessionResponseDTO();
		responseDTO.setId(saved.getId());
		responseDTO.setProviderName(provider.getName());
		responseDTO.setLearnerName(me.getName());
		responseDTO.setStatus(saved.getStatus());

		return responseDTO;
	}

	//  UPDATE SESSION (SECURE)
	public SessionResponseDTO updateSession(Integer sessionId, SessionActionDTO dto) {
		//  Logged-in user
		User me = currentUserService.getCurrentUser();

		Session session = sessionRepository
			.findById(sessionId)
			.orElseThrow(() -> new RuntimeException("Session not found"));

		//  Ownership check
		boolean isProvider = session.getProvider().getId().equals(me.getId());
		boolean isLearner = session.getLearner().getId().equals(me.getId());

		if (!isProvider && !isLearner) {
			throw new RuntimeException("Access Denied: Not your session");
		}

		// Business rules
		if (dto.getStatus() == SessionStatus.COMPLETED && !isProvider) {
			throw new RuntimeException("Only provider can mark session as completed");
		}

		if (dto.getStatus() == SessionStatus.CANCELLED && !isLearner) {
			throw new RuntimeException("Only learner can cancel session");
		}

		//  Update
		session.setStatus(dto.getStatus());
		Session updated = sessionRepository.save(session);

		return modelMapper.map(updated, SessionResponseDTO.class);
	}
}
//
//@Service
//public class SessionService {
//
//	@Autowired
//	private SessionRepository sessionRepository;
//
//	@Autowired
//	private UserService userService;
//
//	//	@Autowired
//	private SkillService skillService;
//
//	//	@Autowired
//	private ModelMapper modelMapper;
//
//	public SessionService(
//		SessionRepository sessionRepository,
//		ModelMapper modelMapper,
//		SkillService skillService,
//		UserService userService
//	) {
//		this.sessionRepository = sessionRepository;
//		this.modelMapper = modelMapper;
//		this.skillService = skillService;
//		this.userService = userService;
//	}
//
//	//Book Session
//	@Operation(summary = "BOOK A SESSION")
//	public SessionResponseDTO bookSession(SessionRequestDTO dto) {
//		User provider = userService.getUserEntityById(dto.getProviderId());
//		User learner = userService.getUserEntityById(dto.getLearnerId());
//		Skill skill = skillService.getSkillEntityById(dto.getSkillId());
//		Session session = new Session();
//		session.setProvider(provider);
//		session.setLearner(learner);
//		session.setSkill(skill);
//		session.setStatus(SessionStatus.PENDING);
//		session.setSessionTime(dto.getSessionTime());
//
//		Session saved = sessionRepository.save(session);
//
//		SessionResponseDTO responseDTO = new SessionResponseDTO();
//		responseDTO.setProviderName(provider.getName());
//		responseDTO.setLearnerName(learner.getName());
//		responseDTO.setStatus(saved.getStatus());
//		responseDTO.setId(saved.getId());
//		return responseDTO;
//	}
//
//	//update session
//	@Operation(summary = "UPDATE SESSION")
//	public SessionResponseDTO updateSession(Integer sessionId, SessionActionDTO dto) {
//		Session session = sessionRepository
//			.findById(sessionId)
//			.orElseThrow(() -> new RuntimeException("Session not found"));
//
//		session.setStatus(dto.getStatus());
//		Session updated = sessionRepository.save(session);
//
//		return modelMapper.map(updated, SessionResponseDTO.class);
//	}
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
