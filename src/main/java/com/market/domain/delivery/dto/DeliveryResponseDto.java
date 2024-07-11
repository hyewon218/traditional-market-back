package com.market.domain.delivery.dto;//package com.market.domain.delivery.dto;

import com.market.domain.delivery.entity.Delivery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryResponseDto {

    private Long memberNo;
    private String title;
    private String receiver;
    private String phone;
    private String postCode;
    private String roadAddr;
    private String jibunAddr;
    private String detailAddr;
    private String extraAddr;

    public static DeliveryResponseDto of(Delivery delivery) {
        return DeliveryResponseDto.builder()
                .title(delivery.getTitle())
                .receiver(delivery.getReceiver())
                .phone(delivery.getPhone())
                .postCode(delivery.getPostCode())
                .roadAddr(delivery.getRoadAddr())
                .jibunAddr(delivery.getJibunAddr())
                .detailAddr(delivery.getDetailAddr())
                .extraAddr(delivery.getExtraAddr())
                .memberNo(delivery.getMemberNo())
                .build();
    }
}
