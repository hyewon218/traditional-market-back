package com.market.domain.notice.service;

import com.market.domain.notice.dto.NoticeRequestDto;
import com.market.domain.notice.dto.NoticeResponseDto;
import com.market.domain.notice.dto.NoticeUpdateRequestDto;
import com.market.domain.notice.entity.Notice;

import java.util.List;

public interface NoticeService {

    /**
     * 공지사항 생성
     *
     * @param noticeRequestDto  : 공지사항 생성 요청 정보
     * @return : 공지사항 저장
     */
    Notice createNotice(NoticeRequestDto noticeRequestDto);

    /**
     * 공지사항 전체 조회
     *
     * @return : 공지사항 전체 목록
     */
    List<NoticeResponseDto> findAll();

    /**
     * 특정 공지사항 조회
     *
     * @param noticeNo : 공지사항 고유번호
     * @return : 특정 공지사항
     */
    Notice findById(long noticeNo);

    /**
     * 공지사항 수정
     *
     * @param noticeNo : 공지사항 고유번호
     * @param updateRequestDto : 공지사항 수정 요청 정보
     * @return : 수정된 공지사항
     */
    Notice update(long noticeNo, NoticeUpdateRequestDto updateRequestDto);

    /**
     * 공지사항 삭제
     *
     * @param noticeNo : 공지사항 고유번호
     */
    void delete(long noticeNo);

}
