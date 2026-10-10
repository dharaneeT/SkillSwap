package com.skillSwap.Controllers;

import com.skillSwap.Dto.notification.NotificationFeedDTO;
import com.skillSwap.Dto.response.ApiResponse;
import com.skillSwap.Service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/notifications")
@Tag(name = "Notification API", description = "In-app notifications (bell icon)")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<NotificationFeedDTO>> feed() {
		return ResponseEntity.ok(new ApiResponse<>(true, "Notifications", notificationService.myFeed()));
	}

	@PutMapping("/{id}/read")
	public ResponseEntity<ApiResponse<String>> markRead(@PathVariable Integer id) {
		notificationService.markSeen(id);
		return ResponseEntity.ok(new ApiResponse<>(true, "Marked as read", "OK"));
	}

	@PutMapping("/read-all")
	public ResponseEntity<ApiResponse<Integer>> markAllRead() {
		return ResponseEntity.ok(new ApiResponse<>(true, "All marked as read", notificationService.markAllSeen()));
	}
	
		@PutMapping("/chat/{userId}/read")
	public ResponseEntity<ApiResponse<Integer>> markChatRead(@PathVariable Integer userId) {
		return ResponseEntity.ok(new ApiResponse<>(true, "Chat notifications cleared", notificationService.markChatSeen(userId)));
	}
}
