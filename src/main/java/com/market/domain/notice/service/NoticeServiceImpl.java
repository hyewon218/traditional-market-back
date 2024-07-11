package com.market.domain.notice.service;

import com.market.domain.notice.dto.NoticeRequestDto;
import com.market.domain.notice.dto.NoticeResponseDto;
import com.market.domain.notice.dto.NoticeUpdateRequestDto;
import com.market.domain.notice.entity.Notice;
import com.market.domain.notice.repository.NoticeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;

    // 공지사항 글 생성
    @Override
    @Transactional
    public Notice createNotice(NoticeRequestDto noticeRequestDto) {
        return noticeRepository.save(noticeRequestDto.toEntity());
    }

    // 공지사항 전체 조회
    @Override
    public List<NoticeResponseDto> findAll() {
        List<Notice> notices = noticeRepository.findAll();
        List<NoticeResponseDto> noticeResponseDtos = notices
                .stream()
                .map(NoticeResponseDto::of)
                .toList();
        return noticeResponseDtos;
    }

    // 특정 공지사항 조회
    @Override
    public Notice findById(long noticeNo) {
        return noticeRepository.findById(noticeNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항 조회 실패 : " + noticeNo));
    }
    
    // 공지사항 수정
    @Override
    @Transactional
    public Notice update(long noticeNo, NoticeUpdateRequestDto updateRequestDto) {
        Notice notice = noticeRepository.findById(noticeNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항 조회 실패 : " + noticeNo));
        notice.updateNotice(updateRequestDto.getNoticeTitle(), updateRequestDto.getNoticeContent());
        return notice;
    }
    
    // 공지사항 삭제
    @Override
    @Transactional
    public void delete(long noticeNo) {
        Notice notice = noticeRepository.findById(noticeNo)
                        .orElseThrow(() -> new IllegalArgumentException("해당 공지사항 조회 실패 : " + noticeNo));
        noticeRepository.deleteById(notice.getNoticeNo());
    }
}