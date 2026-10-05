package com.skillSwap.Repository;

import com.skillSwap.Entity.SkillType;
import com.skillSwap.Entity.UserSkill;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Integer> {
	@Query(
		"""
SELECT us.skill.id 
FROM UserSkill us
WHERE us.user.id = :userId
AND us.type = com.skillSwap.Entity.SkillType.WANTED
"""
	)
	List<Integer> findWantedSkillIds(@Param("userId") Integer userId);

	List<UserSkill> findBySkillIdInAndType(List<Integer> skillIds, SkillType type);

	List<UserSkill> findBySkillIdInAndTypeAndUserIdNot(List<Integer> skillIds, SkillType type, Integer userId);

	@Query(
		"SELECT us.skill.id, us.type, COUNT(us) FROM UserSkill us WHERE us.skill.id IN :ids GROUP BY us.skill.id, us.type"
	)
	List<Object[]> countBySkillIds(@Param("ids") Collection<Integer> ids);

	boolean existsByUser_IdAndSkill_IdAndType(Integer userId, Integer skillId, SkillType type);
}
