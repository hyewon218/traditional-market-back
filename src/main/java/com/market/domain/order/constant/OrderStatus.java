package com.market.domain.order.constant;

public enum OrderStatus {
    ORDER("주문"),
    COMPLETE("결제 완료"),
    READYITEM("상품 준비중"),
    READYSHIP("배송 준비중"),
    SHIPPED("배송중"),
    FINISH("배송 완료"),
    PURCHASECONFIRM("구매 확정"),
    CANCEL("주문 취소"),
    RETURN("반품 신청"),
    RETURNCOMPLETE("반품 완료"),
    REFUND("환불 완료");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}