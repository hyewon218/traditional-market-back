package com.market.domain.deliveryMessage.dto;

import com.market.domain.deliveryMessage.entity.DeliveryMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryMessageResponseDto {

    private String content;

    public static DeliveryMessageResponseDto of(DeliveryMessage deliveryMessage) {
        return DeliveryMessageResponseDto.builder()
                .content(deliveryMessage.getContent())
                .build();
    }
}
