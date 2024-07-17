package com.market.domain.delivery.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.delivery.dto.DeliveryUpdateRequestDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "delivery")
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_no")
    private Long deliveryNo;

    private Long memberNo;

    @Column(nullable = false)
    private String title; // 배송지 이름

    @Column(nullable = false)
    private String receiver; // 받는 사람

    @Column(nullable = false)
    private String phone; // 휴대전화번호

    @Column(nullable = false)
    private String postCode; // 우편번호

    private String roadAddr; // 도로명 주소

    private String jibunAddr; // 지번 주소

    @Column(nullable = false)
    private String detailAddr; // 상세 주소

    private String extraAddr; // 참고 항목

    private boolean isPrimary; // 기본배송지 설정, 기본값 false

    // 배송지 정보 수정 메서드
    public void update(DeliveryUpdateRequestDto deliveryUpdateRequestDto) {
        this.title = deliveryUpdateRequestDto.getTitle();
        this.receiver = deliveryUpdateRequestDto.getReceiver();
        this.phone = deliveryUpdateRequestDto.getPhone();
        this.postCode = deliveryUpdateRequestDto.getPostCode();
        this.roadAddr = deliveryUpdateRequestDto.getRoadAddr();
        this.jibunAddr = deliveryUpdateRequestDto.getJibunAddr();
        this.detailAddr = deliveryUpdateRequestDto.getDetailAddr();
        this.extraAddr = deliveryUpdateRequestDto.getExtraAddr();
    }

    // 기본배송지로 변경
    public void updatePrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
}

