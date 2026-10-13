package com.skillSwap.Service;

import com.skillSwap.Entity.Skill;
import com.skillSwap.Entity.SkillAlias;
import com.skillSwap.Repository.SkillAliasRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SkillAliasService {

	private final SkillAliasRepository aliasRepository;

	public SkillAliasService(SkillAliasRepository aliasRepository) {
		this.aliasRepository = aliasRepository;
	}

	/** "Node.js" and " node JS " both become "nodejs". Keeps + and # so C++ / C# stay distinct. */
	public static String key(String name) {
		return name == null ? "" : name.trim().toLowerCase().replaceAll("[^a-z0-9+#]", "");
	}

	public Optional<Skill> resolve(String typedName) {
		String k = key(typedName);
		if (k.isEmpty()) return Optional.empty();
		return aliasRepository.findByAliasKey(k).map(SkillAlias::getSkill);
	}
}
