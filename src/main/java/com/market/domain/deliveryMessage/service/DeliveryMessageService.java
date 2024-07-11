package com.market.domain.deliveryMessage.service;

import com.market.domain.deliveryMessage.dto.DeliveryMessageRequestDto;
import com.market.domain.deliveryMessage.dto.DeliveryMessageResponseDto;
import com.market.domain.deliveryMessage.entity.DeliveryMessage;
import com.market.domain.member.entity.Member;

import java.util.List;

public interface DeliveryMessageService {

    /**
     * 배송 메시지 추가
     *
     * @param deliveryMessageRequestDto  : 배송 메시지 추가 요청 정보
     * @param member : 매핑할 회원
     * @return : 배송 메시지 저장
     */
    DeliveryMessage createDeliveryMessage(DeliveryMessageRequestDto deliveryMessageRequestDto, Member member);

    /**
     * 배송 메시지 목록 조회
     *
     * @param memberNo : 조회할 배송 메시지의 회원 고유번호
     * @return : 저장된 배송 메시지 목록 반환
     */
    List<DeliveryMessageResponseDto> findAll(long memberNo);

    /**
     * 배송 메시지 삭제
     *
     * @param deliveryMessageNo : 삭제할 배송 메시지 고유번호
     */
    void delete(long deliveryMessageNo);
}
