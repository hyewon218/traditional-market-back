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
public class MemberResponseDto {

    private Long memberNo;
    private String memberId;
    private String memberPw;
    private String memberEmail;
    private Role role;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String token;

    public MemberResponseDto(Member member) {
        this.memberNo = member.getMemberNo();
        this.memberId = member.getMemberId();
        this.memberEmail = member.getMemberEmail();
        this.memberPw = member.getMemberPw();
        this.role = member.getRole();
        this.createTime = member.getCreateTime();
        this.updateTime = member.getUpdateTime();
    }

//    public static MemberResponseDto of(Member member){
//        return MemberResponseDto.builder()
//                .memberNo(member.getMemberNo())
//                .memberId(member.getMemberId())
//                .memberEmail(member.getMemberEmail())
//                .memberPw(member.getMemberPw())
//                .role(member.getRole())
//                .createTime(member.getCreateTime())
//                .updateTime(member.getUpdateTime())
//                .build();
//    }

}
