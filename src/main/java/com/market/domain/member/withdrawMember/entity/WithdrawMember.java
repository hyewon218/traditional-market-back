package com.market.domain.member.withdrawMember.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "withdraw_member")
public class WithdrawMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "withdraw_member_no")
    private Long withdrawMemberNo;

    private String withdrawMemberId;

    private String withdrawMemberEmail;

    private LocalDateTime withdrawDate;

    public void setWithdrawDate(LocalDateTime withdrawDate) {
        this.withdrawDate = withdrawDate;
    }

    public static WithdrawMember toEntity(Member member) {
        return WithdrawMember.builder()
            .withdrawMemberId(member.getMemberId())
            .withdrawMemberEmail(member.getMemberEmail())
            .withdrawDate(LocalDateTime.now())
            .build();
    }

}
