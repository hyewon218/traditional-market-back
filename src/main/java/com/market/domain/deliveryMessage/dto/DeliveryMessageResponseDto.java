package com.market.domain.deliveryMessage.dto;

import com.market.domain.deliveryMessage.entity.DeliveryMessage;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryMessageResponseDto {

    private Long no;

    @Size(max = 30)
    private String content;

    public static DeliveryMessageResponseDto of(DeliveryMessage deliveryMessage) {
        return DeliveryMessageResponseDto.builder()
                .no(deliveryMessage.getDeliveryMessageNo())
                .content(deliveryMessage.getContent())
                .build();
    }
}
