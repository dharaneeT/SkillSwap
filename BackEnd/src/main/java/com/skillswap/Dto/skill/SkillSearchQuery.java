package com.skillSwap.Dto.skill;

import com.skillSwap.Entity.SkillType;

public record SkillSearchQuery(
	String search, // already trimmed + lower-cased, or null
	SkillType type, // or null
	Double minRating, // or null
	int page,
	int size,
	String sortField, // "name" | "id"
	boolean asc
) {}
