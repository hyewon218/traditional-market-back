package com.market.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 시장
    NOT_FOUND_MARKET(HttpStatus.NOT_FOUND, "M001", "존재하지 않는 시장입니다."),
    EXISTED_MARKET(HttpStatus.BAD_REQUEST, "M002", "중복된 시장명입니다."),
    NOT_EXISTS_MARKET_LIKE(HttpStatus.BAD_REQUEST, "M003", "좋아요를 누르지 않은 시장입니다."),
    EXISTS_MARKET_LIKE(HttpStatus.BAD_REQUEST,"M004","이미 좋아요를 누른 시장입니다."),
    NOT_USER_MARKET_UPDATE(HttpStatus.BAD_REQUEST,"M005","작성자만 수정할 수 있습니다."),
    NOT_USER_MARKET_DELETE(HttpStatus.BAD_REQUEST,"M006","작성자만 삭제할 수 있습니다."),
    NOT_FOUND_MARKET_COMMENT(HttpStatus.BAD_REQUEST,"M007","존재하지 않는 댓글입니다."),
    // 상점
    NOT_FOUND_SHOP(HttpStatus.NOT_FOUND, "S001", "존재하지 않는 상점입니다."),
    EXISTED_SHOP(HttpStatus.BAD_REQUEST, "S002", "중복된 상점명입니다."),
    NOT_EXISTS_SHOP_LIKE(HttpStatus.BAD_REQUEST, "S003", "좋아요를 누르지 않은 상점입니다."),
    EXISTS_SHOP_LIKE(HttpStatus.CONFLICT,"S004","이미 좋아요를 누른 상점입니다."),
    NOT_USER_SHOP_UPDATE(HttpStatus.BAD_REQUEST,"S005","작성자만 수정할 수 있습니다."),
    NOT_USER_SHOP_DELETE(HttpStatus.BAD_REQUEST,"S006","작성자만 삭제할 수 있습니다."),
    NOT_FOUND_SHOP_COMMENT(HttpStatus.BAD_REQUEST,"S007","존재하지 않는 댓글입니다."),
    NOT_EXISTS_SELLER(HttpStatus.BAD_REQUEST, "S008", "해당 상점의 사장님이 존재하지 않습니다."),
    // 상품
    NOT_FOUND_ITEM(HttpStatus.NOT_FOUND, "I001", "존재하지 않는 상품입니다."),
    EXISTED_ITEM(HttpStatus.BAD_REQUEST, "I002", "중복된 상품명입니다."),
    NOT_EXISTS_ITEM_LIKE(HttpStatus.BAD_REQUEST, "I003", "좋아요를 누르지 않은 상품입니다."),
    EXISTS_ITEM_LIKE(HttpStatus.CONFLICT,"I004","이미 좋아요를 누른 상품입니다."),
    NOT_USER_ITEM_UPDATE(HttpStatus.BAD_REQUEST,"I005","작성자만 수정할 수 있습니다."),
    NOT_USER_ITEM_DELETE(HttpStatus.BAD_REQUEST,"I006","작성자만 삭제할 수 있습니다."),
    NOT_FOUND_ITEM_COMMENT(HttpStatus.BAD_REQUEST,"I007","존재하지 않는 댓글입니다."),
    // 주문
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "O001", "재고가 존재하지 않습니다."),
    ORDER_NOT_EXISTS(HttpStatus.BAD_REQUEST, "O002", "해당 주문이 존재하지 않습니다."),
    NOT_ORDER_DELETE(HttpStatus.BAD_REQUEST, "O003", "주문 취소 권한이 없습니다."),
    NOT_ORDER(HttpStatus.BAD_REQUEST, "O004", "주문 권한이 없습니다."),
    // 장바구니 상품
    NOT_FOUND_CART_ITEM(HttpStatus.NOT_FOUND, "C001", "장바구니에 존재하지 않는 상품입니다."),
    NOT_AUTHORITY_CART_ITEM(HttpStatus.BAD_REQUEST, "C002", "장바구니 권한이 없습니다."),
    // 알람
    NOTIFICATION_CONNECT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "N001", "notification 연결 에러입니다."),
    // 관리자
    NOT_EXISTS_ADMIN(HttpStatus.BAD_REQUEST, "A002", "관리자가 존재하지 않습니다."),
    // aws s3
    EXISTED_FILE(HttpStatus.BAD_REQUEST, "AS001", "중복된 파일명입니다."),
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
