package com.market.domain.member.dto;

import lombok.Getter;

@Getter
public class FindIdRequestDto {

    private String memberEmail;
    private String memberNickname;
    private String code;
}
