package com.market.domain.inquiry.service;

import com.market.domain.image.config.AwsS3upload;
import com.market.domain.image.config.ImageConfig;
import com.market.domain.image.entity.Image;
import com.market.domain.image.repository.ImageRepository;
import com.market.domain.inquiry.dto.InquiryRequestDto;
import com.market.domain.inquiry.dto.InquiryResponseDto;
import com.market.domain.inquiry.dto.InquiryUpdateRequestDto;
import com.market.domain.inquiry.entity.Inquiry;
import com.market.domain.inquiry.repository.InquiryRepository;
import com.market.domain.inquiryAnswer.entity.InquiryAnswer;
import com.market.domain.inquiryAnswer.repository.InquiryAnswerRepository;
import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;
    private final AwsS3upload awsS3upload;
    private final ImageRepository imageRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;

    // 문의사항 생성
    @Override
    @Transactional
    public InquiryResponseDto createInquiry(InquiryRequestDto inquiryRequestDto, Member member,
        List<MultipartFile> files) throws IOException {
        // 하루에 생성 가능한 최대 문의사항 수
        int maxInquiriesPerDay = 5;
        LocalDate today = LocalDate.now();
        // 회원의 오늘 생성한 문의사항 개수 조회
        long inquiriesCountToday = inquiryRepository.countByMemberNoAndCreateTimeBetween(
            member.getMemberNo(), today.atStartOfDay(), today.atTime(LocalTime.MAX));
        // 제한 초과 여부 체크
        if (inquiriesCountToday >= maxInquiriesPerDay) {
            throw new BusinessException(ErrorCode.INQUIRY_LIMIT_EXCEEDED);
        }
        Inquiry inquiry = inquiryRequestDto.toEntity(member);
        inquiryRepository.save(inquiry);
        // 파일이 존재하는지 체크
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue; // 빈 파일은 건너뜀
                }
                String fileUrl = awsS3upload.upload(file, "inquiry " + inquiry.getInquiryNo());
                // 중복 파일 체크
                if (imageRepository.existsByImageUrlAndInquiry_InquiryNo(fileUrl,
                    inquiry.getInquiryNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(inquiry, fileUrl));
            }
        }
        return InquiryResponseDto.of(inquiry);
    }

    @Override
    @Transactional(readOnly = true) // 특정 문의사항 조회
    public InquiryResponseDto getInquiry(Member member, Long inquiryNo) {
        Inquiry inquiry = findById(inquiryNo);
        validateIsMasterAndAdmin(member, inquiryNo);
        return InquiryResponseDto.of(inquiry);
    }

    @Override
    @Transactional(readOnly = true) // 문의사항 전체 조회(해당 회원의 문의사항만 조회)
    public Page<InquiryResponseDto> getAllMine(Long memberNo, Pageable pageable) {
        Page<Inquiry> inquiries = inquiryRepository.findAllByMemberNo(memberNo, pageable);
        return inquiries.map(InquiryResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 문의사항 전체 조회(모든 회원의 문의사항 조회, admin 만 가능)
    public Page<InquiryResponseDto> getAllInquiries(Pageable pageable) {
        Page<Inquiry> inquiries = inquiryRepository.findAll(pageable);
        return inquiries.map(InquiryResponseDto::of);
    }

    @Override
    @Transactional // 문의사항 수정
    public InquiryResponseDto updateInquiry(Long memberNo, InquiryUpdateRequestDto updateRequestDto,
        Long inquiryNo) {
        Inquiry inquiry = findById(inquiryNo);
        if (validateIsMaster(memberNo, inquiryNo)) {
            inquiry.updateInquiry(updateRequestDto);
        }
        return InquiryResponseDto.of(inquiry);
    }

    @Override
    @Transactional // 문의사항 개별 삭제
    public void deleteInquiry(Member member, Long inquiryNo) {
        validateIsMasterAndAdmin(member, inquiryNo);
        Inquiry inquiry = findById(inquiryNo);
        // 해당 문의사항의 답변 + S3 도 함께 삭제
        deleteInquiryAnswer(inquiryNo);
        // S3 에서도 이미지 삭제
        deleteImagesByInquiry(inquiryNo);
        // 문의사항 삭제
        inquiryRepository.delete(inquiry);
    }

    private void deleteInquiryAnswer(Long inquiryNo) {
        Optional<InquiryAnswer> inquiryAnswer = inquiryAnswerRepository.findByInquiryNo(inquiryNo);
        inquiryAnswer.ifPresent(answer -> {
            // S3 이미지들 삭제
            List<Image> images = imageRepository.findByInquiryAnswer_AnswerNo(answer.getAnswerNo());
            for (Image image : images) {
                if (!image.getImageUrl().equals(ImageConfig.DEFAULT_IMAGE_URL)) {
                    awsS3upload.delete(image.getImageUrl());
                }
            }
            inquiryAnswerRepository.deleteById(answer.getAnswerNo());
        });
    }

    @Override
    @Transactional // 문의사항 전체 삭제(본인것만)
    public void deleteAllMine(Long memberNo) {
        deleteImages(false, memberNo);
        inquiryRepository.deleteAllByMemberNo(memberNo);
    }

    @Override
    @Transactional // 문의사항 전체 삭제(모두, admin 만 가능)
    public void deleteAll(Member member) {
        validateIsAdmin(member);
        deleteImages(true, null);
        inquiryRepository.deleteAll();
    }

    @Override
    @Transactional // 문의사항 내 S3 에서도 이미지 삭제
    public void deleteImages(boolean deleteAll, Long memberNo) {
        List<Inquiry> inquiries = getInquiries(deleteAll, memberNo);
        for (Inquiry inquiry : inquiries) {
            // S3 에서도 이미지 삭제
            deleteImagesByInquiry(inquiry.getInquiryNo());
        }
    }

    private List<Inquiry> getInquiries(boolean deleteAll, Long memberNo) {
        return deleteAll ? inquiryRepository.findAll()
            : inquiryRepository.findAllByMemberNo(memberNo);
    }

    private void deleteImagesByInquiry(Long inquiryNo) {
        List<Image> images = imageRepository.findByInquiry_InquiryNo(inquiryNo);
        // 이미지가 존재하는 경우에만 S3 삭제 작업 수행
        if (images != null && !images.isEmpty()) {
            for (Image image : images) {
                awsS3upload.delete(image.getImageUrl());
            }
        }
    }

    @Override
    @Transactional(readOnly = true) // 문의사항 찾기
    public Inquiry findById(Long inquiryNo) {
        return inquiryRepository.findById(inquiryNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_INQUIRY));
    }

    // 전체 문의사항 개수 조회
    @Override
    @Transactional(readOnly = true)
    public Long countInquiry() {
        return inquiryRepository.count();
    }

    @Override
    @Transactional // 문의사항 작성자인지 확인
    public boolean validateIsMaster(Long memberNo, Long inquiryNo) {
        boolean isOwner = inquiryRepository.existsByMemberNoAndInquiryNo(memberNo, inquiryNo);
        if (!isOwner) {
            throw new BusinessException(ErrorCode.NOT_AUTHORITY_INQUIRY);
        }
        return true;
    }

    @Override
    @Transactional // 문의사항 작성자인지 관리자인지 확인
    public void validateIsMasterAndAdmin(Member member, Long inquiryNo) {
        boolean isOwner = inquiryRepository.existsByMemberNoAndInquiryNo(member.getMemberNo(),
            inquiryNo);
        if (!isOwner && !member.getRole().equals(Role.ADMIN)) {
            throw new BusinessException(ErrorCode.NOT_AUTHORITY_INQUIRY);
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