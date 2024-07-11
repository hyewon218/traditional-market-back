package com.market.domain.delivery.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.member.entity.Member;
import jakarta.persistence.*;
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

    // 배송지 정보 수정 메서드
    public void update(String title, String receiver, String phone,
                       String postCode, String roadAddr, String jibunAddr,
                       String detailAddr, String extraAddr) {

        this.title = title;
        this.receiver = receiver;
        this.phone = phone;
        this.postCode = postCode;
        this.roadAddr = roadAddr;
        this.jibunAddr = jibunAddr;
        this.detailAddr = detailAddr;
        this.extraAddr = extraAddr;
    }

}

