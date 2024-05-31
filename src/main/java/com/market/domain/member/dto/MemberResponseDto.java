package com.market.domain.member.dto;

import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import com.market.global.security.oauth2.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseDto {

    private Long memberNo;
    private String memberId;
    private String memberEmail;
    private String memberNickname;
    private String memberPw;
    private Role role;
    private ProviderType providerType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String accessToken;
    private String refreshToken; // 삭제할 것

    public MemberResponseDto(Member member) {
        this.memberNo = member.getMemberNo();
        this.memberId = member.getMemberId();
        this.memberEmail = member.getMemberEmail();
        this.memberNickname = member.getMemberNickname();
        this.memberPw = member.getMemberPw();
        this.providerType = member.getProviderType();
        this.role = member.getRole();
        this.createTime = member.getCreateTime();
        this.updateTime = member.getUpdateTime();
    }

//    public static MemberResponseDto of(Member member){
//        return MemberResponseDto.builder()
//                .memberNo(member.getMemberNo())
//                .memberId(member.getMemberId())
//                .memberEmail(member.getMemberEmail())
//                .memberNickname(member.getMemberNickname())
//                .memberPw(member.getMemberPw())
//                .providerType(member.getProviderType())
//                .role(member.getRole())
//                .createTime(member.getCreateTime())
//                .updateTime(member.getUpdateTime())
//                .build();
//    }

}
