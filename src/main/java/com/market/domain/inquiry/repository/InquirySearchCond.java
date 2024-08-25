package com.market.domain.inquiry.repository;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquirySearchCond {
    private String keyword;
    private String type;
}