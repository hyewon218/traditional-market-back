package com.market.domain.member.withdrawMember.repository;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawMemberSearchCond {
    private String keyword;
    private String type; // 추가된 타입, memberId / memberEmail로 각각 검색
}