package com.skillSwap.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "sender_id")
	private User sender;

	@ManyToOne(optional = false)
	@JoinColumn(name = "receiver_id")
	private User receiver;

	@Column(nullable = false, length = 1000)
	private String content;

	@Column(nullable = false)
	private LocalDateTime sentAt;
}
