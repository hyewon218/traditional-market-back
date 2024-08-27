package com.market.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    private NotificationArgs args;

    private boolean isRead;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime createTime;

    public static NotificationResponseDto of(Notification notification) {
        return NotificationResponseDto.builder()
            .no(notification.getNo())
            .notificationType(notification.getNotificationType())
            .args(notification.getArgs())
            .createTime(notification.getCreateTime())
            .isRead(notification.isRead())
            .build();
    }
}