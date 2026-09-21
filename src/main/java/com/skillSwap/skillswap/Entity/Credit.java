package com.skillSwap.skillswap.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Credit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer amount;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
}
