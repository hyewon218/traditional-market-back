package com.market.domain.delivery.repository;

import com.market.domain.delivery.entity.Delivery;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    void deleteAllByMemberNo(long memberNo);

    List<Delivery> findAllByMemberNo(long memberNo);

    boolean existsByDeliveryNoAndMemberNo(Long deliveryNo, Long memberNo);

    Delivery findByMemberNoAndIsPrimary(Long memberNo, boolean isPrimary); // 기본배송지 조회 시 필요
}
