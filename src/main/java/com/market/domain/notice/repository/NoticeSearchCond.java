package com.market.domain.notice.repository;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeSearchCond {
    private String keyword;
}