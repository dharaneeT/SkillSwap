
package com.skillSwap.Service;

import com.skillSwap.Dto.userskill.UserSkillByNameRequestDTO;
import com.skillSwap.Dto.userskill.UserSkillRequestDTO;
import com.skillSwap.Dto.userskill.UserSkillResponseDTO;
import com.skillSwap.Entity.Skill;
import com.skillSwap.Entity.User;
import com.skillSwap.Entity.UserSkill;
import com.skillSwap.Exception.DuplicateResourceException;
import com.skillSwap.Exception.UserSkillException;
import com.skillSwap.Repository.SkillRepository;
import com.skillSwap.Repository.UserSkillRepository;
import com.skillSwap.Security.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserSkillService {

    private final UserSkillRepository userSkillRepository;
    private final UserService userService;
    private final SkillService skillService;
    private final ModelMapper modelMapper;
    private final SkillRepository skillRepository;
    private final CurrentUserService currentUserService;
    private final SkillAliasService skillAliasService;

    public UserSkillService(
            UserSkillRepository userSkillRepository,
            UserService userService,
            SkillService skillService,
            ModelMapper modelMapper,
            SkillRepository skillRepository,
            CurrentUserService currentUserService,
            SkillAliasService skillAliasService
    ) {
        this.userSkillRepository = userSkillRepository;
        this.userService = userService;
        this.skillService = skillService;
        this.modelMapper = modelMapper;
        this.skillRepository = skillRepository;
        this.currentUserService = currentUserService;
        this.skillAliasService = skillAliasService;
    }

    // ADD USER SKILL BY ID
    @Transactional
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

    // ADD SKILL BY NAME
    // Resolves aliases before creating a new skill.
    @Transactional
    @CacheEvict(cacheNames = "skillSearch", allEntries = true)
    public UserSkillResponseDTO addByName(UserSkillByNameRequestDTO dto) {

        // 1. Get the currently logged-in user.
        User me = currentUserService.getCurrentUser();

        // 2. Validate and normalize the submitted skill name.
        if (dto == null || dto.getName() == null) {
            throw new IllegalArgumentException(
                    "Skill name should not be empty"
            );
        }

        String name = dto.getName()
                .trim()
                .replaceAll("\\s+", " ");

        if (name.isEmpty()) {
            throw new IllegalArgumentException(
                    "Skill name should not be empty"
            );
        }

        if (dto.getType() == null) {
            throw new IllegalArgumentException(
                    "Skill type should not be empty"
            );
        }

        // 3. Find the canonical skill:
        //    a. Search by the actual skill name.
        //    b. If not found, resolve the submitted name as an alias.
        //    c. If neither exists, create a new skill.
        Skill skill = skillRepository
                .findFirstByNameIgnoreCase(name)
                .or(() -> skillAliasService.resolve(name))
                .orElseGet(() -> {
                    Skill newSkill = new Skill();
                    newSkill.setName(name);
                    return skillRepository.save(newSkill);
                });

        // 4. Prevent duplicate associations for the same user,
        //    canonical skill, and skill type.
        boolean alreadyExists =
                userSkillRepository.existsByUser_IdAndSkill_IdAndType(
                        me.getId(),
                        skill.getId(),
                        dto.getType()
                );

        if (alreadyExists) {
            throw new DuplicateResourceException(
                    "You already added "
                            + skill.getName()
                            + " as "
                            + dto.getType()
            );
        }

        // 5. Create the user-skill association.
        UserSkill userSkill = new UserSkill();
        userSkill.setUser(me);
        userSkill.setSkill(skill);
        userSkill.setType(dto.getType());

        UserSkill saved = userSkillRepository.save(userSkill);

        // 6. Return the response.
        UserSkillResponseDTO response = new UserSkillResponseDTO();
        response.setId(saved.getId());
        response.setUserName(me.getName());
        response.setSkillName(skill.getName());
        response.setType(saved.getType());

        return response;
    }

    // GET ALL USER SKILLS
    @Operation(summary = "GET USER_SKILL")
    public List<UserSkillResponseDTO> getUserSkill() {

        return userSkillRepository.findAll()
                .stream()
                .map(userSkill -> {
                    UserSkillResponseDTO response =
                            new UserSkillResponseDTO();

                    response.setId(userSkill.getId());
                    response.setUserName(
                            userSkill.getUser().getName()
                    );
                    response.setSkillName(
                            userSkill.getSkill().getName()
                    );
                    response.setType(userSkill.getType());

                    return response;
                })
                .collect(Collectors.toList());
    }

    // GET USER SKILL BY ID
    @Operation(summary = "GET USER_SKILL BY ID")
    public UserSkillResponseDTO getUserSkillById(Integer id) {

        UserSkill userSkill = userSkillRepository.findById(id)
                .orElseThrow(() ->
                        new UserSkillException(
                                "User_Skill is Not Found"
                        )
                );

        return modelMapper.map(
                userSkill,
                UserSkillResponseDTO.class
        );
    }
}
