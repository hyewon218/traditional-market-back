package com.market.domain.delivery.repository;

import com.market.domain.delivery.entity.Delivery;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    void deleteAllByMemberNo(long memberNo);

    Page<Delivery> findAllByMemberNo(Long memberNo, Pageable pageable);

    boolean existsByDeliveryNoAndMemberNo(Long deliveryNo, Long memberNo);

    Optional<Delivery> findByMemberNoAndIsPrimary(Long memberNo, boolean isPrimary); // 기본배송지 조회 시 필요
}
