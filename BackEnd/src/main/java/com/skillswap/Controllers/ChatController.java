package com.skillSwap.Controllers;

import com.skillSwap.Dto.chat.*;
import com.skillSwap.Dto.response.ApiResponse;
import com.skillSwap.Service.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/chat")
@Tag(name = "Chat API", description = "Direct messages between users")
public class ChatController {

	private final ChatService chatService;

	public ChatController(ChatService chatService) {
		this.chatService = chatService;
	}

	//Send message to users
	@PostMapping("/send")
	public ResponseEntity<ApiResponse<MessageResponseDTO>> send(@Valid @RequestBody MessageRequestDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Sent", chatService.send(dto)));
	}

	//Return chat of mentioned user
	@GetMapping("/with/{userId}")
	public ResponseEntity<ApiResponse<List<MessageResponseDTO>>> with(@PathVariable Integer userId) {
		return ResponseEntity.ok(new ApiResponse<>(true, "Conversation", chatService.conversation(userId)));
	}

	//return all the partners you were chatting with
	@GetMapping("/partners")
	public ResponseEntity<ApiResponse<List<ChatPartnerDTO>>> partners() {
		return ResponseEntity.ok(new ApiResponse<>(true, "Chat partners", chatService.partners()));
	}
}
