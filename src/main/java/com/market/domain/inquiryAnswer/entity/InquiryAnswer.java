package com.market.domain.inquiryAnswer.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.image.entity.Image;
import com.market.domain.inquiryAnswer.dto.InquiryAnswerRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "inquiry_answer")
public class InquiryAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_no")
    private Long answerNo;

    private Long inquiryWriterNo; // 답변할 문의사항의 작성자

    private Long inquiryNo; // 답변할 문의사항

    private String answerTitle; // 아직 사용하지 않음

    @Column(nullable = false)
    private String answerContent;

    private String answerWriter; // 관리자

    @Builder.Default
    @OneToMany(mappedBy = "inquiryAnswer", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<Image> imageList = new ArrayList<>();

    public void updateAnswer(InquiryAnswerRequestDto updateRequestDto) {
        this.answerContent = updateRequestDto.getAnswerContent();
    }
}
