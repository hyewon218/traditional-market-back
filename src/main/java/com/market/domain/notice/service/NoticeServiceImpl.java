package com.market.domain.notice.service;

import com.market.domain.image.config.AwsS3upload;
import com.market.domain.image.entity.Image;
import com.market.domain.image.repository.ImageRepository;
import com.market.domain.notice.dto.NoticeRequestDto;
import com.market.domain.notice.dto.NoticeResponseDto;
import com.market.domain.notice.entity.Notice;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import com.market.global.ip.IpService;
import com.market.domain.notice.repository.NoticeRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final IpService ipService;
    private final AwsS3upload awsS3upload;
    private final ImageRepository imageRepository;

    // 공지사항 생성
    @Override
    @Transactional
    public NoticeResponseDto createNotice(NoticeRequestDto noticeRequestDto, List<MultipartFile> files)
            throws IOException {

        Notice notice = noticeRequestDto.toEntity();
        noticeRepository.save(notice);

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }

                String fileUrl = awsS3upload.upload(file, "notice " + notice.getNoticeNo());

                if (imageRepository.existsByImageUrlAndNotice_NoticeNo(fileUrl, notice.getNoticeNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(notice, fileUrl));
            }
        }
        return NoticeResponseDto.of(notice);
    }

    // 공지사항 전체 조회
    @Override
    @Transactional(readOnly = true)
    public Page<NoticeResponseDto> findAll(Pageable pageable) {
        Page<Notice> notices = noticeRepository.findAll(pageable);
        return notices.map(NoticeResponseDto::of);
    }

    // 특정 공지사항 조회
    @Override
    @Transactional
    public NoticeResponseDto getNotice(long noticeNo, HttpServletRequest request) {
        Notice notice = noticeRepository.findById(noticeNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항 조회 실패 : " + noticeNo));

        String ipAddress = ipService.getIpAddress(request);
        if (!ipService.hasTypeBeenViewed(ipAddress, "notice", notice.getNoticeNo())) {
            ipService.markTypeAsViewed(ipAddress, "notice", notice.getNoticeNo());
            notice.setViewCount(notice.getViewCount() + 1);
        }
        return NoticeResponseDto.of(notice);
    }
    
    // 공지사항 수정
    @Override
    @Transactional
    public NoticeResponseDto update(long noticeNo, NoticeRequestDto requestDto, List<MultipartFile> files)
            throws IOException {
        Notice notice = noticeRepository.findById(noticeNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항 조회 실패 : " + noticeNo));

        log.info("입력받은 제목 : " + requestDto.getNoticeTitle());
        log.info("입력받은 내용 : " + requestDto.getNoticeContent());

        notice.updateNotice(requestDto);

        // 클라이언트에서 전달받은 이미지 URL 목록
        List<String> imageUrls = requestDto.getImageUrls();
        log.info("imageUrls : " + imageUrls);

        // DB에서 기존 공지사항에 연결된 이미지 목록 조회
        List<Image> existingImages = imageRepository.findByNotice_NoticeNo(noticeNo);
        log.info("existingImages : " + existingImages);

        // 파일이 있는 경우 파일 처리
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // 파일이 존재하면 S3에 업로드 후 DB에 저장
                    String fileUrl = awsS3upload.upload(file, "notice " + notice.getNoticeNo());

                    if (imageRepository.existsByImageUrlAndNotice_NoticeNo(fileUrl, notice.getNoticeNo())) {
                        throw new BusinessException(ErrorCode.EXISTED_FILE);
                    }
                    imageRepository.save(new Image(notice, fileUrl));
                }
            }
        }

        // 이미지 URL이 있을 경우 (기존 이미지 유지 및 삭제 처리)
        if (imageUrls != null) {
            // 기존 이미지 중 클라이언트에서 삭제된 이미지 처리
            for (Image existingImage : existingImages) {
                if (!imageUrls.contains(existingImage.getImageUrl())) {
                    imageRepository.delete(existingImage); // 클라이언트에서 삭제된 데이터 DB 삭제
                }
            }
        }

        // 이미지 URL이 비어 있을 경우 (모든 기존 이미지 삭제 처리)
        if (imageUrls == null || imageUrls.isEmpty()) {
            imageRepository.deleteAll(existingImages);
        }

        return NoticeResponseDto.of(notice);
    }
    
    // 공지사항 삭제
    @Override
    @Transactional
    public void delete(long noticeNo) {
        Notice notice = noticeRepository.findById(noticeNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항 조회 실패 : " + noticeNo));
        noticeRepository.deleteById(notice.getNoticeNo());

        List<Image> savedNoticeImage = imageRepository.findByNotice_NoticeNo(notice.getNoticeNo());
        imageRepository.deleteAll(savedNoticeImage); // 공지사항에 등록된 이미지도 함께 삭제
    }
}