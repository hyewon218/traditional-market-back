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

    private Long memberNo;
    private String memberId;
    private String memberEmail;
    private String memberNickname;
    private String nicknameWithRandomTag;
    private Role role;
    private LocalDateTime createTime; // 가입일

    public static MyInfoResponseDto of(Member member) {
        return MyInfoResponseDto.builder()
                .memberNo(member.getMemberNo())
                .memberId(member.getMemberId())
                .memberEmail(member.getMemberEmail())
                .memberNickname(member.getMemberNickname())
                .nicknameWithRandomTag(member.getMemberNickname() + member.getRandomTag())
                .role(member.getRole())
                .createTime(member.getCreateTime())
                .build();
    }
}
