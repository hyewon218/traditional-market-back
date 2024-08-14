package com.market.domain.notification.constant;

import lombok.Getter;

@Getter
public enum NotificationType {

    NEW_LIKE_ON_MARKET,
    NEW_LIKE_ON_SHOP,
    NEW_LIKE_ON_ITEM,
    NEW_COMMENT_ON_MARKET,
    NEW_COMMENT_ON_SHOP,
    NEW_COMMENT_ON_ITEM,
    NEW_PURCHASE_ON_SHOP,
    NEW_CHAT_ON_CHATROOM,
    NEW_CHAT_REQUEST_ON_CHATROOM,
    NEW_INQUIRY_ANSWER
}