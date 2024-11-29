package com.market.domain.market.repository;

import com.market.domain.market.entity.CategoryEnum;
import com.market.domain.market.entity.Market;
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
public interface MarketRepository extends JpaRepository<Market, Long> {

    Page<Market> findAll(Pageable pageable);
    boolean existsMarketByMarketName(String marketName);

    Page<Market> findByCategoryOrderByMarketName(CategoryEnum category, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    @Query("SELECT m FROM Market m WHERE m.no = :no")
    Optional<Market> findByIdWithLock(@Param("no") Long no);
}