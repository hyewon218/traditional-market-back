package com.market.domain.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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