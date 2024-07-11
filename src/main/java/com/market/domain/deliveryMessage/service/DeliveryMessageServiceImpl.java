package com.market.domain.deliveryMessage.service;

import com.market.domain.deliveryMessage.dto.DeliveryMessageRequestDto;
import com.market.domain.deliveryMessage.dto.DeliveryMessageResponseDto;
import com.market.domain.deliveryMessage.entity.DeliveryMessage;
import com.market.domain.deliveryMessage.repository.DeliveryMessageRepository;
import com.market.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryMessageServiceImpl implements DeliveryMessageService {

    private final DeliveryMessageRepository deliveryMessageRepository;


    // 배송 메시지 생성
    @Override
    @Transactional
    public DeliveryMessage createDeliveryMessage(DeliveryMessageRequestDto deliveryMessageRequestDto, Member member) {
        return deliveryMessageRepository.save(deliveryMessageRequestDto.toEntity(member));
    }

    // 배송 메시지 전체 조회
    @Override
    @Transactional(readOnly = true)
    public List<DeliveryMessageResponseDto> findAll(long memberNo) {
        List<DeliveryMessage> deliveryMessages = deliveryMessageRepository.findAllByMemberNo(memberNo);
        List<DeliveryMessageResponseDto> deliveryMessageResponseDtos = deliveryMessages
                .stream()
                .map(DeliveryMessageResponseDto::of)
                .toList();
        return deliveryMessageResponseDtos;
    }

    // 배송 메시지 삭제
    @Override
    @Transactional
    public void delete(long deliveryMessageNo) {
        DeliveryMessage deliveryMessage = deliveryMessageRepository.findById(deliveryMessageNo)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 배송메시지가 없습니다"));
        deliveryMessageRepository.deleteById(deliveryMessage.getDeliveryMessageNo());
    }

}
