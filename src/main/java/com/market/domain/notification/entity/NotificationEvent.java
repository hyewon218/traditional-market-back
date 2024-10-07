package com.market.domain.notification.entity;

import com.market.domain.notification.constant.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {

    private NotificationType type;
    private NotificationArgs args;
    private Long receiverNo;
}