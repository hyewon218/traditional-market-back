package com.market.domain.cart.service;

import static org.junit.jupiter.api.Assertions.*;

import com.market.domain.cartItem.dto.CartItemRequestDto;
import com.market.domain.cartItem.entity.CartItem;
import com.market.domain.cartItem.repository.CartItemRepository;
import com.market.domain.item.constant.ItemSellStatus;
import com.market.domain.item.entity.Item;
import com.market.domain.item.repository.ItemRepository;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class CartServiceImplTest {
    @Autowired
    ItemRepository itemRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    CartService cartService;

    @Autowired
    CartItemRepository cartItemRepository;

    public Item saveItem(){
        Item item = new Item();
        item.setItemName("테스트 상품");
        item.setPrice(10000);
        item.setItemDetail("테스트 상품 상세 설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        return itemRepository.save(item);
    }

    public Member saveMember(){
        Member member = new Member();
        member.setMemberEmail("test@test.com");
        return memberRepository.save(member);
    }

    @Test
    @DisplayName("장바구니 담기 테스트")
    public void addCart(){
        Item item = saveItem();
        Member member = saveMember();

        CartItemRequestDto cartItemDto = new CartItemRequestDto();
        cartItemDto.setCount(5);
        cartItemDto.setItemNo(item.getNo());

        Long cartItemId = cartService.addCart(cartItemDto, member);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_CART_ITEM));

        assertEquals(item.getNo(), cartItem.getItem().getNo());
        assertEquals(cartItemDto.getCount(), cartItem.getCount());
    }
}