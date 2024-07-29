package com.market.domain.order.constant;

public enum OrderStatus {
    ORDER("주문"),
    COMPLETE("주문 완료"),
    CANCEL("주문 취소");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}