package com.market.domain.inquiry.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class InquiryUpdateRequestDto {

    private String inquiryTitle;
    private String inquiryContent;
}
