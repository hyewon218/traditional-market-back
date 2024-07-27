package com.market.domain.inquiry.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.image.entity.Image;
import com.market.domain.inquiry.constrant.InquiryState;
import com.market.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "inquiry")
public class Inquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_no")
    private Long inquiryNo;

    private Long memberNo;

    private String inquiryWriter;

    @Column(nullable = false)
    private String inquiryTitle;

    @Column(nullable = false)
    private String inquiryContent;

    @Enumerated(EnumType.STRING)
    private InquiryState inquiryState;

    @Builder.Default
    @OneToMany(mappedBy = "inquiry", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<Image> imageList = new ArrayList<>();

    public Inquiry(String inquiryTitle, String inquiryContent, Member member) {
        this.inquiryWriter = member.getNicknameWithRandomTag();
        this.inquiryTitle = inquiryTitle;
        this.inquiryContent = inquiryContent;
    }

    public void update(String inquiryTitle, String inquiryContent) {
        this.inquiryTitle = inquiryTitle;
        this.inquiryContent = inquiryContent;
    }

    public void updateState(InquiryState newState) {
        this.inquiryState = newState;
    }
}
