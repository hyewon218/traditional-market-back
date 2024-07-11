package com.market.domain.inquiry.dto;

import com.market.domain.inquiry.entity.Inquiry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryResponseDto {

    private Long inquiryNo;
    private Long memberNo;
    private String inquiryWriter;
    private String inquiryTitle;
    private String inquiryContent;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static InquiryResponseDto of(Inquiry inquiry) {
        return InquiryResponseDto.builder()
                .inquiryNo(inquiry.getInquiryNo())
                .memberNo(inquiry.getMemberNo())
                .inquiryWriter(inquiry.getInquiryWriter())
                .inquiryTitle(inquiry.getInquiryTitle())
                .inquiryContent(inquiry.getInquiryContent())
                .createTime(inquiry.getCreateTime())
                .updateTime(inquiry.getUpdateTime())
                .build();
    }
}
