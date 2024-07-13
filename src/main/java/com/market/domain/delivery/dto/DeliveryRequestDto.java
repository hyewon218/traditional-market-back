package com.market.domain.delivery.dto;//package com.market.domain.delivery.dto;

import com.market.domain.delivery.entity.Delivery;
import com.market.domain.member.entity.Member;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryRequestDto {

    @NotBlank(message = "배송지 이름을 입력해주세요")
    private String title;

    @NotBlank(message = "받는 사람을 입력해주세요")
    private String receiver;

    @NotBlank(message = "휴대전화번호를 입력해주세요")
    @Size(min = 10, max = 11)
    private String phone; // 휴대전화번호

    @NotBlank(message = "주소를 검색해주세요")
    private String postCode; // 우편번호

    private String roadAddr; // 도로명 주소

    private String jibunAddr; // 지번 주소

    @NotBlank(message = "상세주소를 입력해주세요")
    private String detailAddr; // 상세 주소

    private String extraAddr; // 참고 항목


    public Delivery toEntity(Member member) {
        return Delivery.builder()
                .memberNo(member.getMemberNo())
                .title(this.title)
                .receiver(this.receiver)
                .phone(this.phone)
                .postCode(this.postCode)
                .roadAddr(this.roadAddr)
                .jibunAddr(this.jibunAddr)
                .detailAddr(this.detailAddr)
                .extraAddr(this.extraAddr)
                .isPrimary(false)
                .build();
    }

}

