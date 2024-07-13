package com.market.domain.delivery.service;

import com.market.domain.delivery.dto.DeliveryRequestDto;
import com.market.domain.delivery.dto.DeliveryResponseDto;
import com.market.domain.delivery.dto.DeliveryUpdateRequestDto;
import com.market.domain.delivery.entity.Delivery;
import com.market.domain.member.entity.Member;

import java.util.List;

public interface DeliveryService {

    /**
     * 배송지 추가
     *
     * @param deliveryRequestDto  : 배송지 추가 요청 정보
     * @param member : 매핑할 회원
     * @return : 배송지 저장
     */
    Delivery createDelivery(DeliveryRequestDto deliveryRequestDto, Member member);

    /**
     * 배송지 목록 조회
     *
     * @param memberNo : 해당 배송지 소유 회원
     * @return : 저장된 배송지 목록 반환
     */
    List<DeliveryResponseDto> findAll(long memberNo);

    /**
     * 특정 배송지 조회
     *
     * @param deliveryNo : 조회할 배송지 고유번호
     * @return : 저장된 배송지 목록 반환
     */
    Delivery findById(long deliveryNo);

    /**
     * 배송지 수정
     *
     * @param deliveryNo  : 수정할 배송지 고유번호
     * @param deliveryUpdateRequestDto : 수정할 배송지 필드 요청 정보
     * @return : 수정된 배송지
     */
    Delivery update(long deliveryNo, DeliveryUpdateRequestDto deliveryUpdateRequestDto);

    /**
     * 배송지 삭제
     *
     * @param deliveryNo  : 삭제할 배송지 고유번호
     */
    void delete(long deliveryNo);

    /**
     * 기본배송지 설정
     *
     * @param deliveryNo  : 기본배송지로 설정할 배송지 고유번호
     */
    void setPrimary(long deliveryNo);

    /**
     * 기본배송지 해제
     *
     */
    void removePrimary();
}
