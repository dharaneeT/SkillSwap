package com.skillSwap.skillswap.Service;

import com.skillSwap.skillswap.Dto.Match.MatchResponseDTO;
import com.skillSwap.skillswap.Entity.SkillType;
import com.skillSwap.skillswap.Entity.UserSkill;
import com.skillSwap.skillswap.Repository.UserSkillRepository;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MatchService {

	private final UserSkillRepository userSkillRepository;

	public MatchService(UserSkillRepository userSkillRepository) {
		this.userSkillRepository = userSkillRepository;
	}

	@Operation(summary = "FIND MATCHES")
	public List<MatchResponseDTO> findMatches(Integer userId) {
		// Get wanted skills
		List<Integer> skillIds = userSkillRepository.findWantedSkillIds(userId);

		// Find users who offer those skills
		List<UserSkill> matches = userSkillRepository.findBySkillIdInAndTypeAndUserIdNot(
			skillIds,
			SkillType.OFFERED,
			userId
		);

		// Convert to DTO
		return matches
			.stream()
			.map(us -> {
				MatchResponseDTO dto = new MatchResponseDTO();
				dto.setUserId(us.getUser().getId());
				dto.setUserName(us.getUser().getName());
				dto.setMatchedSkill(us.getSkill().getName());
				return dto;
			})
			.toList();
	}
	//	public List<MatchResponseDto> findMatches(Integer userID) {
	//		List<UserSkill> matches = userSkillRepository.findMatchingUsers(userID);
	//
	//		return matches
	//			.stream()
	//			.map(userSkill -> {
	//				MatchResponseDto dto = new MatchResponseDto();
	//				dto.setUserId(userSkill.getUser().getId());
	//				dto.setUserName(userSkill.getUser().getName());
	//				dto.setMatchedSkill(userSkill.getSkill().getName());
	//				return dto;
	//			})
	//			.collect(Collectors.toList());
	//	}
}
