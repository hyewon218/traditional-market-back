package com.market.domain.member.dto;

import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
// 내정보 조회 시 반환 Dto
public class MyInfoResponseDto {

    private String memberId;
    private String memberEmail;
    private String memberNickname;
    private Role role;
    private LocalDateTime createTime; // 가입일

    public static MyInfoResponseDto of(Member member) {
        return MyInfoResponseDto.builder()
                .memberId(member.getMemberId())
                .memberEmail(member.getMemberEmail())
                .memberNickname(member.getMemberNickname())
                .role(member.getRole())
                .createTime(member.getCreateTime())
                .build();
    }
}
