package com.market.domain.order.service;

import static org.junit.jupiter.api.Assertions.*;

import com.market.domain.item.constant.ItemSellStatus;
import com.market.domain.item.entity.Item;
import com.market.domain.item.repository.ItemRepository;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.domain.order.entity.Order;
import com.market.domain.order.repository.OrderRepository;
import com.market.domain.orderItem.constant.OrderStatus;
import com.market.domain.orderItem.dto.OrderItemRequestDto;
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
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    MemberRepository memberRepository;

    public Item saveItem() {
        Item item = new Item();
        item.setItemName("테스트 상품");
        item.setPrice(10000);
        item.setItemDetail("테스트 상품 상세 설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        return itemRepository.save(item);
    }

    public Member saveMember() {
        Member member = new Member();
        member.setMemberId("test1234");
        return memberRepository.save(member);
    }

    @Test
    @DisplayName("주문 테스트")
    public void order() {
        Item item = saveItem();
        Member member = saveMember();

        OrderItemRequestDto requestDto = new OrderItemRequestDto();
        requestDto.setCount(10);
        requestDto.setItemNo(item.getNo());

        Long orderNo = orderService.order(requestDto, member);

        Order order = orderRepository.findById(orderNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_EXISTS));
        //Order order = orderService.findOrder(orderNo);

        //List<OrderItem> orderItemList = order.getOrderItemList();//하나

        int totalPrice = requestDto.getCount() * item.getPrice();

        assertEquals(totalPrice, order.getTotalPrice());
    }

    @Test
    @DisplayName("주문 취소 테스트")
    public void cancelOrder() {
        Item item = saveItem();
        Member member = saveMember();

        OrderItemRequestDto requestDto = new OrderItemRequestDto();
        requestDto.setCount(10);
        requestDto.setItemNo(item.getNo());

        Long orderNo = orderService.order(requestDto, member); // 주문 : 100 - 10 = 90

        Order order = orderRepository.findById(orderNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_EXISTS));
        //Order order = orderService.findOrder(orderNo);

        orderService.cancelOrder(orderNo, member); // 주문 취소 : 90 + 10 = 100

        assertEquals(OrderStatus.CANCEL, order.getOrderStatus());
        assertEquals(100, item.getStockNumber());
    }
}
