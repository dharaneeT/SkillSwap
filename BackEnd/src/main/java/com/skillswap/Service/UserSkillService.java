package com.skillSwap.Service;

import com.skillSwap.Dto.userskill.UserSkillRequestDTO;
import com.skillSwap.Dto.userskill.UserSkillResponseDTO;
import com.skillSwap.Entity.Skill;
import com.skillSwap.Entity.User;
import com.skillSwap.Entity.UserSkill;
import com.skillSwap.Exception.UserSkillException;
import com.skillSwap.Repository.UserSkillRepository;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import com.skillSwap.Dto.userskill.UserSkillByNameRequestDTO;
import com.skillSwap.Exception.DuplicateResourceException;
import com.skillSwap.Repository.SkillRepository;
import com.skillSwap.Security.CurrentUserService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserSkillService {

	//	@Autowired
	private final UserSkillRepository userSkillRepository;

	private final UserService userService;
	private final SkillService skillService;
	private final ModelMapper modelMapper;
    private final SkillRepository skillRepository;
    private final CurrentUserService currentUserService;

	public UserSkillService(
            UserSkillRepository userSkillRepository,
            UserService userService,
            SkillService skillService,
            ModelMapper modelMapper, SkillRepository skillRepository, CurrentUserService currentUserService
    ) {
		this.userSkillRepository = userSkillRepository;
		this.userService = userService;
		this.skillService = skillService;
		this.modelMapper = modelMapper;
        this.skillRepository = skillRepository;
        this.currentUserService = currentUserService;
    }

	//ADDING USER_SKILL
    @CacheEvict(cacheNames = "skillSearch", allEntries = true)
	@Operation(summary = "ADDING A USER_SKILL")
	public UserSkillResponseDTO addUserSkill(UserSkillRequestDTO dto) {
		User user = userService.getUserEntityById(dto.getUserId());
		Skill skill = skillService.getSkillEntityById(dto.getSkillId());

		UserSkill userSkill = new UserSkill();
		userSkill.setUser(user);
		userSkill.setSkill(skill);
		userSkill.setType(dto.getType());

		UserSkill saved = userSkillRepository.save(userSkill);

		UserSkillResponseDTO response = new UserSkillResponseDTO();
		response.setId(saved.getId());
		response.setUserName(user.getName());
		response.setSkillName(skill.getName());
		response.setType(saved.getType());

		return response;
	}

    //Adding skill by user
    @Transactional
    @CacheEvict(cacheNames = "skillSearch", allEntries = true)
    public UserSkillResponseDTO addByName(UserSkillByNameRequestDTO dto) {
        User me = currentUserService.getCurrentUser();

        String name = dto.getName().trim().replaceAll("\\s+", " ");
        if (name.isEmpty()) throw new IllegalArgumentException("Skill name should not be empty");

        Skill skill = skillRepository
                .findFirstByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Skill s = new Skill();
                    s.setName(name);
                    return skillRepository.save(s);
                });

        if (userSkillRepository.existsByUser_IdAndSkill_IdAndType(me.getId(), skill.getId(), dto.getType())) {
            throw new DuplicateResourceException("You already added " + skill.getName() + " as " + dto.getType());
        }

        UserSkill us = new UserSkill();
        us.setUser(me);
        us.setSkill(skill);
        us.setType(dto.getType());
        UserSkill saved = userSkillRepository.save(us);

        UserSkillResponseDTO out = new UserSkillResponseDTO();
        out.setId(saved.getId());
        out.setUserName(me.getName());
        out.setSkillName(skill.getName());
        out.setType(saved.getType());
        return out;
    }

	//GET USER SKILL
	@Operation(summary = "GET USER_SKILL")
	public List<UserSkillResponseDTO> getUserSkill() {
		return userSkillRepository
			.findAll()
			.stream()
			.map(us -> {
				UserSkillResponseDTO dto = new UserSkillResponseDTO();
				dto.setId(us.getId());
				dto.setUserName(us.getUser().getName());
				dto.setSkillName(us.getSkill().getName());
				dto.setType(us.getType());
				return dto;
			})
			.collect(Collectors.toList());
	}

	//GET USER_SKILL BY ID
	@Operation(summary = "GET USER_SKILL BY ID")
	public UserSkillResponseDTO getUserSkillById(Integer id) {
		UserSkill us = userSkillRepository
			.findById(id)
			.orElseThrow(() -> new UserSkillException("User_Skill is Not Found"));
		return modelMapper.map(us, UserSkillResponseDTO.class);
	}
}
