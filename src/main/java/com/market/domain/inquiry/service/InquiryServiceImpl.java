package com.market.domain.inquiry.service;

import com.market.domain.inquiry.dto.InquiryRequestDto;
import com.market.domain.inquiry.dto.InquiryResponseDto;
import com.market.domain.inquiry.dto.InquiryUpdateRequestDto;
import com.market.domain.inquiry.entity.Inquiry;
import com.market.domain.inquiry.repository.InquiryRepository;
import com.market.domain.member.entity.Member;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;

    // 문의사항 생성
    @Override
    public Inquiry createInquiry(InquiryRequestDto inquiryRequestDto, Member member) {
        return inquiryRepository.save(inquiryRequestDto.toEntity(member));
    }

    // 문의사항 전체 조회(해당 회원의 문의사항만 조회)
    @Override
    public List<InquiryResponseDto> findAll(String memberId) {
        List<Inquiry> inquiries = inquiryRepository.findAllByInquiryWriter(memberId);
        List<InquiryResponseDto> inquiryResponseDtos = inquiries
                .stream()
                .map(InquiryResponseDto::of)
                .toList();
        return inquiryResponseDtos;
    }

    // 특정 문의사항 조회
    @Override
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
    public void delete(long inquiryNo) {
        Inquiry inquiry = inquiryRepository.findById(inquiryNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의사항 조회 실패 : " + inquiryNo));
        inquiryRepository.deleteById(inquiryNo);
    }

    // 문의사항 전체 삭제
    @Override
    @Transactional
    public void deleteAll(String memberId) {
        List<Inquiry> inquiries = inquiryRepository.findAllByInquiryWriter(memberId);
        if (!inquiries.isEmpty()) {
            inquiryRepository.deleteAllByInquiryWriter(memberId);
        }
    }

}