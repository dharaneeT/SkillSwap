package com.skillSwap.Dto.notification;

import java.util.List;

public record NotificationFeedDTO(long unreadCount, List<NotificationDTO> items) {}
