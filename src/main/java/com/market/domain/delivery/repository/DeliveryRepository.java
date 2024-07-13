package com.market.domain.delivery.repository;

import com.market.domain.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    void deleteAllByMemberNo(long memberNo);
    List<Delivery> findAllByMemberNo(long memberNo);
    Delivery findByIsPrimary(boolean isPrimary); // 기본배송지 조회 시 필요
}
