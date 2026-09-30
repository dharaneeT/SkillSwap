package com.skillSwap.Dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequestDTO {

	@NotNull(message = "RECEIVER_ID should not be Empty")
	private Integer receiverId;

	@NotBlank(message = "Message should not be Empty")
	@Size(max = 1000, message = "Message too long")
	private String content;
}
