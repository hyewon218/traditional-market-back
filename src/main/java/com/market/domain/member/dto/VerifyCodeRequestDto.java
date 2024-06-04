package com.market.domain.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyCodeRequestDto {

    private String memberEmail;
    private String code;
}
