package com.market.domain.notification.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationArgs {
    private Long fromMemberNo;
    private Long targetId;

    public static NotificationArgs of(Long fromMemberNo, Long targetId) {
        return NotificationArgs.builder()
            .fromMemberNo(fromMemberNo)
            .targetId(targetId)
            .build();
    }
}