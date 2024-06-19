package com.market.domain.notification.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NotificationType {
    NEW_LIKE_ON_MARKET("시장에 좋아요가 눌렸어요!"),
    NEW_LIKE_ON_SHOP("상점에 좋아요가 눌렸어요!"),
    NEW_LIKE_ON_ITEM("상품에 좋아요가 눌렸어요!"),
    NEW_COMMENT_ON_MARKET("시장에 댓글이 달렸어요!"),
    NEW_COMMENT_ON_SHOP("상점에 댓글이 달렸어요!"),
    NEW_COMMENT_ON_ITEM("상품에 댓글이 달렸어요!"),
    NEW_PURCHASE_ON_SHOP("판매 상품에 구매요청이 왔어요!");

    private final String notificationContent;
}