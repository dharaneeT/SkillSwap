package com.skillSwap.Dto.chat;

import java.time.LocalDateTime;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponseDTO {

	private Integer id;
	private Integer senderId;
	private Integer receiverId;
	private String content;
	private LocalDateTime sentAt;
}
