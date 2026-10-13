package com.skillSwap.Dto.skill;

import java.util.List;

public record SkillModerationDTO(Integer id, String name, long usageCount, List<Suggestion> suggestions) {
	public record Suggestion(Integer id, String name) {}
}
