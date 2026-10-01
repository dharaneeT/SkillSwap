package com.skillSwap.Service;

import com.skillSwap.Dto.common.PageResponse;
import com.skillSwap.Dto.skill.SkillSearchQuery;
import com.skillSwap.Dto.skill.SkillSearchResultDTO;
import com.skillSwap.Entity.Skill;
import com.skillSwap.Entity.SkillType;
import com.skillSwap.Repository.SkillRepository;
import com.skillSwap.Repository.SkillSpecs;
import com.skillSwap.Repository.UserSkillRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillSearchService {

	private final SkillRepository skillRepository;
	private final UserSkillRepository userSkillRepository;

	public SkillSearchService(SkillRepository skillRepository, UserSkillRepository userSkillRepository) {
		this.skillRepository = skillRepository;
		this.userSkillRepository = userSkillRepository;
	}

	@Cacheable(cacheNames = "skillSearch", key = "#q")
	@Transactional(readOnly = true)
	public PageResponse<SkillSearchResultDTO> search(SkillSearchQuery q) {
		Specification<Skill> spec = (root, query, cb) -> cb.conjunction();
		if (q.search() != null) spec = spec.and(SkillSpecs.nameContains(q.search()));
		if (q.type() != null) spec = spec.and(SkillSpecs.hasUsersOfType(q.type()));
		if (q.minRating() != null) spec = spec.and(SkillSpecs.offeredByProviderRatedAtLeast(q.minRating()));

		Sort.Direction dir = q.asc() ? Sort.Direction.ASC : Sort.Direction.DESC;
		Sort sort = Sort.by(new Sort.Order(dir, q.sortField()));
		if (!q.sortField().equals("id")) sort = sort.and(Sort.by("id")); // stable paging on ties

		Page<Skill> page = skillRepository.findAll(spec, PageRequest.of(q.page(), q.size(), sort));

		// one grouped query for the whole page (not one per skill)
		Map<Integer, long[]> counts = new HashMap<>();
		List<Integer> ids = page.getContent().stream().map(Skill::getId).toList();
		if (!ids.isEmpty()) {
			for (Object[] row : userSkillRepository.countBySkillIds(ids)) {
				long[] c = counts.computeIfAbsent((Integer) row[0], k -> new long[2]);
				c[row[1] == SkillType.OFFERED ? 0 : 1] = (Long) row[2];
			}
		}

		return PageResponse.of(
			page.map(s -> {
				long[] c = counts.getOrDefault(s.getId(), new long[2]);
				return new SkillSearchResultDTO(s.getId(), s.getName(), c[0], c[1]);
			})
		);
	}
}
