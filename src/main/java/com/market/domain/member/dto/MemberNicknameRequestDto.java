package com.market.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
public class MemberNicknameRequestDto {

    @NotBlank(message = "닉네임은 반드시 입력해주세요")
    private String memberNickname;
}
