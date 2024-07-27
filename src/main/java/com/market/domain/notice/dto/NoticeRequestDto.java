package com.market.domain.notice.dto;

import com.market.domain.notice.entity.Notice;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeRequestDto {

    @NotBlank(message = "제목을 입력해주세요")
    private String noticeTitle;

    @NotBlank(message = "내용을 입력해주세요")
    private String noticeContent;

    private String noticeWriter;

    private List<String> imageUrls; // 공지사항 수정 시 남아있는 이미지

    public NoticeRequestDto(String noticeTitle, String noticeContent) {
        this.noticeTitle = noticeTitle;
        this.noticeContent = noticeContent;
        this.noticeWriter = "관리자";
    }

    public Notice toEntity() {
        return Notice.builder()
                .noticeTitle(this.noticeTitle)
                .noticeContent(this.noticeContent)
                .noticeWriter("관리자")
                .viewCount(0L)
                .build();
    }
}
