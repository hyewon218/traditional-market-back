package com.market.domain.member.repository;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSearchCond {
    private String keyword;
    private String type; // 추가된 타입, memberId / memberNickname로 각각 검색
}