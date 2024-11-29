package com.market.domain.shop.repository;

import com.market.domain.shop.entity.CategoryEnum;
import com.market.domain.shop.entity.Shop;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    Page<Shop> findAll(Pageable pageable);

    Page<Shop> findAllByMarket_No(Long marketNo, Pageable pageable);

    Page<Shop> findByCategoryOrderByCategoryDesc(CategoryEnum category, Pageable pageable);

    Page<Shop> findByMarketNoAndCategory(Long marketNo, CategoryEnum category, Pageable pageable);

    Long countByMarket_No(Long marketNo);
    
    Page<Shop> findBySeller_MemberNo(Long memberNo, Pageable pageable); // 판매자가 소유한 상점 목록 조회

    boolean existsBySeller_MemberNo(Long memberNo); // 상점에 대한 판매자가 맞는지 확인

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    @Query("SELECT s FROM Shop s WHERE s.no = :no")
    Optional<Shop> findByIdWithLock(@Param("no") Long no);
}