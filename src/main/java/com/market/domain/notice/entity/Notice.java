package com.market.domain.notice.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.image.entity.Image;
import com.market.domain.notice.dto.NoticeRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "notice")
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_no")
    private Long noticeNo;

    @Column(nullable = false)
    private String noticeTitle;

    @Column(nullable = false)
    private String noticeContent;

    private String noticeWriter; // "관리자"로 고정

    private Long viewCount; // 조회수

    @Builder.Default
    @OneToMany(mappedBy = "notice", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<Image> imageList = new ArrayList<>();

    public Notice(String noticeTitle, String noticeContent) {
        this.noticeTitle = noticeTitle;
        this.noticeContent = noticeContent;
        this.noticeWriter = "관리자";
    }

    public void updateNotice(NoticeRequestDto requestDto) {
        this.noticeTitle = requestDto.getNoticeTitle();
        this.noticeContent = requestDto.getNoticeContent();
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }
}
