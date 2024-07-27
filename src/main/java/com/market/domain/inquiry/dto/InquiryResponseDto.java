package com.market.domain.inquiry.dto;

import com.market.domain.image.dto.ImageResponseDto;
import com.market.domain.inquiry.entity.Inquiry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
    private String inquiryState;
    private List<ImageResponseDto> imageList;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static InquiryResponseDto of(Inquiry inquiry) {
        return InquiryResponseDto.builder()
                .inquiryNo(inquiry.getInquiryNo())
                .memberNo(inquiry.getMemberNo())
                .inquiryWriter(inquiry.getInquiryWriter())
                .inquiryTitle(inquiry.getInquiryTitle())
                .inquiryContent(inquiry.getInquiryContent())
                .inquiryState(inquiry.getInquiryState().toString())
                .imageList(inquiry.getImageList().stream().map(ImageResponseDto::of).toList())
                .createTime(inquiry.getCreateTime())
                .updateTime(inquiry.getUpdateTime())
                .build();
    }
}
