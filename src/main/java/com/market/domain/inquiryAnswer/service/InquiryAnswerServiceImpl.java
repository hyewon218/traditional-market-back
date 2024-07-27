package com.market.domain.inquiryAnswer.service;

import com.market.domain.image.config.AwsS3upload;
import com.market.domain.image.entity.Image;
import com.market.domain.image.repository.ImageRepository;
import com.market.domain.inquiry.constrant.InquiryState;
import com.market.domain.inquiry.entity.Inquiry;
import com.market.domain.inquiry.service.InquiryService;
import com.market.domain.inquiryAnswer.dto.InquiryAnswerRequestDto;
import com.market.domain.inquiryAnswer.dto.InquiryAnswerResponseDto;
import com.market.domain.inquiryAnswer.entity.InquiryAnswer;
import com.market.domain.inquiryAnswer.repository.InquiryAnswerRepository;
import com.market.domain.member.entity.Member;
import com.market.domain.member.service.MemberServiceImpl;
import com.market.domain.notification.service.NotificationService;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryAnswerServiceImpl implements InquiryAnswerService {

    private final InquiryAnswerRepository inquiryAnswerRepository;
    private final InquiryService inquiryService;
    private final AwsS3upload awsS3upload;
    private final ImageRepository imageRepository;

    // 문의사항 답변 생성
    @Override
    @Transactional
    public InquiryAnswerResponseDto createAnswer(
            InquiryAnswerRequestDto requestDto, Long inquiryNo, List<MultipartFile> files)
            throws IOException {

        Inquiry inquiry = inquiryService.findById(inquiryNo);
        InquiryAnswer inquiryAnswer = requestDto.toEntity(inquiry);
        inquiry.updateState(InquiryState.ANSWER_COMPLETED);

        log.info("작성 내용 : " + requestDto.getAnswerContent());

        inquiryAnswerRepository.save(inquiryAnswer);

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }

                String fileUrl = awsS3upload.upload(file, "answer " + inquiryAnswer.getAnswerNo());

                if (imageRepository.existsByImageUrlAndInquiryAnswer_AnswerNo(fileUrl, inquiryAnswer.getAnswerNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(inquiryAnswer, fileUrl));
            }
        }
        return InquiryAnswerResponseDto.of(inquiryAnswer);
    }

    // 특정 문의사항 답변 조회
    @Override
    @Transactional(readOnly = true)
    public InquiryAnswerResponseDto getAnswer(Member member, Long inquiryNo) {
        validateAnswer(member, inquiryNo);
        InquiryAnswer inquiryAnswer = inquiryAnswerRepository.findByInquiryNo(inquiryNo);
        return InquiryAnswerResponseDto.of(inquiryAnswer);
    }

    // 문의사항 답변 수정 // 아직 사용하지않음
    @Override
    @Transactional
    public InquiryAnswerResponseDto updateAnswer(Long answerNo, InquiryAnswerRequestDto updateRequestDto, List<MultipartFile> files)
            throws IOException {
        InquiryAnswer inquiryAnswer = findAnswer(answerNo);

        log.info("입력받은 내용 : " + updateRequestDto.getAnswerContent());

        inquiryAnswer.updateAnswer(updateRequestDto);
        List<String> imageUrls = updateRequestDto.getImageUrls(); // 클라이언트
        List<Image> existingImages = imageRepository.findByInquiryAnswer_AnswerNo(answerNo); // DB

        if (files != null) {
            for (MultipartFile file : files) {
                String fileUrl = awsS3upload.upload(file, "answer " + inquiryAnswer.getAnswerNo());

                if (imageRepository.existsByImageUrlAndNotice_NoticeNo(fileUrl, inquiryAnswer.getAnswerNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(inquiryAnswer, fileUrl));
            }

        } else if (imageUrls != null) { // 기존 이미지 중 삭제되지 않은(남은) 이미지만 남도록
            // 이미지 URL 비교 및 삭제
            for (Image existingImage : existingImages) {
                if (!imageUrls.contains(existingImage.getImageUrl())) {
                    imageRepository.delete(existingImage); // 클라이언트에서 삭제된 데이터 DB 삭제
                }
            }

            if (imageUrls == null) { // 기존 미리보기 이미지 전부 삭제 시 기존 DB image 삭제
                imageRepository.deleteAll(existingImages);
            }
        }
        return InquiryAnswerResponseDto.of(inquiryAnswer);
    }

    // 문의사항 답변 삭제 // 아직 사용하지않음
    @Override
    @Transactional
    public void deleteAnswer(Long answerNo) {
        InquiryAnswer inquiryAnswer = findAnswer(answerNo);
        inquiryAnswerRepository.deleteById(inquiryAnswer.getAnswerNo());
    }

    // 문의사항 답변 찾기
    @Override
    public InquiryAnswer findAnswer(Long answerNo) {
        return inquiryAnswerRepository.findById(answerNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_INQUIRY_ANSWER));
    }

    // 답변 열람 검증
    @Override
    @Transactional(readOnly = true)
    public boolean validateAnswer(Member member, Long inquiryNo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(
                authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        log.info("Admin true or false : " + isAdmin);

        boolean exists = inquiryAnswerRepository.existsByInquiryWriterNoAndInquiryNo(member.getMemberNo(), inquiryNo);

        if (!exists && !isAdmin) {
            throw new BusinessException(ErrorCode.NOT_AUTHORITY_ANSWER);
        }
        return true;
    }

}
