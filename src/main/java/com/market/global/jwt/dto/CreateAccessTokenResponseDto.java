package com.market.global.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreateAccessTokenResponseDto {

    private String accessToken;
}
