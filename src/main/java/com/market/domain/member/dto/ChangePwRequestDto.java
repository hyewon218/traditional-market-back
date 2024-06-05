package com.market.domain.member.dto;

import lombok.Getter;

@Getter
public class ChangePwRequestDto {

    private String currentPw; // 현재 비밀번호
    private String changePw; // 변경할 비밀번호
    private String confirmPw; // 변경할 비밀번호 재확인
}
