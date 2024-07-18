package com.market.domain.cartItem.repository;

import com.market.domain.cartItem.dto.CartItemDetailResponseDto;
import com.market.domain.cartItem.entity.CartItem;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    CartItem findByCart_NoAndItem_No(Long cartNo, Long itemNo); // 상품이 장바구니에 있는지 조회

    @Query( // DTO 의 생성자를 이용하여 반환 값으로 DTO 객체를 생성
        "select new com.market.domain.cartItem.dto.CartItemDetailResponseDto(ci.no, i.no, i.itemName, i.price, ci.count, img.imageUrl) "
            +
            "from CartItem ci " +
            "join ci.item i " +
            "join ci.item.imageList img " +
            "where ci.cart.no = :cartId " +
            "order by ci.createTime desc"
    )
    List<CartItemDetailResponseDto> findCartDetailDtoList(@Param("cartId") Long cartId);

    List<CartItem> findAllByCart_No(Long cartNo);
}