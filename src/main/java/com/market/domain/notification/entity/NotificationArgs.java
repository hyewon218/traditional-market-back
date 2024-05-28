package com.market.domain.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationArgs {
    // user who occur alarm
    private Long fromMemberNo;
    private Long targetId;
}