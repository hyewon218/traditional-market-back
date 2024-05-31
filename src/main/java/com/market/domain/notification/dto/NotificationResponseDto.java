package com.market.domain.notification.dto;

import com.market.domain.notification.constant.NotificationType;
import com.market.domain.notification.entity.Notification;
import com.market.domain.notification.entity.NotificationArgs;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponseDto {

    private Long id;

    private NotificationType notificationType;

    private NotificationArgs args;

    private boolean isRead;

    private LocalDateTime createdAt;

    public static NotificationResponseDto of(Notification notification) {
        return NotificationResponseDto.builder()
            .id(notification.getNo())
            .notificationType(notification.getNotificationType())
            .args(notification.getArgs())
            .createdAt(notification.getCreateTime())
            .isRead(false)
            .build();
    }
}