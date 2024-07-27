package com.market.domain.notice.dto;

import com.market.domain.image.dto.ImageResponseDto;
import com.market.domain.notice.entity.Notice;
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
public class NoticeResponseDto {

    private Long noticeNo;
    private String noticeTitle;
    private String noticeContent;
    private String noticeWriter;
    private Long viewCount;
    private List<ImageResponseDto> imageList;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static NoticeResponseDto of(Notice notice) {
        return NoticeResponseDto.builder()
                .noticeNo(notice.getNoticeNo())
                .noticeTitle(notice.getNoticeTitle())
                .noticeContent(notice.getNoticeContent())
                .noticeWriter(notice.getNoticeWriter())
                .viewCount(notice.getViewCount())
                .imageList(notice.getImageList().stream().map(ImageResponseDto::of).toList())
                .createTime(notice.getCreateTime())
                .updateTime(notice.getUpdateTime())
                .build();
    }
}
