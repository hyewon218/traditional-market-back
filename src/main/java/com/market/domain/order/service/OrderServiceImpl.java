package com.market.domain.order.service;

import com.market.domain.item.entity.Item;
import com.market.domain.item.repository.ItemRepository;
import com.market.domain.market.entity.Market;
import com.market.domain.market.repository.MarketRepository;
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
import com.market.domain.shop.entity.Shop;
import com.market.domain.shop.repository.ShopRepository;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;

import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepositoryQuery orderRepositoryQuery;
    private final MarketRepository marketRepository;
    private final ShopRepository shopRepository;
    private final ItemRepository itemRepository;

/*    @Override
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
    }*/

    @Override
    @Retryable(
        // 락 획득 실패 시(락 타임아웃, 비관적 락 실패) 재시도를 수행할 예외 클래스 목록
        retryFor = {LockTimeoutException.class, PessimisticLockException.class},
        // 최초 1회 실행 + 실패 시 2회 재시도 = 총 3회 시도 후 실패하면 @Recover 메서드 호출
        maxAttempts = 3,
        // 재시도 사이의 대기 시간을 500ms(0.5초)로 설정하여 부하 분산 및 락 해제 대기
        backoff = @Backoff(delay = 500)
    )
    @Transactional(isolation = Isolation.REPEATABLE_READ) // 트랜잭션 내에서 같은 데이터를 여러 번 읽어도 동일한 결과를 보장
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
    /*@Override
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
    }*/

    /*결제 승인 후*/
    // 비관적 락 적용
    @Override
    @Retryable(
        // 락 획득 실패 시(락 타임아웃, 비관적 락 실패) 재시도를 수행할 예외 클래스 목록
        retryFor = {LockTimeoutException.class, PessimisticLockException.class},
        // 최초 1회 실행 + 실패 시 2회 재시도 = 총 3회 시도 후 실패하면 @Recover 메서드 호출
        maxAttempts = 3,
        // 재시도 사이의 대기 시간을 500ms(0.5초)로 설정하여 부하 분산 및 락 해제 대기
        backoff = @Backoff(delay = 500)
    )
    @Transactional(isolation = Isolation.REPEATABLE_READ) // 트랜잭션 내에서 같은 데이터를 여러 번 읽어도 동일한 결과를 보장
    @Caching(evict = {
        @CacheEvict(cacheNames = "shops", allEntries = true, cacheManager = "marketCacheManager"),
        @CacheEvict(cacheNames = "getTop5Items", allEntries = true, cacheManager = "ItemTop5CacheManager")
    })
    public void completeOrder(Order order) {
        try {
            // 비관적 락으로 엔티티들을 다시 조회
            order = orderRepository.findByIdWithLock(order.getNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER));
            order.setOrderComplete();

            List<OrderItem> orderItemList = order.getOrderItemList();
            for (OrderItem orderItem : orderItemList) {
                Item item = itemRepository.findByIdWithLock(orderItem.getItem().getNo())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ITEM));
                Shop shop = shopRepository.findByIdWithLock(item.getShop().getNo())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_SHOP));
                Market market = marketRepository.findByIdWithLock(shop.getMarket().getNo())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MARKET));

                item.decreaseStock(orderItem.getCount()); // 주문 수량만큼 재고 차감
                item.addCountSales(orderItem.getCount()); // 상품 판매량 증가 및 총 매출액 증가
                shop.setTotalSalesPrice(orderItem.getTotalPrice()); // 해당 상점의 모든 상품의 총 매출액 합계 설정
                market.setTotalSalesPrice(orderItem.getTotalPrice()); // 해당 시장의 모든 상품의 총 매출액 합계 설정
            }
        } catch (LockTimeoutException | PessimisticLockException e) {
            log.warn("주문번호 {}의 처리 중 락 타임아웃이 발생했습니다. 재시도를 진행합니다. 에러 메시지: {}",
                order.getNo(),
                e.getMessage());
            throw e;
        }
    }

    /*
     * @Recover 메서드: @Retryable 이 지정된 최대 재시도 횟수를 모두 실패한 경우 최종적으로 호출되는 복구 메서드
     * 동작 과정:
     * 1. @Retryable 메서드가 지정된 횟수(maxAttempts)만큼 재시도 실패
     * 2. 마지막 실패 후 자동으로 이 메서드가 호출됨
     * 3. 에러 로그를 기록하고 비즈니스 예외를 발생시켜 상위 레이어에서 처리하도록 함
     * @param e 재시도 실패의 원인이 된 마지막 예외
     * @param order 처리하려고 했던 주문 객체
     * @throws BusinessException 주문 처리 실패를 나타내는 비즈니스 예외
     */
    @Recover
    public void recoverOrderComplete(Exception e, Order order) {
        log.error("주문번호 {}의 처리가 3회 재시도 후에도 실패했습니다. 최종 에러: {}",
            order.getNo(), e.getMessage());
        throw new BusinessException(ErrorCode.ORDER_PROCESSING_FAILED);
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
