package com.skillSwap.Service;

import com.skillSwap.Dto.notification.NotificationDTO;
import com.skillSwap.Dto.notification.NotificationFeedDTO;
import com.skillSwap.Entity.Notification;
import com.skillSwap.Entity.NotificationType;
import com.skillSwap.Entity.User;
import com.skillSwap.Exception.ResourceNotFoundException;
import com.skillSwap.Repository.NotificationRepository;
import com.skillSwap.Security.CurrentUserService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

	private static final int FEED_SIZE = 30;

	private final NotificationRepository repository;
	private final CurrentUserService currentUserService;

	public NotificationService(NotificationRepository repository, CurrentUserService currentUserService) {
		this.repository = repository;
		this.currentUserService = currentUserService;
	}

	// No @Transactional on purpose: joins the caller's transaction,
	// so it is saved/rolled back together with the booking, accept, etc.
	public void notify(User recipient, NotificationType type, String message, Integer sessionId) {
		String text = message.length() > 300 ? message.substring(0, 297) + "..." : message;
		repository.save(new Notification(recipient, type, text, sessionId));
	}

	@Transactional(readOnly = true)
	public NotificationFeedDTO myFeed() {
		Integer me = currentUserService.getCurrentUser().getId();
		List<NotificationDTO> items = repository
			.findByUser_IdOrderByIdDesc(me, PageRequest.of(0, FEED_SIZE))
			.stream()
			.map(this::toDto)
			.toList();
		return new NotificationFeedDTO(repository.countByUser_IdAndSeenFalse(me), items);
	}

	@Transactional
	public void markSeen(Integer id) {
		Integer me = currentUserService.getCurrentUser().getId();
		Notification n = repository
			.findByIdAndUser_Id(id, me)
			.orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
		n.setSeen(true);
	}

	@Transactional
	public int markAllSeen() {
		return repository.markAllSeen(currentUserService.getCurrentUser().getId());
	}
		// One unread chat notification per sender: new messages update it instead of piling up.
	@Transactional
	public void notifyChat(User receiver, User sender, String content) {
		String preview = content.length() > 80 ? content.substring(0, 77) + "..." : content;
		String text = sender.getName() + ": " + preview;
		repository
			.findFirstByUser_IdAndTypeAndFromUserIdAndSeenFalse(receiver.getId(), NotificationType.CHAT_MESSAGE, sender.getId())
			.ifPresentOrElse(
				n -> {
					n.setMessage(text);
					n.setCreatedAt(LocalDateTime.now());
				},
				() -> repository.save(new Notification(receiver, NotificationType.CHAT_MESSAGE, text, null, sender.getId()))
			);
	}

	// called when I open a chat: clears that partner's bell entry
	@Transactional
	public int markChatSeen(Integer fromUserId) {
		return repository.markChatSeen(currentUserService.getCurrentUser().getId(), fromUserId);
	}

	private NotificationDTO toDto(Notification n) {
				return new NotificationDTO(n.getId(), n.getType(), n.getMessage(), n.getSessionId(), n.isSeen(), n.getCreatedAt(), n.getFromUserId());
	}
}
