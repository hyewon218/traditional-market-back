package com.market.domain.kakaoPay.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadyResponseDto {

    private String tid; // 결제 고유번호
    private String next_redirect_pc_url; // 요청 클라이언트가 pc일 경우 받는 결제 페이지
    private String next_redirect_mobile_url; // 요청 클라이언트가 모바일웹일 경우 받는 결제 페이지
    private String next_redirect_app_url; // 요청 클라이언트가 앱일 경우 받는 결제 페이지
    private String created_at;
}
