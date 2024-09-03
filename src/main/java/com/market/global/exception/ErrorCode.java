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
    NOT_ALLOW_PROFANITY_MARKET(HttpStatus.BAD_REQUEST, "M008", "비속어가 포함된 메시지는 작성할수없습니다."),
    // 상점
    NOT_FOUND_SHOP(HttpStatus.NOT_FOUND, "S001", "존재하지 않는 상점입니다."),
    EXISTED_SHOP(HttpStatus.BAD_REQUEST, "S002", "중복된 상점명입니다."),
    NOT_EXISTS_SHOP_LIKE(HttpStatus.BAD_REQUEST, "S003", "좋아요를 누르지 않은 상점입니다."),
    EXISTS_SHOP_LIKE(HttpStatus.CONFLICT, "S004", "이미 좋아요를 누른 상점입니다."),
    NOT_USER_SHOP_UPDATE(HttpStatus.BAD_REQUEST, "S005", "작성자만 수정할 수 있습니다."),
    NOT_USER_SHOP_DELETE(HttpStatus.BAD_REQUEST, "S006", "작성자만 삭제할 수 있습니다."),
    NOT_FOUND_SHOP_COMMENT(HttpStatus.BAD_REQUEST, "S007", "존재하지 않는 댓글입니다."),
    NOT_EXISTS_SELLER(HttpStatus.BAD_REQUEST, "S008", "해당 상점의 판매자가 등록되어 있지 않습니다."),
    NOT_FOUND_CATEGORY_SHOPS(HttpStatus.NOT_FOUND, "S009", "카테고리에 대한 시장 내 상점 목록이 존재하지 않습니다."),
    NOT_ALLOW_PROFANITY_SHOP(HttpStatus.BAD_REQUEST, "S010", "비속어가 포함된 댓글은 작성할 수 없습니다."),
    ONLY_ADMIN_HAVE_AUTHORITY_ON_SHOP(HttpStatus. BAD_REQUEST, "S011", "관리자만 권한이 있습니다."),
    ONLY_SELLER_HAVE_AUTHORITY_ON_SHOP(HttpStatus.BAD_REQUEST, "S012", "해당 상점의 판매자만 권한이 있습니다."),
    // 상품
    NOT_FOUND_ITEM(HttpStatus.NOT_FOUND, "I001", "존재하지 않는 상품입니다."),
    EXISTED_ITEM(HttpStatus.BAD_REQUEST, "I002", "중복된 상품명입니다."),
    NOT_EXISTS_ITEM_LIKE(HttpStatus.BAD_REQUEST, "I003", "좋아요를 누르지 않은 상품입니다."),
    EXISTS_ITEM_LIKE(HttpStatus.CONFLICT, "I004", "이미 좋아요를 누른 상품입니다."),
    NOT_USER_ITEM_UPDATE(HttpStatus.BAD_REQUEST, "I005", "작성자만 수정할 수 있습니다."),
    NOT_USER_ITEM_DELETE(HttpStatus.BAD_REQUEST, "I006", "작성자만 삭제할 수 있습니다."),
    NOT_FOUND_ITEM_COMMENT(HttpStatus.BAD_REQUEST, "I007", "존재하지 않는 댓글입니다."),
    NOT_FOUND_CATEGORY_ITEMS(HttpStatus.NOT_FOUND, "I008", "카테고리에 대한 시장 내 상품 목록이 존재하지 않습니다."),
    NOT_FOUND_ITEMS(HttpStatus.NOT_FOUND, "I009", "해당 상품명에 대한 시장 내 상품 목록이 존재하지 않습니다."),
    NOT_ALLOW_PROFANITY_ITEM(HttpStatus.BAD_REQUEST, "I010", "비속어가 포함된 댓글은 작성할 수 없습니다."),
    // 주문
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "O001", "재고가 존재하지 않습니다."),
    ORDER_NOT_EXISTS(HttpStatus.BAD_REQUEST, "O002", "해당 주문이 존재하지 않습니다."),
    NOT_AUTHORITY_ORDER_DELETE(HttpStatus.BAD_REQUEST, "O003", "주문 취소 권한이 없습니다."),
    NOT_AUTHORITY_ORDER(HttpStatus.BAD_REQUEST, "O004", "주문 권한이 없습니다."),
    NOT_FOUND_RECENT_ORDER(HttpStatus.BAD_REQUEST, "O005", "해당 회원의 최근 주문을 찾을 수 없습니다."),
    NOT_FOUND_ORDER_ITEM(HttpStatus.BAD_REQUEST, "O006", "주문할 상품이 존재하지 않습니다."),
    INVALID_ACCESS(HttpStatus.BAD_REQUEST, "0007", "잘못된 접근입니다"),
    NOT_LESS_THAN_ZERO(HttpStatus.BAD_REQUEST, "O008", "재고는 0개 미만이 될 수 없습니다."),
    // 장바구니
    NOT_FOUND_CART(HttpStatus.NOT_FOUND, "C001", "장바구니가 존재하지 않습니다."),
    // 장바구니 상품
    NOT_FOUND_CART_ITEM(HttpStatus.NOT_FOUND, "CI001", "장바구니에 존재하지 않는 상품입니다."),
    NOT_AUTHORITY_CART_ITEM(HttpStatus.BAD_REQUEST, "CI002", "장바구니 권한이 없습니다."),
    // 배송지
    NOT_FOUND_DELIVERY(HttpStatus.NOT_FOUND, "D001", "해당 배송지가 존재하지 않습니다."),
    NOT_FOUND_PRIMARY_DELIVERY(HttpStatus.NOT_FOUND, "D002", "기본 배송지가 존재하지 않습니다."),
    NOT_FOUND_SELECTED_DELIVERY(HttpStatus.NOT_FOUND, "D003", "선택된 배송지가 존재하지 않습니다."),
    NOT_AUTHORITY_DELIVERY(HttpStatus.BAD_REQUEST, "D004", "배송지 권한이 없습니다."),
    // 알람
    NOTIFICATION_CONNECT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "N001", "notification 연결 에러입니다."),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "N002", "수신자 역할이 올바르지 않습니다."),
    NOT_FOUND_NOTIFICATION(HttpStatus.BAD_REQUEST, "N003", "존재하지 않는 알람입나다."),
    NOT_RECEIVER_FOR_NOTIFICATION(HttpStatus.BAD_REQUEST, "N004", "알람에 대한 수신자가 아닙니다.."),
    // 관리자
    NOT_EXISTS_ADMIN(HttpStatus.BAD_REQUEST, "A001", "관리자가 존재하지 않습니다."),
    // 채팅
    INVALID_AUTH_TOKEN(HttpStatus.BAD_REQUEST, "C001", "잘못된 인증 토큰입니다."),
    NOT_FOUND_CHATROOM(HttpStatus.BAD_REQUEST, "C002", "존재하지 않는 채팅방입나다."),
    ONLY_MASTER_AND_ADMIN_EDIT(HttpStatus.BAD_REQUEST, "CO03", "채팅방 개설자와 관리자만 수정할 수 있습니다."),
    ONLY_MASTER_AND_ADMIN_DELETE(HttpStatus.BAD_REQUEST, "CO04", "채팅방 개설자와 관리자만 삭제할 수 있습니다."),
    ONLY_MASTER_AND_ADMIN_HAVE_AUTHORITY(HttpStatus. BAD_REQUEST, "C005", "채팅방 개설자와 관리자만 권한이 있습니다."),
    ONLY_ADMIN_HAVE_AUTHORITY(HttpStatus. BAD_REQUEST, "C006", "관리자만 권한이 있습니다."),
    NOT_ALLOW_PROFANITY_CHAT(HttpStatus.BAD_REQUEST, "C007", "비속어가 포함된 메시지는 전송할 수 없습니다."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.BAD_REQUEST, "C010", "해당 채팅방을 찾을 수 없습니다."),
    SENDER_NOT_FOUND(HttpStatus.BAD_REQUEST, "C011", "보낸 사람은 이 채팅방에 속해 있지 않습니다."),
    // aws s3
    EXISTED_FILE(HttpStatus.BAD_REQUEST, "AS001", "중복된 파일명입니다."),
    // 문의사항
    INQUIRY_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "IQ001", "오늘의 문의사항 생성 개수 제한을 초과하였습니다."),
    NOT_FOUND_INQUIRY(HttpStatus.BAD_REQUEST, "IQ002", "존재하지 않는 문의사항입나다."),
    NOT_AUTHORITY_INQUIRY(HttpStatus.BAD_REQUEST, "IQA003", "문의사항에 대한 권한이 없습니다."),
    NOT_ALLOW_PROFANITY_INQUIRY(HttpStatus.BAD_REQUEST, "C007", "비속어가 포함된 제목 또는 내용은 작성할 수 없습니다."),
    // 문의사항 답변
    NOT_FOUND_INQUIRY_ANSWER(HttpStatus.BAD_REQUEST, "IQA001", "존재하지 않는 문의사항 답변입나다."),
    NOT_AUTHORITY_ANSWER(HttpStatus.BAD_REQUEST, "IQA002", "답변 열람 권한이 없습니다."),
    // 회원
    EXISTS_ID(HttpStatus.CONFLICT,"MB001","이미 존재하는 아이디입니다."),
    EXISTS_EMAIL(HttpStatus.CONFLICT,"MB002","이미 존재하는 이메일입니다."),
    UNAUTHORIZED_ACTION(HttpStatus.FORBIDDEN, "MB003", "현재 제재로 인해 30일간 댓글 작성이 불가능합니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "MB004", "접근 권한이 없습니다."),
    NOT_ALLOW_PROFANITY_ID(HttpStatus.BAD_REQUEST, "MB005", "비속어가 포함되어 있는 아이디는 사용할 수 없습니다."),
    NOT_ALLOW_PROFANITY_NICKNAME(HttpStatus.BAD_REQUEST, "MB006", "비속어가 포함되어 있는 닉네임은 사용할 수 없습니다."),
    NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, "MB007", "일치하는 회원이 없습니다."),
    NOT_CORRECT_CODE(HttpStatus.NOT_FOUND, "MB008", "인증번호가 일치하지 않습니다."),
    FAIL_TO_CHANGE_PW(HttpStatus.NOT_FOUND, "MB009", "비밀번호 변경에 실패했습니다."),
    NOT_CORRECT_PW(HttpStatus.NOT_FOUND, "MB009", "비밀번호가 일치하지 않습니다."),
    // 탈퇴회원
    EXISTS_WITHDRAWMEMBER_ID(HttpStatus.CONFLICT, "W001", "탈퇴했거나 생성 불가능한 아이디입니다."),
    EXISTS_WITHDRAWMEMBER_EMAIL(HttpStatus.CONFLICT, "W002", "탈퇴했거나 생성 불가능한 이메일입니다."),
    EXISTS_WITHDRAWMEMBER_IPADDR(HttpStatus.CONFLICT, "W003", "탈퇴한 회원입니다. 30일 후 가입 가능합니다."),
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
