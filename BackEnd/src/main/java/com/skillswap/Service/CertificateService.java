package com.skillSwap.Service;

import com.skillSwap.Dto.certificate.CertificateDTO;
import com.skillSwap.Entity.Session;
import com.skillSwap.Entity.SessionStatus;
import com.skillSwap.Exception.ResourceNotFoundException;
import com.skillSwap.Repository.SessionRepository;
import com.skillSwap.Security.CurrentUserService;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A certificate = N COMPLETED sessions of the same skill, with the same teacher, as the learner.
 * Nothing is stored: it is computed from sessions, so it can never disagree with the data.
 * Code format: SS-<learnerId>-<providerId>-<skillId>. Verifying a code recomputes it.
 */
@Service
public class CertificateService {

	private final SessionRepository sessionRepository;
	private final CurrentUserService currentUserService;
	private final int minSessions;

	public CertificateService(
		SessionRepository sessionRepository,
		CurrentUserService currentUserService,
		@Value("${skillswap.certificate.min-sessions:3}") int minSessions
	) {
		this.sessionRepository = sessionRepository;
		this.currentUserService = currentUserService;
		this.minSessions = minSessions;
	}

	@Transactional(readOnly = true)
	public List<CertificateDTO> myCertificates() {
		return build(currentUserService.getCurrentUser().getId());
	}

	@Transactional(readOnly = true)
	public int countFor(Integer learnerId) {
		return build(learnerId).size();
	}

	@Transactional(readOnly = true)
	public CertificateDTO verify(String code) {
		String[] p = code == null ? new String[0] : code.split("-");
		if (p.length != 4 || !"SS".equals(p[0])) throw new ResourceNotFoundException("Certificate", "code", code);
		Integer learnerId;
		try {
			learnerId = Integer.valueOf(p[1]);
		} catch (NumberFormatException e) {
			throw new ResourceNotFoundException("Certificate", "code", code);
		}
		return build(learnerId)
			.stream()
			.filter(c -> c.code().equals(code))
			.findFirst()
			.orElseThrow(() -> new ResourceNotFoundException("Certificate", "code", code));
	}

	private List<CertificateDTO> build(Integer learnerId) {
		Map<String, List<Session>> groups = sessionRepository
			.findUserSessions(learnerId)
			.stream()
			.filter(s ->
				s.getStatus() == SessionStatus.COMPLETED &&
				s.getLearner() != null &&
				s.getProvider() != null &&
				s.getSkill() != null &&
				learnerId.equals(s.getLearner().getId())
			)
			.collect(Collectors.groupingBy(s -> s.getProvider().getId() + "-" + s.getSkill().getId()));

		List<CertificateDTO> out = new ArrayList<>();
		for (List<Session> g : groups.values()) {
			if (g.size() < minSessions) continue;
			Session first = g.get(0);
			LocalDateTime last = g
				.stream()
				.map(Session::getSessionTime)
				.filter(Objects::nonNull)
				.max(Comparator.naturalOrder())
				.orElse(LocalDateTime.now());
			out.add(
				new CertificateDTO(
					"SS-" + learnerId + "-" + first.getProvider().getId() + "-" + first.getSkill().getId(),
					first.getLearner().getName(),
					first.getProvider().getName(),
					first.getSkill().getName(),
					g.size(),
					last
				)
			);
		}
		out.sort(Comparator.comparing(CertificateDTO::issuedAt).reversed());
		return out;
	}
}
