package com.market.domain.delivery.service;

import com.market.domain.delivery.dto.DeliveryRequestDto;
import com.market.domain.delivery.dto.DeliveryResponseDto;
import com.market.domain.delivery.dto.DeliveryUpdateRequestDto;
import com.market.domain.delivery.entity.Delivery;
import com.market.domain.delivery.repository.DeliveryRepository;
import com.market.domain.member.entity.Member;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;

    @Override
    @Transactional // 배송지 추가
    public DeliveryResponseDto createDelivery(DeliveryRequestDto deliveryRequestDto,
        Member member) {
        Delivery delivery = deliveryRequestDto.toEntity(member);
        deliveryRepository.save(delivery);
        return DeliveryResponseDto.of(delivery);
    }

    @Override
    @Transactional(readOnly = true) // 배송지 전체 조회
    public List<DeliveryResponseDto> getAllDeliveries(Member member) {
        return deliveryRepository.findAllByMemberNo(member.getMemberNo())
            .stream()
            .map(DeliveryResponseDto::of)
            .toList();
    }

    @Override
    @Transactional(readOnly = true) // 회원 특정 배송지 조회
    public DeliveryResponseDto getDeliveryById(Member member, Long deliveryNo) {
        validateDelivery(member, deliveryNo);
        Delivery delivery = findById(deliveryNo);
        return DeliveryResponseDto.of(delivery);
    }

    @Override
    @Transactional(readOnly = true) // 특정 배송지 조회(공통)
    public Delivery findById(Long deliveryNo) {
        return deliveryRepository.findById(deliveryNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_DELIVERY));
    }

    @Override
    @Transactional // 배송지 수정
    public DeliveryResponseDto updateDelivery(Member member, Long deliveryNo,
        DeliveryUpdateRequestDto deliveryUpdateRequestDto) {
        validateDelivery(member, deliveryNo);
        Delivery delivery = findById(deliveryNo);
        delivery.update(deliveryUpdateRequestDto);
        return DeliveryResponseDto.of(delivery);
    }

    @Override
    @Transactional // 배송지 삭제
    public void deleteDelivery(Member member, Long deliveryNo) {
        validateDelivery(member, deliveryNo);
        deliveryRepository.deleteById(deliveryNo);
    }

    @Override
    @Transactional // 기본배송지 설정
    public void setPrimary(Member member, Long deliveryNo) {
        // 기존 기본배송지 찾기
        Delivery currentPrimaryDelivery = getCurrentPrimaryDelivery(member);
        Delivery newPrimaryDelivery = findById(deliveryNo);
        if (currentPrimaryDelivery != null) {
            // 기존 기본배송지를 기본 배송지에서 해제
            currentPrimaryDelivery.updatePrimary(false);
            // 새로운 기본배송지 설정
            newPrimaryDelivery.updatePrimary(true);
        } else {
            // 새로운 기본배송지 설정
            newPrimaryDelivery.updatePrimary(true);
        }
    }

    @Override
    @Transactional(readOnly = true) // 기본 배송지 조회(주문 페이지)
    public DeliveryResponseDto getCurrentPrimaryDeliveryDto(Member member) {
        Delivery primaryDelivery = getCurrentPrimaryDelivery(member);
        return DeliveryResponseDto.of(primaryDelivery);
    }

    @Override
    @Transactional // 기본배송지 해제
    public void removePrimary(Member member) {
        // 기본 배송지 찾기
        Delivery currentPrimaryDelivery = getCurrentPrimaryDelivery(member);
        currentPrimaryDelivery.updatePrimary(false);
        deliveryRepository.save(currentPrimaryDelivery);
    }

    @Override
    @Transactional(readOnly = true) // 회원에 해당하는 배송지 확인
    public void validateDelivery(Member member, Long deliveryNo) {
        boolean exists = deliveryRepository.existsByDeliveryNoAndMemberNo(deliveryNo,
            member.getMemberNo());
        if (!exists) {
            throw new BusinessException(ErrorCode.NOT_AUTHORITY_DELIVERY);
        }
    }

    @Override
    @Transactional(readOnly = true) // 기본 배송지 찾기
    public Delivery getCurrentPrimaryDelivery(Member member) {
        return deliveryRepository.findByMemberNoAndIsPrimary(member.getMemberNo(), true)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PRIMARY_DELIVERY));
    }
}
