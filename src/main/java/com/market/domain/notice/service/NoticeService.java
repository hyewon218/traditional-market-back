package com.market.domain.notice.service;

import com.market.domain.notice.dto.NoticeRequestDto;
import com.market.domain.notice.dto.NoticeResponseDto;
import com.market.domain.notice.entity.Notice;
import com.market.domain.notice.repository.NoticeSearchCond;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface NoticeService {

    /**
     * 공지사항 생성
     *
     * @param noticeRequestDto  : 공지사항 생성 요청 정보
     * @param files : 생성된 첨부파일
     * @return : 공지사항 저장
     */
    NoticeResponseDto createNotice(NoticeRequestDto noticeRequestDto, List<MultipartFile> files) throws IOException;

    /**
     * 공지사항 전체 조회
     *
     * @return : 공지사항 전체 목록
     */
    Page<NoticeResponseDto> getAllNotices(Pageable pageable);

    /**
     * 키워드 검색 공지사항 목록 조회
     *
     * @param cond 조건
     * @return 검색한 키워드가 있는 공지사항 목록 조회
     */
    Page<NoticeResponseDto> searchNotices(NoticeSearchCond cond, Pageable pageable);

    /**
     * 특정 공지사항 조회
     *
     * @param noticeNo : 공지사항 고유번호
     * @return : 특정 공지사항
     */
    NoticeResponseDto getNotice(Long noticeNo, HttpServletRequest request);

    /**
     * 문의사항 찾기
     *
     * @param noticeNo : 찾을 공지사항 no
     * @return : 공지사항 Entity
     */
    Notice findById(Long noticeNo);

    /**
     * 공지사항 수정
     *
     * @param noticeNo : 공지사항 고유번호
     * @param requestDto : 공지사항 수정 요청 정보
     * @param files : 생성된 첨부파일
     * @return : 수정된 공지사항
     */
    NoticeResponseDto updateNotice(Long noticeNo, NoticeRequestDto requestDto, List<MultipartFile> files) throws IOException;

    /**
     * 공지사항 삭제
     *
     * @param noticeNo : 공지사항 고유번호
     */
    void deleteNotice(Long noticeNo);

}
