package com.market.domain.inquiryAnswer.dto;

import com.market.domain.image.dto.ImageResponseDto;
import com.market.domain.inquiryAnswer.entity.InquiryAnswer;
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
public class InquiryAnswerResponseDto {

    private Long answerNo;
    private Long inquiryWriterNo;
    private Long inquiryNo;
    private String answerContent;
    private String answerWriter;
    private List<ImageResponseDto> imageList;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static InquiryAnswerResponseDto of(InquiryAnswer inquiryAnswer) {
        return InquiryAnswerResponseDto.builder()
                .answerNo(inquiryAnswer.getAnswerNo())
                .inquiryWriterNo(inquiryAnswer.getInquiryWriterNo())
                .inquiryNo(inquiryAnswer.getInquiryNo())
                .answerContent(inquiryAnswer.getAnswerContent())
                .answerWriter(inquiryAnswer.getAnswerWriter())
                .imageList(inquiryAnswer.getImageList().stream().map(ImageResponseDto::of).toList())
                .createTime(inquiryAnswer.getCreateTime())
                .updateTime(inquiryAnswer.getUpdateTime())
                .build();
    }

}
