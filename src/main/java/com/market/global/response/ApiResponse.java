package com.market.global.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse {

    private String message;
    private Integer statusCode;

    public ApiResponse(String massage, Integer statusCode) {
        this.message = massage;
        this.statusCode = statusCode;
    }
}