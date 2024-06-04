package com.market.domain.member.email;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class EmailMessageEntity {

    private String to; // 수신자
    private String subject; // 제목
    private String message; // 내용
}
