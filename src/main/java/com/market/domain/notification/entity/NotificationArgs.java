package com.market.domain.notification.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationArgs {
    private Long fromMemberNo;
    private Long targetId;
}