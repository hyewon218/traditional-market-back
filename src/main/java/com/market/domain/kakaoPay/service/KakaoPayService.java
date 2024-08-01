package com.market.domain.kakaoPay.service;

import com.market.domain.cartItem.service.CartItemService;
import com.market.domain.kakaoPay.config.KakaoPayProperties;
import com.market.domain.kakaoPay.dto.cancel.CancelResponseDto;
import com.market.domain.kakaoPay.dto.payment.ApproveResponseDto;
import com.market.domain.kakaoPay.dto.payment.ReadyResponseDto;
import com.market.domain.member.entity.Member;
import com.market.domain.order.entity.Order;
import com.market.domain.order.service.OrderServiceImpl;
import com.market.domain.orderItem.entity.OrderItem;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoPayService {

    private final KakaoPayProperties kakaoPayProperties;
    private final OrderServiceImpl orderService;
    private final CartItemService cartItemService;

    // 결제 요청
    @Transactional
    public ReadyResponseDto kakaoPayReady(Member member) {
        // 로그인 사용자 정보로 가장 최근 주문 가져오기
        Order order = orderService.getFirstOrderByMemberNo(member);

        // 주문 상품 목록 가져오기
        List<OrderItem> orderItems = order.getOrderItemList();

        // 주문 상품이 없을 경우 예외 처리
        if (orderItems == null || orderItems.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ORDER_ITEM);
        }

        String orderId = member.getMemberId() + order.getNo();

        // 카카오 결제 요청 파라미터 설정
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();

        // 주문 상품이 1개 이상일 경우(한 상품의 갯수 말고 각각 다른 상품이 1개 이상일 경우)
        if (orderItems.size() > 1) {
            // 상품 정보 리스트 초기화
            List<Map<String, Object>> itemList = new ArrayList<>();
            int totalPrice = 0;
            int totalQuantity = 0;

            for (OrderItem orderItem : orderItems) {
                Map<String, Object> item = new LinkedHashMap<>();
                // 각 상품의 코드, 이름, 수량, 가격 추가
                item.put("item_name", orderItem.getItem().getItemName());
                item.put("item_code", orderItem.getItem().getNo());
                item.put("quantity", orderItem.getCount());
                item.put("total_amount", orderItem.getTotalPrice());

                // 총 합계 금액 및 수량 계산
                totalPrice += orderItem.getTotalPrice();
                totalQuantity += orderItem.getCount();

                // 상품 정보 리스트에 추가
                itemList.add(item);
            }
            param.add("cid", kakaoPayProperties.getCid());
            param.add("partner_order_id", orderId);
            param.add("partner_user_id", member.getMemberId());
            param.add("item_name",
                itemList.get(0).get("item_name") + " 외 " + (orderItems.size() - 1) + "개");

            // 개별 상품 코드 추가 (StringJoiner를 사용하여 모든 item_code를 하나의 문자열로 합침)
            StringJoiner itemCodes = new StringJoiner(", ");
            for (Map<String, Object> item : itemList) {
                itemCodes.add(item.get("item_code").toString());
            }
            param.add("item_code", itemCodes.toString());

            param.add("quantity", totalQuantity);
            param.add("total_amount", totalPrice);
            param.add("tax_free_amount", 0);
            param.add("approval_url", "http://localhost:8080/api/payment/success");
            param.add("cancel_url", "http://localhost:8080/api/payment/cancel");
            param.add("fail_url", "http://localhost:8080/api/payment/fail");

            // 주문 상품이 1개일 경우
        } else {
            OrderItem orderItem = orderItems.get(0);
            param.add("cid", kakaoPayProperties.getCid());
            param.add("partner_order_id", orderId);
            param.add("partner_user_id", member.getMemberId());
            param.add("item_name", orderItem.getItem().getItemName());
            param.add("item_code", orderItem.getItem().getNo()); // 없어도 됨
            param.add("quantity", orderItem.getCount());
            param.add("total_amount", orderItem.getTotalPrice());
            param.add("tax_free_amount", 0);
            param.add("approval_url", "http://localhost:8080/api/payment/success");
            param.add("cancel_url", "http://localhost:8080/api/payment/cancel");
            param.add("fail_url", "http://localhost:8080/api/payment/fail");
        }

        // 파라미터, 헤더
        HttpEntity<String> requestEntity = new HttpEntity<>(convertMultiValueMapToJson(param),
            this.getHeaders());

        // 파라미터 로그
        log.info("카카오페이 요청 파라미터 : " + convertMultiValueMapToJson(param));

        // 외부에 보낼 url
        RestTemplate restTemplate = new RestTemplate();
        ReadyResponseDto readyResponseDto = restTemplate.postForObject(
            "https://open-api.kakaopay.com/online/v1/payment/ready",
            requestEntity,
            ReadyResponseDto.class);

        // tid 추출해서 Order 에 저장
        if (readyResponseDto != null && readyResponseDto.getTid() != null) {
            order.setTid(readyResponseDto.getTid());
        }
        return readyResponseDto;
    }

    // 결제 승인
    @Transactional
    public ApproveResponseDto kakaoPayApprove(String pgToken, Member member) {
        // 로그인 사용자 정보로 가장 최근 주문 가져오기
        Order order = orderService.getFirstOrderByMemberNo(member);

        String orderId = member.getMemberId() + order.getNo();

        // 카카오 요청
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("cid", kakaoPayProperties.getCid());
        param.add("tid", order.getTid());
        param.add("partner_order_id", orderId);
        param.add("partner_user_id", member.getMemberId());
        param.add("pg_token", pgToken);

        // 파라미터, 헤더
        HttpEntity<String> requestEntity = new HttpEntity<>(convertMultiValueMapToJson(param),
            this.getHeaders());

        // 외부에 보낼 url
        RestTemplate restTemplate = new RestTemplate();
        ApproveResponseDto approveResponseDto = restTemplate.postForObject(
            "https://open-api.kakaopay.com/online/v1/payment/approve",
            requestEntity,
            ApproveResponseDto.class);

        // 장바구니에서 주문한 경우에만 장바구니 상품 삭제
        if (order.isCartOrder()) {
            cartItemService.deleteAllCartItems(member);
        }
        // 주문 상태 COMPLETE 으로 변경 및 주문 상태 ORDER 인 주문 상품 목록 재고 증가 후 주문 목록 삭제
        orderService.afterPayApprove(member, order);

        return approveResponseDto;
    }

    // 결제 환불
    @Transactional
    public CancelResponseDto kakaoPayCancel(Long orderNo) {
        // 주분번호로 주문 조회
        Order order = orderService.findById(orderNo);

        int total_amount = 0;

        List<OrderItem> orderItems = order.getOrderItemList();
        if (orderItems != null) {
            for (OrderItem orderItem : orderItems) {
                total_amount += orderItem.getTotalPrice();
            }
        }

        // 카카오 요청
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("cid", kakaoPayProperties.getCid());
        param.add("tid", order.getTid());
        param.add("cancel_amount", total_amount);
        // 승인시 vat_amount 를 보냈다면 취소시에도 동일하게 요청, 승인과 동일하게 요청 시 값을 전달하지 않을 경우 자동계산
        param.add("cancel_tax_free_amount", 0);

        // 파라미터, 헤더
        HttpEntity<String> requestEntity = new HttpEntity<>(convertMultiValueMapToJson(param),
            this.getHeaders());

        // 외부에 보낼 url
        RestTemplate restTemplate = new RestTemplate();
        CancelResponseDto cancelResponseDto = restTemplate.postForObject(
            "https://open-api.kakaopay.com/online/v1/payment/cancel",
            requestEntity,
            CancelResponseDto.class);

        return cancelResponseDto;
    }

    // 카카오 요청 헤더값
    private HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        String auth = "SECRET_KEY " + kakaoPayProperties.getSecret_key();
        httpHeaders.set("Authorization", auth);
        httpHeaders.set("Content-Type", "application/json");
        return httpHeaders;
    }

    // MultiValueMap 을 JSON 문자열로 변환하는 메서드
    private String convertMultiValueMapToJson(MultiValueMap<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        map.forEach((key, value) -> {
            if (value != null) {
                if (value.size() == 1) {
                    json.append(String.format("\"%s\":%s,", key, convertValueToJson(
                        ((List<?>) value).get(0))));
                } else {
                    json.append(String.format("\"%s\":%s,", key, value));
                }
            } else {
                json.append(String.format("\"%s\":%s,", key, convertValueToJson(value)));
            }
        });
        json.deleteCharAt(json.length() - 1); // 마지막 쉼표 제거
        json.append("}");
        return json.toString();
    }

    // 값 형식에 따라 JSON 문자열로 변환하는 메서드
    private String convertValueToJson(Object value) {
        if (value instanceof String) {
            return String.format("\"%s\"", value);
        } else {
            return value.toString();
        }
    }
}
