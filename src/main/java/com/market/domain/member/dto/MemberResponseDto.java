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
    private String nicknameWithRandomTag;
    private Role role;
    private ProviderType providerType;
    private LocalDateTime lastNicknameChangeDate; // 마지막 닉네임 변경 시간
    private boolean isWarning; // 제재 받은 상태인지 여부 (댓글, 채팅상담 제한)
    private LocalDateTime warningStartDate; // 제재 시작 시간
    private Long countWarning; // 제재 누적 횟수
    private Long countReport; // 다른 회원에게 신고당한 누적 횟수
    private String reportMember; // 내가 신고한 회원 목록
    private String reporters; // 나를 신고한 회원 목록
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String accessToken;
    private String refreshToken; // 삭제할 것

    public static MemberResponseDto of(Member member){
        return MemberResponseDto.builder()
            .memberNo(member.getMemberNo())
            .memberId(member.getMemberId())
            .memberEmail(member.getMemberEmail())
            .memberNickname(member.getMemberNickname())
            .nicknameWithRandomTag(member.getNicknameWithRandomTag())
            .role(member.getRole())
            .providerType(member.getProviderType())
            .lastNicknameChangeDate(member.getLastNicknameChangeDate())
            .isWarning(member.isWarning())
            .warningStartDate(member.getWarningStartDate())
            .countWarning(member.getCountWarning())
            .countReport(member.getCountReport())
            .reportMember(member.getReportMember().toString())
            .reporters(member.getReporters().toString())
            .createTime(member.getCreateTime())
            .updateTime(member.getUpdateTime())
            .build();
    }

    // 로그인 시에만 사용(토큰 추가), 최종 배포 전엔 삭제하기
    public static MemberResponseDto ofLogin(Member member, String accessToken, String refreshToken){
        return MemberResponseDto.builder()
            .memberNo(member.getMemberNo())
            .memberId(member.getMemberId())
            .memberEmail(member.getMemberEmail())
            .memberNickname(member.getMemberNickname())
            .nicknameWithRandomTag(member.getNicknameWithRandomTag())
            .role(member.getRole())
            .providerType(member.getProviderType())
            .lastNicknameChangeDate(member.getLastNicknameChangeDate())
            .isWarning(member.isWarning())
            .warningStartDate(member.getWarningStartDate())
            .countWarning(member.getCountWarning())
            .countReport(member.getCountReport())
            .reportMember(member.getReportMember().toString())
            .reporters(member.getReporters().toString())
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .createTime(member.getCreateTime())
            .updateTime(member.getUpdateTime())
            .build();
    }
}
