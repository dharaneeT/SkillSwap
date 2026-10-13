package com.skillSwap.Repository;

import com.skillSwap.Entity.SkillAlias;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillAliasRepository extends JpaRepository<SkillAlias, Integer> {
	Optional<SkillAlias> findByAliasKey(String aliasKey);

	List<SkillAlias> findBySkillId(Integer skillId);
}
