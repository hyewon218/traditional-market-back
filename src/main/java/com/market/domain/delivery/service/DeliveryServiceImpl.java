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
import java.util.NoSuchElementException;

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

    // 기본배송지 설정
    @Override
    @Transactional
    public void setPrimary(long deliveryNo) {
        // 기존 기본배송지 찾기
        Delivery currentDelivery = deliveryRepository.findByIsPrimary(true);
        if (currentDelivery != null) {
            currentDelivery.updatePrimary(false);
            deliveryRepository.save(currentDelivery);
            
            // 기존 기본배송지 false 설정하고 새로운 기본배송지는 true 설정
            Delivery newDelivery = deliveryRepository.findById(deliveryNo)
                    .orElseThrow(() -> new IllegalArgumentException("일치하는 배송지가 없습니다"));
            newDelivery.updatePrimary(true);
            deliveryRepository.save(newDelivery);

        } else {
            // 기존 기본배송지 없을 경우 새로운 기본배송지 설정
            Delivery newDelivery = deliveryRepository.findById(deliveryNo)
                    .orElseThrow(() -> new IllegalArgumentException("일치하는 배송지가 없습니다"));
            newDelivery.updatePrimary(true);
            deliveryRepository.save(newDelivery);
        }
    }

    // 기본배송지 해제
    @Override
    @Transactional
    public void removePrimary() {
        Delivery currentDelivery = deliveryRepository.findByIsPrimary(true);
        if (currentDelivery == null) {
            throw new NoSuchElementException("기본배송지가 존재하지 않습니다");
        }
        currentDelivery.updatePrimary(false);
        deliveryRepository.save(currentDelivery);
    }
}
