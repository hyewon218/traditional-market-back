package com.market.domain.inquiry.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

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

    private String inquiryWriter;

    @Column(nullable = false)
    private String inquiryTitle;

    @Column(nullable = false)
    private String inquiryContent;

    @ManyToOne
    @JoinColumn(name = "member_no")
    private Member member;

    public Inquiry(String inquiryTitle, String inquiryContent, Member member) {
        this.inquiryWriter = member.getMemberId();
        this.inquiryTitle = inquiryTitle;
        this.inquiryContent = inquiryContent;
        this.member = member;
    }

    public void update(String inquiryTitle, String inquiryContent) {
        this.inquiryTitle = inquiryTitle;
        this.inquiryContent = inquiryContent;
    }
}
