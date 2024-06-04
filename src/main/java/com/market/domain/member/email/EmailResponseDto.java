package com.market.domain.member.email;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailResponseDto {

    // 전송된 인증번호
    private String code;
}