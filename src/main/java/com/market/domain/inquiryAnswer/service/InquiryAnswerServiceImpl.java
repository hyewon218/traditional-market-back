package com.market.domain.inquiryAnswer.service;

import com.market.domain.image.config.AwsS3upload;
import com.market.domain.image.entity.Image;
import com.market.domain.image.repository.ImageRepository;
import com.market.domain.inquiry.constrant.InquiryState;
import com.market.domain.inquiry.entity.Inquiry;
import com.market.domain.inquiry.repository.InquiryRepository;
import com.market.domain.inquiry.service.InquiryService;
import com.market.domain.inquiryAnswer.dto.InquiryAnswerRequestDto;
import com.market.domain.inquiryAnswer.dto.InquiryAnswerResponseDto;
import com.market.domain.inquiryAnswer.entity.InquiryAnswer;
import com.market.domain.inquiryAnswer.repository.InquiryAnswerRepository;
import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.domain.member.service.MemberService;
import com.market.domain.notification.constant.NotificationType;
import com.market.domain.notification.entity.NotificationArgs;
import com.market.domain.notification.service.NotificationService;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryAnswerServiceImpl implements InquiryAnswerService {

    private final InquiryAnswerRepository inquiryAnswerRepository;
    private final InquiryRepository inquiryRepository;
    private final InquiryService inquiryService;
    private final ImageRepository imageRepository;
    private final AwsS3upload awsS3upload;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional // 문의사항 답변 생성
    public InquiryAnswerResponseDto createAnswer(
        InquiryAnswerRequestDto requestDto, Long inquiryNo, List<MultipartFile> files)
        throws IOException {
        Inquiry inquiry = inquiryService.findById(inquiryNo);
        InquiryAnswer inquiryAnswer = requestDto.toEntity(inquiry);
        inquiry.updateState(InquiryState.ANSWER_COMPLETED);

        /*문의사항 남긴 사용자에게 알람*/
        Member admin = memberRepository.findByRole(Role.ADMIN).orElseThrow(
            () -> new BusinessException(ErrorCode.NOT_EXISTS_ADMIN));
        Member receiver = memberService.findById(inquiry.getMemberNo());

        NotificationArgs notificationArgs = NotificationArgs.builder()
            .fromMemberNo(admin.getMemberNo()) // 관리자 No
            .targetId(inquiry.getInquiryNo())
            .build();
        notificationService.send(
            NotificationType.NEW_INQUIRY_ANSWER, notificationArgs, receiver);

        //log.info("작성 내용 : " + requestDto.getAnswerContent());

        inquiryAnswerRepository.save(inquiryAnswer);

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }
                String fileUrl = awsS3upload.upload(file, "answer " + inquiryAnswer.getAnswerNo());

                if (imageRepository.existsByImageUrlAndInquiryAnswer_AnswerNo(fileUrl,
                    inquiryAnswer.getAnswerNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(inquiryAnswer, fileUrl));
            }
        }
        return InquiryAnswerResponseDto.of(inquiryAnswer);
    }

    @Override
    @Transactional(readOnly = true) // 특정 문의사항 답변 조회
    public InquiryAnswerResponseDto getAnswer(Member member, Long inquiryNo) {
        validateIsMasterAndIsAdmin(member, inquiryNo); // 작성자인지 관리자인지 확인
        Optional<InquiryAnswer> inquiryAnswerOpt = inquiryAnswerRepository.findByInquiryNo(
            inquiryNo);
        return inquiryAnswerOpt.map(InquiryAnswerResponseDto::of)
            .orElseGet(InquiryAnswerResponseDto::new); // orElse 를 활용하여 빈 객체 반환
    }

    @Override
    @Transactional // 문의사항 답변 수정 // 아직 사용하지 않음
    public InquiryAnswerResponseDto updateAnswer(Member member, Long answerNo,
        InquiryAnswerRequestDto updateRequestDto, List<MultipartFile> files)
        throws IOException {
        validateIsAdmin(member);
        InquiryAnswer inquiryAnswer = findAnswer(answerNo);
        //log.info("입력받은 내용 : " + updateRequestDto.getAnswerContent());
        inquiryAnswer.updateAnswer(updateRequestDto);

        List<String> imageUrls = updateRequestDto.getImageUrls(); // 클라이언트
        List<Image> existingImages = imageRepository.findByInquiryAnswer_AnswerNo(answerNo); // DB

        if (files != null) {
            for (MultipartFile file : files) {
                String fileUrl = awsS3upload.upload(file, "answer " + inquiryAnswer.getAnswerNo());

                if (imageRepository.existsByImageUrlAndInquiryAnswer_AnswerNo(fileUrl,
                    inquiryAnswer.getAnswerNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(inquiryAnswer, fileUrl));
            }
        } else if (imageUrls != null) { // 기존 이미지 중 삭제되지 않은(남은) 이미지만 남도록
            // 이미지 URL 비교 및 삭제
            for (Image existingImage : existingImages) {
                if (!imageUrls.contains(existingImage.getImageUrl())) {
                    deleteImage(existingImage);
                }
            }
        }
       /* if (imageUrls == null) { // 기존 미리보기 이미지 전부 삭제 시 기존 DB image 삭제
            for (Image existingImage : existingImages) {
                imageRepository.delete(existingImage);
            }
        }*/
        return InquiryAnswerResponseDto.of(inquiryAnswer);
    }

    @Override
    @Transactional // 문의사항 답변 삭제 // 아직 사용하지 않음
    public void deleteAnswer(Member member, Long answerNo) {
        validateIsAdmin(member);
        InquiryAnswer inquiryAnswer = findAnswer(answerNo);
        // S3에서 이미지 삭제
        deleteImagesByInquiryAnswer(answerNo);
        // 문의사항 답변 삭제
        inquiryAnswerRepository.delete(inquiryAnswer);
    }

    private void deleteImagesByInquiryAnswer(Long answerNo) {
        List<Image> images = imageRepository.findByInquiryAnswer_AnswerNo(answerNo);
        // 이미지가 존재하는 경우에만 삭제 작업 수행
        if (images != null && !images.isEmpty()) {
            for (Image image : images) {
                deleteImage(image);
            }
        }
    }

    private void deleteImage(Image image) {
        awsS3upload.delete(image.getImageUrl()); // S3에서 이미지 삭제
        imageRepository.delete(image); // 클라이언트에서 저거된 데이터 DB 삭제
    }

    @Override
    @Transactional(readOnly = true)  // 문의사항 답변 찾기
    public InquiryAnswer findAnswer(Long answerNo) {
        return inquiryAnswerRepository.findById(answerNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_INQUIRY_ANSWER));
    }

    @Override
    @Transactional // 작성자인지 관리자인지 확인
    public void validateIsMasterAndIsAdmin(Member member, Long inquiryNo) {
        boolean isOwner = inquiryRepository.existsByMemberNoAndInquiryNo(member.getMemberNo(),
            inquiryNo); // 작성자인지 확인
        if (!isOwner && !member.getRole().equals(Role.ADMIN)) {
            throw new BusinessException(ErrorCode.ONLY_ADMIN_HAVE_AUTHORITY);
        }
    }

    @Override
    @Transactional // 관리자인지 확인
    public void validateIsAdmin(Member member) {
        if (!member.getRole().equals(Role.ADMIN)) {
            throw new BusinessException(ErrorCode.ONLY_ADMIN_HAVE_AUTHORITY);
        }
    }
}
