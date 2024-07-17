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
     * @param deliveryRequestDto : 배송지 추가 요청 정보
     * @param member             : 매핑할 회원
     * @return : 배송지 저장
     */
    DeliveryResponseDto createDelivery(DeliveryRequestDto deliveryRequestDto, Member member);

    /**
     * 배송지 목록 조회
     *
     * @param member : 해당 배송지 소유 회원
     * @return : 저장된 배송지 목록 반환
     */
    List<DeliveryResponseDto> getAllDeliveries(Member member);

    /**
     * 회원 특정 배송지 조회
     *
     * @param member     : 해당 배송지 소유 회원
     * @param deliveryNo : 조회할 배송지 고유번호
     * @return : 저장된 배송지 목록 반환
     */
    DeliveryResponseDto getDeliveryById(Member member, Long deliveryNo);

    /**
     * 특정 배송지 조회
     *
     * @param deliveryNo : 조회할 배송지 고유번호
     * @return : 저장된 배송지 목록 반환
     */
    Delivery findById(Long deliveryNo);

    /**
     * 배송지 수정
     *
     * @param deliveryNo               : 수정할 배송지 고유번호
     * @param deliveryUpdateRequestDto : 수정할 배송지 필드 요청 정보
     * @return : 수정된 배송지
     */
    DeliveryResponseDto updateDelivery(Member member, Long deliveryNo,
        DeliveryUpdateRequestDto deliveryUpdateRequestDto);

    /**
     * 배송지 삭제
     *
     * @param member : 배송지 회원 정보
     * @param deliveryNo : 삭제할 배송지 고유번호
     */
    void deleteDelivery(Member member, Long deliveryNo);

    /**
     * 기본배송지 설정
     *
     * @param deliveryNo : 기본배송지로 설정할 배송지 고유번호
     * @param member : 배송지 회원 정보
     */
    void setPrimary(Member member, Long deliveryNo);

    /**
     * 기본배송지 해제
     *
     * @param member : 기본 배송지 확인할 회원 정보
     */
    void removePrimary(Member member);

    /**
     * 회원에 해당하는 배송지 확인
     *
     * @param deliveryNo : 확인할 배송지 고유번호
     * @param member : 확인할 회원 정보
     */
    void validateDelivery(Member member, Long deliveryNo);

    /**
     * 기본 배송지 찾기
     *
     * @param member : 기본 배송지 확인할 회원 정보
     */
    Delivery getCurrentPrimaryDelivery(Member member);
}
