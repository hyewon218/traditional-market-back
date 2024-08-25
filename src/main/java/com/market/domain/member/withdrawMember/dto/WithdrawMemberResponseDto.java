package com.market.domain.member.withdrawMember.dto;

import com.market.domain.member.withdrawMember.entity.WithdrawMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawMemberResponseDto {

    private Long withdrawMemberNo;
    private String withdrawMemberId;
    private String withdrawMemberEmail;
    private String withdrawIpAddr;
    private LocalDateTime withdrawDate;

    public static WithdrawMemberResponseDto of(WithdrawMember withdrawMember) {
        return WithdrawMemberResponseDto.builder()
            .withdrawMemberNo(withdrawMember.getWithdrawMemberNo())
            .withdrawMemberId(withdrawMember.getWithdrawMemberId())
            .withdrawMemberEmail(withdrawMember.getWithdrawMemberEmail())
            .withdrawIpAddr(withdrawMember.getWithdrawIpAddr())
            .withdrawDate(withdrawMember.getWithdrawDate())
            .build();
    }
}
