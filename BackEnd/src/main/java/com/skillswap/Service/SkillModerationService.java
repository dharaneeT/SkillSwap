package com.skillSwap.Service;

import com.skillSwap.Dto.skill.SkillModerationDTO;
import com.skillSwap.Dto.skill.SkillModerationDTO.Suggestion;
import com.skillSwap.Entity.Session;
import com.skillSwap.Entity.Skill;
import com.skillSwap.Entity.SkillAlias;
import com.skillSwap.Entity.SkillStatus;
import com.skillSwap.Entity.UserSkill;
import com.skillSwap.Exception.ResourceNotFoundException;
import com.skillSwap.Repository.SkillAliasRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillModerationService {

	// extend freely: normalized short form -> normalized full form
	private static final Map<String, String> ABBREVIATIONS = Map.ofEntries(
		Map.entry("js", "javascript"),
		Map.entry("ts", "typescript"),
		Map.entry("py", "python"),
		Map.entry("cpp", "c++"),
		Map.entry("golang", "go"),
		Map.entry("reactjs", "react"),
		Map.entry("nodejs", "node"),
		Map.entry("ml", "machinelearning")
	);

	@PersistenceContext
	private EntityManager em;

	private final SkillAliasRepository aliasRepository;

	public SkillModerationService(SkillAliasRepository aliasRepository) {
		this.aliasRepository = aliasRepository;
	}

	@Transactional(readOnly = true)
	public List<SkillModerationDTO> listPending() {
		List<Skill> all = em.createQuery("SELECT s FROM Skill s ORDER BY s.name", Skill.class).getResultList();
		Map<Integer, Long> usage = usageBySkill();
		List<Skill> approved = all.stream().filter(s -> s.getStatus() != SkillStatus.PENDING).toList();

		return all
			.stream()
			.filter(s -> s.getStatus() == SkillStatus.PENDING)
			.map(p ->
				new SkillModerationDTO(
					p.getId(),
					p.getName(),
					usage.getOrDefault(p.getId(), 0L),
					approved
						.stream()
						.filter(c -> similar(SkillAliasService.key(p.getName()), SkillAliasService.key(c.getName())))
						.sorted(Comparator.comparing((Skill c) -> usage.getOrDefault(c.getId(), 0L)).reversed())
						.limit(3)
						.map(c -> new Suggestion(c.getId(), c.getName()))
						.collect(Collectors.toList())
				)
			)
			.toList();
	}

	@Transactional
	public void approve(Integer id) {
		find(id).setStatus(SkillStatus.APPROVED);
	}

	/** Moves everything from source to target, remembers source's name as an alias, deletes source. */
	@Transactional
	public void merge(Integer sourceId, Integer targetId) {
		if (sourceId.equals(targetId)) {
			throw new IllegalArgumentException("Choose a different skill to merge into");
		}
		Skill source = find(sourceId);
		Skill target = find(targetId);

		// 1. user skills: drop rows the user already has on the target, move the rest
		Set<String> have = userSkills(targetId)
			.stream()
			.map(us -> us.getUser().getId() + ":" + us.getType())
			.collect(Collectors.toCollection(HashSet::new));
		for (UserSkill us : userSkills(sourceId)) {
			String k = us.getUser().getId() + ":" + us.getType();
			if (have.contains(k)) {
				em.remove(us);
			} else {
				us.setSkill(target);
				have.add(k);
			}
		}

		// 2. sessions
		for (Session s : em
			.createQuery("SELECT s FROM Session s WHERE s.skill.id = :id", Session.class)
			.setParameter("id", sourceId)
			.getResultList()) {
			s.setSkill(target);
		}

		// 3. aliases: old aliases follow the target, and the source's own name becomes one
		for (SkillAlias a : aliasRepository.findBySkillId(sourceId)) a.setSkill(target);
		String k = SkillAliasService.key(source.getName());
		if (
			!k.isEmpty() &&
			!k.equals(SkillAliasService.key(target.getName())) &&
			aliasRepository.findByAliasKey(k).isEmpty()
		) {
			aliasRepository.save(new SkillAlias(null, k, source.getName(), target));
		}

		// 4. target is now the real one; remove the duplicate
		target.setStatus(SkillStatus.APPROVED);
		em.flush();
		em.remove(source);
	}

	// ---------------------------------------------------------------

	private Skill find(Integer id) {
		Skill s = em.find(Skill.class, id);
		if (s == null) throw new ResourceNotFoundException("Skill", "id", id);
		return s;
	}

	private List<UserSkill> userSkills(Integer skillId) {
		return em
			.createQuery("SELECT us FROM UserSkill us WHERE us.skill.id = :id", UserSkill.class)
			.setParameter("id", skillId)
			.getResultList();
	}

	private Map<Integer, Long> usageBySkill() {
		Map<Integer, Long> m = new HashMap<>();
		for (Object[] r : em
			.createQuery("SELECT us.skill.id, COUNT(us) FROM UserSkill us GROUP BY us.skill.id", Object[].class)
			.getResultList()) {
			m.put((Integer) r[0], (Long) r[1]);
		}
		return m;
	}

	private static boolean similar(String a, String b) {
		String x = ABBREVIATIONS.getOrDefault(a, a);
		String y = ABBREVIATIONS.getOrDefault(b, b);
		if (x.isEmpty() || y.isEmpty()) return false;
		if (x.equals(y)) return true;
		return distance(x, y) <= (Math.max(x.length(), y.length()) <= 5 ? 1 : 2); // typos
	}

	private static int distance(String a, String b) {
		int[] prev = new int[b.length() + 1];
		int[] cur = new int[b.length() + 1];
		for (int j = 0; j <= b.length(); j++) prev[j] = j;
		for (int i = 1; i <= a.length(); i++) {
			cur[0] = i;
			for (int j = 1; j <= b.length(); j++) {
				int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
				cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
			}
			int[] t = prev;
			prev = cur;
			cur = t;
		}
		return prev[b.length()];
	}
}
