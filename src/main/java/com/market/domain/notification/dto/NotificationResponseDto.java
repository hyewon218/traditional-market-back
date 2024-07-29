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

    private Long no;

    private NotificationType notificationType;

    private String notificationContent;

    private NotificationArgs args;

    private boolean isRead;

    private LocalDateTime createdAt;

    public static NotificationResponseDto of(Notification notification) {
        return NotificationResponseDto.builder()
            .no(notification.getNo())
            .notificationType(notification.getNotificationType())
            .notificationContent(notification.getNotificationType().toString())
            .args(notification.getArgs())
            .createdAt(notification.getCreateTime())
            .isRead(false)
            .build();
    }
}