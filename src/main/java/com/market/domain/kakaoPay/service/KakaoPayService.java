package com.market.domain.kakaoPay.service;

import com.market.domain.kakaoPay.config.KakaoPayProperties;
import com.market.domain.kakaoPay.dto.cancel.CancelResponseDto;
import com.market.domain.kakaoPay.dto.payment.ApproveResponseDto;
import com.market.domain.kakaoPay.dto.payment.ReadyResponseDto;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.domain.order.entity.Order;
import com.market.domain.order.repository.OrderRepository;
import com.market.domain.orderItem.entity.OrderItem;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoPayService {

    private final KakaoPayProperties kakaoPayProperties;
    private final OrderRepository orderRepository;

    private String tid;

    // 우선 order 빼고 임의값 넣어서 테스트 해보기
    // 결제 요청
    @Transactional
    public ReadyResponseDto kakaoPayReady(Member member, Order order) {

        String itemName = null;
        int quantity = 0;
        int total_amount = 0;

        List<OrderItem> orderItems = order.getOrderItemList();
        if(orderItems != null) {
            for(OrderItem orderItem : orderItems) {
                itemName = orderItem.getItem().getItemName();
                quantity = orderItem.getCount();
                total_amount = orderItem.getTotalPrice();
            }
        }

        String orderId = member.getMemberId() + order.getNo();

        // 카카오 요청
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("cid", kakaoPayProperties.getCid());
        param.add("partner_order_id", orderId);
        param.add("partner_user_id", member.getMemberId());
        param.add("item_name", itemName);
        param.add("quantity", quantity);
        param.add("total_amount", total_amount);
        param.add("tax_free_amount", 0);
        param.add("approval_url", "http://localhost:8080/api/payment/success");
        param.add("cancel_url", "http://localhost:8080/api/payment/cancel");
        param.add("fail_url", "http://localhost:8080/api/payment/fail");

        // 파라미터, 헤더
        HttpEntity<String> requestEntity = new HttpEntity<>(convertMultiValueMapToJson(param), this.getHeaders());

        // 파라미터 로그
        log.info("카카오페이 요청 파라미터 : " + convertMultiValueMapToJson(param));

        // 외부에 보낼 url
        RestTemplate restTemplate = new RestTemplate();
        ReadyResponseDto readyResponseDto = restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/ready", // 안되면 이걸로 해보기 "https://kapi.kakao.com/v1/payment/ready"
                requestEntity,
                ReadyResponseDto.class);

        // tid 추출해서 필드에 저장
//        this.tid = readyResponseDto.getTid();

        // tid 추출해서 Order에 저장
        if (readyResponseDto.getTid() != null) {
            order.setTid(readyResponseDto.getTid());
            orderRepository.save(order);
        }

        return readyResponseDto;
    }

//    @Transactional
//    public ReadyResponseDto kakaoPayReady(Member member) {
//
//        // 카카오 요청
//        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
//        param.add("cid", kakaoPayProperties.getCid());
//        param.add("partner_order_id", "partner_order_id");
//        param.add("partner_user_id", "paytest");
//        param.add("item_name", "소고기");
//        param.add("quantity", 1); // 예시로 상품 수량을 정수로 전달
//        param.add("total_amount", 10000); // 예시로 가격을 정수로 전달
//        param.add("tax_free_amount", 0); // 예시로 비과세금액을 정수로 전달
//        param.add("approval_url", "http://localhost:8080/api/payment/success");
//        param.add("cancel_url", "http://localhost:8080/api/payment/cancel");
//        param.add("fail_url", "http://localhost:8080/api/payment/fail");
//
//        // 파라미터, 헤더
//        HttpEntity<String> requestEntity = new HttpEntity<>(convertMultiValueMapToJson(param), this.getHeaders());
//
//        // 파라미터 로그
//        log.info("카카오페이 요청 파라미터 : " + convertMultiValueMapToJson(param));
//
//        // 외부에 보낼 url
//        RestTemplate restTemplate = new RestTemplate();
//        ReadyResponseDto readyResponseDto = restTemplate.postForObject(
//                "https://open-api.kakaopay.com/online/v1/payment/ready",
//                requestEntity,
//                ReadyResponseDto.class);
//
//        // tid 추출해서 필드에 저장
//        this.tid = readyResponseDto.getTid();
//
//        return readyResponseDto;
//    }

    // 결제 승인
    @Transactional
    public ApproveResponseDto kakaoPayApprove(String pgToken, Member member, Order order) {

        String orderId = member.getMemberId() + order.getNo();

        // 카카오 요청
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("cid", kakaoPayProperties.getCid());
        param.add("tid", order.getTid());
        param.add("partner_order_id", orderId);
        param.add("partner_user_id", member.getMemberId());
        param.add("pg_token", pgToken);

        // 파라미터, 헤더
        HttpEntity<String> requestEntity = new HttpEntity<>(convertMultiValueMapToJson(param), this.getHeaders());

        // 외부에 보낼 url
        RestTemplate restTemplate = new RestTemplate();
        ApproveResponseDto approveResponseDto = restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/approve",
                requestEntity,
                ApproveResponseDto.class);

        return approveResponseDto;
    }

    // 결제 환불
    @Transactional
    public CancelResponseDto kakaoPayRefund(Member member, Order order) {

        int total_amount = 0;

        List<OrderItem> orderItems = order.getOrderItemList();
        if(orderItems != null) {
            for(OrderItem orderItem : orderItems) {
                total_amount = orderItem.getTotalPrice();
            }
        }

        // 카카오 요청
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("cid", kakaoPayProperties.getCid());
        param.add("tid", order.getTid());
        param.add("cancel_amount", total_amount);
        // 승인시 vat_amount를 보냈다면 취소시에도 동일하게 요청, 승인과 동일하게 요청 시 값을 전달하지 않을 경우 자동계산
        param.add("cancel_tax_free_amount", 0);

        // 파라미터, 헤더
        HttpEntity<String> requestEntity = new HttpEntity<>(convertMultiValueMapToJson(param), this.getHeaders());

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

    // MultiValueMap을 JSON 문자열로 변환하는 메서드
    private String convertMultiValueMapToJson(MultiValueMap<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        map.forEach((key, value) -> {
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                if (list.size() == 1) {
                    json.append(String.format("\"%s\":%s,", key, convertValueToJson(list.get(0))));
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
