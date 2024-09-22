package com.market.domain.order.service;

import com.market.domain.item.entity.Item;
import com.market.domain.item.repository.ItemRepository;
import com.market.domain.member.entity.Member;
import com.market.domain.order.constant.OrderStatus;
import com.market.domain.order.dto.OrderHistResponseDto;
import com.market.domain.order.dto.SaveDeliveryRequestDto;
import com.market.domain.order.entity.Order;
import com.market.domain.order.repository.OrderRepository;
import com.market.domain.order.repository.OrderRepositoryQuery;
import com.market.domain.order.repository.OrderSearchCond;
import com.market.domain.orderItem.dto.OrderItemHistResponseDto;
import com.market.domain.orderItem.dto.OrderItemRequestDto;
import com.market.domain.orderItem.entity.OrderItem;
import com.market.domain.orderItem.repository.OrderItemRepository;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepositoryQuery orderRepositoryQuery;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public Long order(OrderItemRequestDto orderItemDto, Member member) {
        // 선택한 상품 주문
        Item item = itemRepository.findByIdWithLock(orderItemDto.getItemNo()).orElseThrow(
            () -> new BusinessException(ErrorCode.NOT_FOUND_ITEM)
        );
        // 이전 주문(결제하지 않은, 주문 상태 ORDER) 이 있는지 확인
        Order existOrder = getStatusOrder(member);
        if (existOrder != null) { // 이전 주문이 있으면 삭제
            orderRepository.delete(existOrder);
        }
        List<OrderItem> orderItemList = new ArrayList<>(); // 주문 상품 담는 리스트
        orderItemList.add(orderItemDto.toEntity(item)); // (상품 담아) 주문 상품 생성
        Order order = Order.toEntity(member, orderItemList, false); // (주문 상품 담아) 주문 생성
        orderRepository.save(order); // 주문 저장
        return order.getNo();
    }

    @Override
    @Transactional(readOnly = true)
    public Order getFirstOrderByMemberNo(Member member) { // 가장 최근 주문 찾기 (주문페이지)
        return orderRepositoryQuery.findLatestOrder(member.getMemberNo(), OrderStatus.ORDER)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_EXISTS));
    }

    @Override
    @Transactional(readOnly = true) // 주문 내 상품 목록 조회
    public List<OrderItemHistResponseDto> getOrderItemList(Member member) {
        Order order = getFirstOrderByMemberNo(member); // 로그인한 member 정보로 가장 최근 order 정보 가져오기
        List<OrderItem> orderItemList = orderItemRepository.findAllByOrderNo(order.getNo());
        return orderItemList.stream().map(OrderItemHistResponseDto::of).toList();
    }

    @Override
    @Transactional // 결제 요청 시 주문 테이블에 배송지 저장
    public void setDeliveryAddr(Member member, SaveDeliveryRequestDto saveDeliveryRequestDto) {
        Order order = getFirstOrderByMemberNo(member);
        order.setDelivery(saveDeliveryRequestDto);
    }

    @Override
    @Transactional(readOnly = true) // 가장 최근 COMPLETE 주문 조회 (결제 완료 후 주문 상세 정보 조회 시 사용)
    public OrderHistResponseDto getLatestOrder(Member member) {
        Order order = orderRepositoryQuery.findLatestOrder(member.getMemberNo(),
                OrderStatus.COMPLETE)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_EXISTS));
        return OrderHistResponseDto.of(order);
    }

    @Override
    @Transactional(readOnly = true) // 본인의 CANCEL, ORDER 제외한 모든 주문상태 주문 목록 조회
    public Page<OrderHistResponseDto> getOrders(Member member, Pageable pageable) {
        // 회원 및 주문 데이터 조회
        Page<Order> orderList = orderRepositoryQuery.findOrders(member.getMemberNo(), pageable);
        return orderList.map(OrderHistResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 본인의 CANCEL 주문 목록 조회
    public Page<OrderHistResponseDto> getCancelOrders(Member member, Pageable pageable) {
        // 회원 및 주문 데이터 조회
        Page<Order> orderList = orderRepositoryQuery.findCancelOrders(member.getMemberNo(),
            pageable);
        return orderList.map(OrderHistResponseDto::of);
    }

    @Transactional(readOnly = true) // CANCEL, ORDER 제외한 모든 주문 목록 조회(관리자만 가능)
    public Page<OrderHistResponseDto> getOrdersAdmin(Pageable pageable) {
        Page<Order> orderList = orderRepositoryQuery.findOrdersByAdminExcludingCanceled(pageable);
        return orderList.map(OrderHistResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 주문 상태별 조회(관리자만 가능)
    public Page<OrderHistResponseDto> getOrderStatusAdmin(OrderStatus orderStatus,
        Pageable pageable) {
        Page<Order> orderList = orderRepositoryQuery.findOrdersByAdminAndOrderStatus(orderStatus, pageable);
        return orderList.map(OrderHistResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // CANCEL, ORDER 제외한 판매자가 자신이 소유한 상점의 상품들에 대한 주문 목록 조회 (판매자만 가능)
    public Page<OrderHistResponseDto> getOrdersSeller(Member seller, Pageable pageable) {
        Page<Order> orderList = orderRepositoryQuery.findOrdersBySellerExcludingCanceled(
            seller.getMemberNo(), pageable);
        return orderList.map(OrderHistResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 판매자가 자신이 소유한 상점의 상품들에 대한 CANCEL 주문 목록 조회 (판매자만 가능)
    public Page<OrderHistResponseDto> getCancelOrdersSeller(Member seller, OrderStatus orderStatus,
        Pageable pageable) {
        Page<Order> orderList = orderRepositoryQuery.findOrdersBySellerAndOrderStatus(
            seller.getMemberNo(), orderStatus, pageable);
        return orderList.map(OrderHistResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 주문 목록 검색 (관리자 주문 관리 페이지에서 사용)
    public Page<OrderHistResponseDto> searchOrders(OrderSearchCond cond, Pageable pageable) {
        return orderRepositoryQuery.searchOrders(cond, pageable).map(OrderHistResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 주문 목록 검색 (판매자 주문 관리 페이지에서 사용)
    public Page<OrderHistResponseDto> searchOrdersSeller(Member seller, OrderSearchCond cond,
        Pageable pageable) {
        return orderRepositoryQuery.searchOrdersSeller(seller.getMemberNo(), cond, pageable)
            .map(OrderHistResponseDto::of);
    }

    /*결제 승인 후*/
    @Override
    @Transactional // 주문 상태 COMPLETE 로 변경, COMPLETE 시 재고 감소
    public void setOrderComplete(Order order) {
        order.setOrderComplete();
        // 각 상품 판매량 및 총 판매액, 그리고 상점의 총 매출액 증가
        List<OrderItem> orderItemList = order.getOrderItemList();
        for (OrderItem orderItem : orderItemList) {
            orderItem.getItem().decreaseStock(orderItem.getCount()); // 주문상품 개수 상품 재고에서 차감
            orderItem.getItem().addCountSales(orderItem.getCount()); // 상품마다 판매량 및 매출액 설정
            orderItem.getItem().getShop().setTotalSalesPrice(
                orderItem.getTotalPrice()); // 상품 매출액을 해당 상품을 보유한 상점의 총매출액에 합산
            orderItem.getItem().getShop().getMarket().setTotalSalesPrice(
                orderItem.getTotalPrice()); // 상품 매출액을 해당 상품을 보유한 상점을 소유하고 있는 시장의 총매출액에 합산
        }
    }

    @Override
    @Transactional(readOnly = true) // 주문 상태 ORDER 주문 조회
    public Order getStatusOrder(Member member) {
        return orderRepositoryQuery.findOrder(member.getMemberNo(), OrderStatus.ORDER);
    }

    @Override  //스케줄러로 주기적으로 삭제
    @Transactional // 주문 상태 ORDER 인 주문 목록 일괄 삭제
    public void deleteOrdersInBatches(OrderStatus status, int batchSize) {
        int deletedCount = 0;
        int batchDeleted;

        long startTime = System.currentTimeMillis();
        log.info("Starting deletion of orders with status {} in batches of {}", status, batchSize);

        try {
            do {
                batchDeleted = orderRepository.deleteBatchByStatus(status); // 주문 삭제
                deletedCount += batchDeleted;

                log.info("Deleted batch of {} orders", batchDeleted);
            } while (batchDeleted >= batchSize);

            // Record the end time
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("Successfully deleted {} orders with status {}. Time taken: {} ms",
                deletedCount, status, duration);
        } catch (Exception e) {
            log.error("An error occurred while deleting orders: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional // 주문 취소
    public void cancelOrder(Long orderNo, OrderStatus orderStatus, Member member,
        String returnMessage) {
        Order order = findById(orderNo);
        validateOrder(orderNo, member);

        // 각 상품 판매량 및 총판매액 감소
        List<OrderItem> orderItemList = order.getOrderItemList();
        for (OrderItem orderItem : orderItemList) {
            orderItem.getItem().removeCountSales(orderItem.getCount());
            orderItem.getItem().getShop()
                .minusTotalSalesPrice(orderItem.getTotalPrice());
            orderItem.getItem().getShop().getMarket()
                .minusTotalSalesPrice(orderItem.getTotalPrice());
        }
        order.cancelOrder(orderStatus, LocalDateTime.now(), returnMessage);
    }

    @Override
    @Transactional // 주문 검증
    public void validateOrder(Long orderNo, Member member) {
        boolean exists = orderRepository.existsByNoAndMember_MemberNo(orderNo,
            member.getMemberNo());
        if (!exists) {
            throw new BusinessException(ErrorCode.NOT_AUTHORITY_ORDER);
        }
    }

    @Override
    @Transactional(readOnly = true) // 특정 주문 조회
    public Order findById(Long orderNo) {
        return orderRepository.findById(orderNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_EXISTS));
    }

    @Override
    @Transactional // 주문 삭제
    public void deleteMyOrder(Member member, Long orderNo) {
        Order order = findById(orderNo);
        validateOrder(orderNo, member);
        List<OrderItem> orderItemList = orderItemRepository.findAllByOrderNo(orderNo);
        orderItemRepository.deleteAll(orderItemList);
        orderRepository.deleteById(order.getNo());
    }

    @Override
    @Transactional // 주문 상태 변경 (관리자 또는 판매자가 주문 관리에서 주문 상태 변경)
    public void changeOrderState(Long orderNo, OrderStatus orderStatus, String returnMessage) {
        Order order = findById(orderNo);
        order.changeOrderStatus(orderStatus);
        if (orderStatus.equals(OrderStatus.FINISH)) { // 배송 완료로 변경 시 배송 완료일 설정
            order.setFinishDate(LocalDateTime.now());
        } else if (orderStatus.equals(OrderStatus.PURCHASECONFIRM)) {
            order.setPurchaseCompleteDate(LocalDateTime.now()); // 구매 확정으로 변경 시 구매 확정일 설정
        } else if (orderStatus.equals(OrderStatus.RETURN)) {
            order.setReturnDate(LocalDateTime.now()); // 반품 신청으로 변경 시 반품 신청일 설정
            order.setReturnMessage(returnMessage); // 반품 사유 설정
        } else if (orderStatus.equals(OrderStatus.RETURNCOMPLETE)) {
            order.setReturnCompleteDate(LocalDateTime.now()); // 반품 완료로 변경 시 반품 완료일 설정
        }
    }

    // 매일 0시 10분에 실행, 주문 상태가 배송완료 (FINISH)면서 배송완료일 (finishDate)이 14일 이후인것 찾아서 자동 구매확정
    @Scheduled(cron = "0 10 0 * * ?") // 스케줄러 동시 실행될 경우 자원 경합 문제로 시간 나눠서 설정
    @Transactional
    public void autoPurchaseConfirm() {
        LocalDateTime fourteenDaysAgo = LocalDateTime.now().minusDays(14);
        List<Order> orders = orderRepository.findAllByOrderStatusAndFinishDateBefore(
            OrderStatus.FINISH, fourteenDaysAgo);
        for (Order order : orders) {
            order.changeOrderStatus(OrderStatus.PURCHASECONFIRM); // 구매 확정 상태 설정
            order.setPurchaseCompleteDate(LocalDateTime.now()); // 구매 확정일 설정
            orderRepository.save(order);
        }
    }

}
