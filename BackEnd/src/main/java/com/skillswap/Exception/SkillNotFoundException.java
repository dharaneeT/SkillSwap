package com.skillSwap.Exception;

public class SkillNotFoundException extends ResourceNotFoundException {

	public SkillNotFoundException(Object id) {
		super("Skill", "id", id);
	}
}
