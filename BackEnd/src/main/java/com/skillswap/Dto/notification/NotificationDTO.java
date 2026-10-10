package com.skillSwap.Dto.notification;

import com.skillSwap.Entity.NotificationType;
import java.time.LocalDateTime;

public record NotificationDTO(
        Integer id,
        NotificationType type,
        String message,
        Integer sessionId,
        boolean seen,
                LocalDateTime createdAt,
        Integer fromUserId
) {}