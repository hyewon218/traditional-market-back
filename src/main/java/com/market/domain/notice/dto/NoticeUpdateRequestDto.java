package com.market.domain.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class NoticeUpdateRequestDto {

    private String noticeTitle;
    private String noticeContent;
}
