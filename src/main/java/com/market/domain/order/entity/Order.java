package com.market.domain.order.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.member.entity.Member;
import com.market.domain.order.constant.OrderStatus;
import com.market.domain.order.dto.SaveDeliveryRequestDto;
import com.market.domain.orderItem.entity.OrderItem;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "orders") // order = 예약된 키워드
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_no")
    private Long no;

    private LocalDateTime orderDate; // 주문일

    private LocalDateTime finishDate; // 배송 완료일
    
    private LocalDateTime purchaseCompleteDate; // 구매 확정일

    private LocalDateTime orderCancelDate; // 주문 취소일

    private LocalDateTime returnDate; // 반품 신청일

    private LocalDateTime returnCompleteDate; // 반품 완료일

    private String tid; // 카카오페이 결제고유번호, 결제 승인되면 생성됨 / 결제 취소할 때 해당값 필요

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private OrderStatus orderStatus; // 주문 상태

    private String receiver; // 받는 사람

    private String phone; // 휴대전화번호

    private String deliveryAddr; // 결제 시 선택한 배송지
    
    private String deliveryMessage; // 결제 시 입력한 배송메시지

    private String randomOrderNo; // 랜덤값으로 생성되는 주문 번호
    
    private String returnMessage; // 반품 사유 (소비자 또는 판매자가 작성(판매자가 품절 등의 이유로 주문취소할 경우))
    
    private String paymentMethod; // 결제 수단

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_no")
    private Member member; // 주문한 회원

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItemList = new ArrayList<>(); // 장바구니 페이지에서 한 번에 여러개 주문 가능

    private boolean isCartOrder; // 장바구니 주문 여부

    public void setOder(OrderItem orderItem) {
        orderItem.setOrder(this); // orderItem 객체에 order 객체 세팅(양방향 참조)
    }

    public void setTid(String tid) { // tid(카카오페이 결제고유번호) 저장 메서드
        this.tid = tid;
    }

    public void setDelivery(SaveDeliveryRequestDto saveDeliveryRequestDto) {
        this.receiver = saveDeliveryRequestDto.getReceiver();
        this.phone = saveDeliveryRequestDto.getPhone();
        this.deliveryAddr = saveDeliveryRequestDto.getDeliveryAddr();
        this.deliveryMessage = saveDeliveryRequestDto.getDeliveryMessage();
    }

    public static Order toEntity(Member member, List<OrderItem> orderItemList, Boolean isCartOrder) {
        Order order = Order.builder()
            .member(member)
            .orderItemList(orderItemList)
            .orderStatus(OrderStatus.ORDER)
            .orderDate(LocalDateTime.now())
            .isCartOrder(isCartOrder)
            .randomOrderNo(generateRandomOrderNo())
            .build();
        for (OrderItem orderItem : orderItemList) {
            order.setOder(orderItem);
        }
        return order;
    }

    public int getTotalPrice() {
        return this.orderItemList.stream()
            .mapToInt(OrderItem::getTotalPrice).sum();
    }

    public void cancelOrder(OrderStatus orderStatus, LocalDateTime orderCancelDate, String returnMessage) {
        this.orderStatus = orderStatus;
        this.orderItemList.forEach(OrderItem::cancelOrder);
        this.orderCancelDate = orderCancelDate; // 주문 취소일 설정
        if (orderStatus.equals(OrderStatus.RETURNCOMPLETE)) {
            this.returnCompleteDate = orderCancelDate;
        }
        if (orderStatus.equals(OrderStatus.CANCEL)) {
            this.returnMessage = returnMessage; // 주문취소 시에만 주문취소 사유 설정
        }
    }

    public void setOrderComplete() { // 결제 승인 시 주문 상태 변경
        this.orderStatus = OrderStatus.COMPLETE;
    }
    
    // 주문 상태 변경 메서드 (관리자 또는 판매자가 주문 관리 페이지에서 변경)
    public void changeOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    // 반품 사유 (소비자 또는 판매자가 작성(판매자가 품절 등의 이유로 주문취소할 경우)) 설정
    public void setReturnMessage(String returnMessage) {
        this.returnMessage = returnMessage;
    }
    
    // 배송 완료일 설정
    public void setFinishDate(LocalDateTime finishDate) {
        this.finishDate = finishDate;
    }

    // 구매 확정일 설정
    public void setPurchaseCompleteDate(LocalDateTime purchaseCompleteDate) {
        this.purchaseCompleteDate = purchaseCompleteDate;
    }

    // 반품 신청일 설정
    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    // 반품 완료일 설정
    public void setReturnCompleteDate(LocalDateTime returnCompleteDate) {
        this.returnCompleteDate = returnCompleteDate;
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    // 랜덤 문자열 생성 메서드
    public static String generateRandomOrderNo() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

}
