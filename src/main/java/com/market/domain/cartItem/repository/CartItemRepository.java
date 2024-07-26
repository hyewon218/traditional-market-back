package com.market.domain.cartItem.repository;

import com.market.domain.cartItem.entity.CartItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    CartItem findByCart_NoAndItem_No(Long cartNo, Long itemNo); // 상품이 장바구니에 있는지 조회

    List<CartItem> findAllByCart_No(Long cartNo);
}