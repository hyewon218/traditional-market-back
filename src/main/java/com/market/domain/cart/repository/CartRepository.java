package com.market.domain.cart.repository;

import com.market.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findByMember_MemberNo(Long memberNo);
}