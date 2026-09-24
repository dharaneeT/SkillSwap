package com.skillSwap.skillswap.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;
	private String email;
	private Integer credits;

	@Column(nullable = false)
	private String password;

	@OneToMany(mappedBy = "user")
	@JsonManagedReference
	private List<UserSkill> skills;
}
