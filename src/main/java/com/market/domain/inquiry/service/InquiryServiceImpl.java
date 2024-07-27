package com.market.domain.inquiry.service;

import com.market.domain.image.config.AwsS3upload;
import com.market.domain.image.entity.Image;
import com.market.domain.image.repository.ImageRepository;
import com.market.domain.inquiry.dto.InquiryRequestDto;
import com.market.domain.inquiry.dto.InquiryResponseDto;
import com.market.domain.inquiry.dto.InquiryUpdateRequestDto;
import com.market.domain.inquiry.entity.Inquiry;
import com.market.domain.inquiry.repository.InquiryRepository;
import com.market.domain.inquiryAnswer.entity.InquiryAnswer;
import com.market.domain.inquiryAnswer.repository.InquiryAnswerRepository;
import com.market.domain.member.entity.Member;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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

                String fileUrl = awsS3upload.upload(file, "inquiry/" + inquiry.getInquiryNo());

                // 중복 파일 체크
                if (imageRepository.existsByImageUrlAndInquiry_InquiryNo(fileUrl, inquiry.getInquiryNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(inquiry, fileUrl));
            }
        }
        return InquiryResponseDto.of(inquiry);
    }

    // 문의사항 전체 조회(해당 회원의 문의사항만 조회)
    @Override
    @Transactional(readOnly = true)
    public Page<InquiryResponseDto> findAllMine(long memberNo, Pageable pageable) {
        Page<Inquiry> inquiries = inquiryRepository.findAllByMemberNo(memberNo, pageable);
        return inquiries.map(InquiryResponseDto::of);
    }

    // 문의사항 전체 조회(모든 회원의 문의사항 조회, admin만 가능)
    @Override
    @Transactional(readOnly = true)
    public Page<InquiryResponseDto> getAllInquiries(Pageable pageable) {
        Page<Inquiry> inquiries = inquiryRepository.findAll(pageable);
        return inquiries.map(InquiryResponseDto::of);
    }

    // 특정 문의사항 조회
    @Override
    @Transactional(readOnly = true)
    public Inquiry findById(long inquiryNo) {
        return inquiryRepository.findById(inquiryNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의사항 조회 실패 : " + inquiryNo));
    }

    // 문의사항 수정
    @Override
    @Transactional
    public Inquiry update(long inquiryNo, InquiryUpdateRequestDto updateRequestDto) {
        Inquiry inquiry = inquiryRepository.findById(inquiryNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의사항 조회 실패 : " + inquiryNo));

        inquiry.update(updateRequestDto.getInquiryTitle(), updateRequestDto.getInquiryContent());
        return inquiry;
    }

    // 문의사항 개별 삭제
    @Override
    @Transactional
    public void delete(long inquiryNo) {
        Inquiry inquiry = inquiryRepository.findById(inquiryNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의사항 조회 실패 : " + inquiryNo));

        inquiryRepository.deleteById(inquiry.getInquiryNo());
        List<Image> savedInquiryImages = imageRepository.findByInquiry_InquiryNo(inquiry.getInquiryNo());
        imageRepository.deleteAll(savedInquiryImages);

        InquiryAnswer inquiryAnswer = inquiryAnswerRepository.findByInquiryNo(inquiry.getInquiryNo());

        if (inquiryAnswer != null) {
            inquiryAnswerRepository.deleteById(inquiryAnswer.getAnswerNo()); // 해당 문의사항의 답변도 함께 삭제
            List<Image> savedInquiryAnswerImages = imageRepository.findByInquiryAnswer_AnswerNo(inquiryAnswer.getAnswerNo());
            imageRepository.deleteAll(savedInquiryAnswerImages);
        }
    }

    // 문의사항 전체 삭제(본인것만)
    @Override
    @Transactional
    public void deleteAllMine(long memberNo, Pageable pageable) {
        Page<Inquiry> inquiries = inquiryRepository.findAllByMemberNo(memberNo, pageable);
        if (!inquiries.isEmpty()) {
            inquiryRepository.deleteAllByMemberNo(memberNo);
        }
    }

    // 문의사항 전체 삭제(모두, admin만 가능)
    @Override
    @Transactional
    public void deleteAll(Pageable pageable) {
        Page<Inquiry> inquiries = inquiryRepository.findAll(pageable);
        if (!inquiries.isEmpty()) {
            inquiryRepository.deleteAll();
        }
    }

    // 전체 문의사항 개수 측정
    @Transactional
    public Long countInquiry() {
        return inquiryRepository.count();
    }

}