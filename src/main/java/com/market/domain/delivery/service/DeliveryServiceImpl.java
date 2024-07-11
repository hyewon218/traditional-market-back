package com.market.domain.delivery.service;

import com.market.domain.delivery.dto.DeliveryRequestDto;
import com.market.domain.delivery.dto.DeliveryResponseDto;
import com.market.domain.delivery.dto.DeliveryUpdateRequestDto;
import com.market.domain.delivery.entity.Delivery;
import com.market.domain.delivery.repository.DeliveryRepository;
import com.market.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;

    // 배송지 추가
    @Override
    @Transactional
    public Delivery createDelivery(DeliveryRequestDto deliveryRequestDto, Member member) {
        return deliveryRepository.save(deliveryRequestDto.toEntity(member));
    }

    // 배송지 전체 조회
    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponseDto> findAll(long memberNo) {
        List<Delivery> deliveries = deliveryRepository.findAllByMemberNo(memberNo);
        List<DeliveryResponseDto> deliveryResponseDtos = deliveries
                .stream()
                .map(DeliveryResponseDto::of)
                .toList();
        return deliveryResponseDtos;
    }

    // 특정 배송지 조회
    @Override
    @Transactional(readOnly = true)
    public Delivery findById(long deliveryNo) {
        return deliveryRepository.findById(deliveryNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 배송지가 없습니다"));
    }

    // 배송지 수정
    @Override
    @Transactional
    public Delivery update(long deliveryNo, DeliveryUpdateRequestDto deliveryUpdateRequestDto) {
        Delivery delivery = deliveryRepository.findById(deliveryNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 배송지가 존재하지않습니다"));

        delivery.update(deliveryUpdateRequestDto.getTitle(), deliveryUpdateRequestDto.getReceiver(),
                deliveryUpdateRequestDto.getPhone(), deliveryUpdateRequestDto.getPostCode(),
                deliveryUpdateRequestDto.getRoadAddr(), deliveryUpdateRequestDto.getJibunAddr(),
                deliveryUpdateRequestDto.getDetailAddr(), deliveryUpdateRequestDto.getExtraAddr());

        return delivery;
    }

    // 배송지 삭제
    @Override
    @Transactional
    public void delete(long deliveryNo) {
        Delivery delivery = deliveryRepository.findById(deliveryNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 배송지가 존재하지않습니다"));
        deliveryRepository.deleteById(delivery.getDeliveryNo());
    }
}
