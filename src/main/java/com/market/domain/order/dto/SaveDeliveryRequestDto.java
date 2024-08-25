package com.market.domain.order.dto;

import lombok.Getter;

@Getter
public class SaveDeliveryRequestDto {
    private String receiver;
    private String phone;
    private String deliveryAddr;
    private String deliveryMessage;
}