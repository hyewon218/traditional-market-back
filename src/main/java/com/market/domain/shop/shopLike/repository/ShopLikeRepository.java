package com.market.domain.shop.shopLike.repository;

import com.market.domain.member.entity.Member;
import com.market.domain.shop.shopLike.entity.ShopLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopLikeRepository extends JpaRepository<ShopLike, Long> {
    Optional<ShopLike> findByShopNoAndMember(Long shopNo, Member user);

    Long countByShopNo(Long shop);
}
