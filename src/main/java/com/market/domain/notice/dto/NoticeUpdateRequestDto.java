package com.market.domain.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class NoticeUpdateRequestDto {

    private String noticeTitle;
    private String noticeContent;
    private List<String> imageUrls; // 공지사항 수정 화면에서 남은 기존 이미지들
}
