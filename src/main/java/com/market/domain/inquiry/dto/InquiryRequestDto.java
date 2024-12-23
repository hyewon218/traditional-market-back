package com.market.domain.inquiry.dto;

import com.market.domain.inquiry.constrant.InquiryState;
import com.market.domain.inquiry.entity.Inquiry;
import com.market.domain.member.entity.Member;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryRequestDto {

    private String inquiryWriter;

    @NotBlank(message = "제목을 입력하세요")
    private String inquiryTitle;

    @NotBlank(message = "내용을 입력하세요")
    private String inquiryContent;

    public Inquiry toEntity(Member member) {
        return Inquiry.builder()
                .memberNo(member.getMemberNo())
                .inquiryWriter(member.getMemberId())
                .inquiryTitle(this.inquiryTitle)
                .inquiryContent(this.inquiryContent)
                .inquiryState(InquiryState.ANSWER_PENDING)
                .build();
    }
}
