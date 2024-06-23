package com.market.domain.kakaoPay.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Amount {

    private int total; // 총 결제금액
    private int tax_free; // 비과세금액
    private int vat; // 부가세금액
    private int point; // 포인트
    private int discount; // 할인금액
    private int green_deposit; // 컵 보증금
}
