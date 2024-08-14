package com.market.domain.inquiry.service;

import com.market.domain.inquiry.dto.InquiryRequestDto;
import com.market.domain.inquiry.dto.InquiryResponseDto;
import com.market.domain.inquiry.dto.InquiryUpdateRequestDto;
import com.market.domain.inquiry.entity.Inquiry;
import com.market.domain.member.entity.Member;
import java.io.IOException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface InquiryService {

    /**
     * 문의사항 생성
     *
     * @param inquiryRequestDto : 문의사항 생성 요청 정보
     * @param member            : 문의사항 작성자
     * @param files             : 생성된 첨부파일
     * @return : 생성된 문의사항 반환
     */
    InquiryResponseDto createInquiry(InquiryRequestDto inquiryRequestDto, Member member,
        List<MultipartFile> files) throws IOException;

    /**
     * 특정 문의사항 조회
     *
     * @param inquiryNo : 찾을 문의사항 no
     * @return : 문의사항 Entity
     */
    InquiryResponseDto getInquiry(Member member, Long inquiryNo);

    /**
     * 전체 문의사항 조회(본인것만)
     *
     * @param memberNo : 문의사항 작성자 고유번호
     * @return : 전체 문의사항(본인것만)
     */
    Page<InquiryResponseDto> getAllMine(Long memberNo, Pageable pageable);

    /**
     * 전체 문의사항 조회(모두, 관리자만 가능)
     *
     * @return : 전체 문의사항
     */
    Page<InquiryResponseDto> getAllInquiries(Pageable pageable);

    /**
     * 문의사항 찾기
     *
     * @param inquiryNo : 찾을 문의사항 no
     * @return : 문의사항 Entity
     */
    Inquiry findById(Long inquiryNo);

    /**
     * 문의사항 수정
     *
     * @param inquiryNo        : 문의사항 고유 번호
     * @param updateRequestDto : 문의사항 수정 요청 정보
     * @return : 수정된 문의사항
     */
    InquiryResponseDto updateInquiry(Long memberNo, InquiryUpdateRequestDto updateRequestDto,
        Long inquiryNo);

    /**
     * 문의사항 개별 삭제
     *
     * @param inquiryNo : 문의사항 고유 번호
     */
    void deleteInquiry(Member member, Long inquiryNo);

    /**
     * 문의사항 전체 삭제(본인것만)
     *
     * @param memberNo : 문의사항 작성자 고유번호
     */
    void deleteAllMine(Long memberNo);

    /**
     * 문의사항 전체 삭제(모두, admin 만 가능)
     */
    void deleteAll(Member member);

    /**
     * 문의사항 전체 개수 조회
     */
    Long countInquiry();

    /**
     * 문의사항 작성자인지 확인
     */
    boolean validateIsMaster(Long memberNo, Long inquiryNo);

    /**
     * 문의사항 작성자인지 관리자인지 확인
     */
    void validateIsMasterAndAdmin(Member member, Long inquiryNo);

    /**
     * 관리자인지 확인
     */
    void validateIsAdmin(Member member);
}
