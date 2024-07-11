package com.market.domain.deliveryMessage.dto;

import com.market.domain.deliveryMessage.entity.DeliveryMessage;
import com.market.domain.member.entity.Member;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryMessageRequestDto {

    @Size(max = 30)
    private String content;

    public DeliveryMessage toEntity(Member member) {
        return DeliveryMessage.builder()
                .memberNo(member.getMemberNo())
                .content(this.content)
                .build();
    }
}
