package com.market.domain.shop.shopComment.repository;

import com.market.domain.shop.shopComment.entity.ShopComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopCommentRepository extends JpaRepository<ShopComment, Long> {
}
