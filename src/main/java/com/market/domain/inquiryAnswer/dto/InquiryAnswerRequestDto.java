package com.market.domain.inquiryAnswer.dto;

import com.market.domain.inquiry.entity.Inquiry;
import com.market.domain.inquiryAnswer.entity.InquiryAnswer;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryAnswerRequestDto {

    private Long inquiryWriterNo;

    private Long inquiryNo;

    @NotBlank(message = "답변 내용을 입력해주세요")
    private String answerContent;

    private String answerWriter;

    private List<String> imageUrls;

    public InquiryAnswer toEntity(Inquiry inquiry) {
        return InquiryAnswer.builder()
                .inquiryWriterNo(inquiry.getMemberNo())
                .inquiryNo(inquiry.getInquiryNo())
                .answerContent(this.answerContent)
                .answerWriter("관리자")
                .build();
    }
}
