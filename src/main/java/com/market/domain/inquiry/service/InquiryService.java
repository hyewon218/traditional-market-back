package com.market.domain.inquiry.service;

import com.market.domain.inquiry.dto.InquiryRequestDto;
import com.market.domain.inquiry.dto.InquiryResponseDto;
import com.market.domain.inquiry.dto.InquiryUpdateRequestDto;
import com.market.domain.inquiry.entity.Inquiry;
import com.market.domain.member.entity.Member;

import java.util.List;

public interface InquiryService {
    
    /**
     * 문의사항 생성
     *
     * @param inquiryRequestDto  : 문의사항 생성 요청 정보
     * @param member : 문의사항 작성자
     * @return : 문의사항 저장
     */
    Inquiry createInquiry(InquiryRequestDto inquiryRequestDto, Member member);

    /**
     * 전체 문의사항 조회
     *
     * @param memberNo : 문의사항 작성자 고유번호
     * @return : 전체 문의사항
     */
    List<InquiryResponseDto> findAll(long memberNo);

    /**
     * 특정 문의사항 조회
     *
     * @param inquiryNo  : 문의사항 고유 번호
     * @return : 특정 문의사항
     */
    Inquiry findById(long inquiryNo);

    /**
     * 문의사항 수정
     *
     * @param inquiryNo  : 문의사항 고유 번호
     * @param updateRequestDto : 문의사항 수정 요청 정보
     * @return : 수정된 문의사항
     */
    Inquiry update(long inquiryNo, InquiryUpdateRequestDto updateRequestDto);

    /**
     * 문의사항 개별 삭제
     *
     * @param inquiryNo  : 문의사항 고유 번호
     */
    void delete(long inquiryNo);

    /**
     * 문의사항 전체 삭제
     *
     * @param memberNo  : 문의사항 작성자 고유번호
     */
    void deleteAll(long memberNo);
}
