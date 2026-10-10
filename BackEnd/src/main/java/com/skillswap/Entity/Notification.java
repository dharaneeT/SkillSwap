package com.skillSwap.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification", indexes = @Index(name = "idx_notification_user_seen", columnList = "user_id, seen"))
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private NotificationType type;

	@Column(nullable = false, length = 300)
	private String message;

	private Integer sessionId;

	// for CHAT_MESSAGE: who sent it (null for other types)
	private Integer fromUserId;

	@Column(nullable = false)
	private boolean seen = false;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	// REQUIRED by JPA/Hibernate - do not delete
	public Notification() {}

	public Notification(User user, NotificationType type, String message, Integer sessionId) {
		this(user, type, message, sessionId, null);
	}

	public Notification(User user, NotificationType type, String message, Integer sessionId, Integer fromUserId) {
		this.user = user;
		this.type = type;
		this.message = message;
		this.sessionId = sessionId;
		this.fromUserId = fromUserId;
		this.seen = false;
		this.createdAt = LocalDateTime.now();
	}

	public Integer getId() { return id; }
	public User getUser() { return user; }
	public NotificationType getType() { return type; }
	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }
	public Integer getSessionId() { return sessionId; }
	public Integer getFromUserId() { return fromUserId; }
	public boolean isSeen() { return seen; }
	public void setSeen(boolean seen) { this.seen = seen; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}