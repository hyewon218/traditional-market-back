package com.market.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 시장
    NOT_FOUND_MARKET(HttpStatus.BAD_REQUEST, "M001", "존재하지 않는 시장입니다."),
    EXISTED_MARKET(HttpStatus.BAD_REQUEST, "M001", "중복된 시장명입니다."),
    // 상점
    NOT_FOUND_SHOP(HttpStatus.BAD_REQUEST, "S001", "존재하지 않는 상점입니다."),
    EXISTED_SHOP(HttpStatus.BAD_REQUEST, "M001", "중복된 상점명입니다."),
    // aws s3
    EXISTED_FILE(HttpStatus.BAD_REQUEST, "S001", "중복된 파일명입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
