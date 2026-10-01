package com.skillSwap.Repository;

import com.skillSwap.Entity.Skill;
import com.skillSwap.Entity.SkillType;
import com.skillSwap.Entity.User;
import com.skillSwap.Entity.UserSkill;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public final class SkillSpecs {

	private SkillSpecs() {}

	// name contains <term>, case-insensitive; % and _ typed by the user are treated literally
	public static Specification<Skill> nameContains(String term) {
		String escaped = term.replace("!", "!!").replace("%", "!%").replace("_", "!_");
		return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + escaped + "%", '!');
	}

	// at least one user has this skill with the given type
	public static Specification<Skill> hasUsersOfType(SkillType type) {
		return (root, query, cb) -> {
			Subquery<Integer> sub = query.subquery(Integer.class);
			Root<UserSkill> us = sub.from(UserSkill.class);
			sub
				.select(us.<Integer>get("id"))
				.where(cb.equal(us.get("skill").get("id"), root.get("id")), cb.equal(us.get("type"), type));
			return cb.exists(sub);
		};
	}

	// at least one OFFERING provider has averageRating >= minRating
	public static Specification<Skill> offeredByProviderRatedAtLeast(double minRating) {
		return (root, query, cb) -> {
			Subquery<Integer> sub = query.subquery(Integer.class);
			Root<UserSkill> us = sub.from(UserSkill.class);
			Join<UserSkill, User> user = us.join("user");
			sub
				.select(us.<Integer>get("id"))
				.where(
					cb.equal(us.get("skill").get("id"), root.get("id")),
					cb.equal(us.get("type"), SkillType.OFFERED),
					cb.greaterThanOrEqualTo(user.<Double>get("averageRating"), minRating)
				);
			return cb.exists(sub);
		};
	}
}
