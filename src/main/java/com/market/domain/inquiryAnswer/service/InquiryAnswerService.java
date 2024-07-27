package com.market.domain.inquiryAnswer.service;

import com.market.domain.inquiryAnswer.dto.InquiryAnswerRequestDto;
import com.market.domain.inquiryAnswer.dto.InquiryAnswerResponseDto;
import com.market.domain.inquiryAnswer.entity.InquiryAnswer;
import com.market.domain.member.entity.Member;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface InquiryAnswerService {

    /**
     * 문의사항 답변 생성
     *
     * @param requestDto  : 문의사항 답변 생성 요청 정보
     * @param inquiryNo : 해당 문의사항 고유번호
     * @param files : 생성된 첨부파일
     * @return : 생성된 문의사항 답변 responseDto 반환
     */
    InquiryAnswerResponseDto createAnswer(
            InquiryAnswerRequestDto requestDto, Long inquiryNo, List<MultipartFile> files) throws IOException;

    /**
     * 특정 문의사항 답변 조회
     *
     * @param inquiryNo : 문의사항 고유번호
     * @return : 특정 문의사항 답변
     */
    InquiryAnswerResponseDto getAnswer(Member member, Long inquiryNo);

    /**
     * 문의사항 답변 수정
     *
     * @param answerNo : 문의사항 답변 고유번호
     * @param updateRequestDto : 문의사항 답변 수정 요청 정보
     * @param files : 생성된 첨부파일
     * @return : 수정된 문의사항 답변 responseDto 반환
     */
    InquiryAnswerResponseDto updateAnswer(
            Long answerNo, InquiryAnswerRequestDto updateRequestDto, List<MultipartFile> files) throws IOException;

    /**
     * 문의사항 답변 삭제
     *
     * @param answerNo : 문의사항 답변 고유번호
     */
    void deleteAnswer(Long answerNo);

    /**
     * 문의사항 답변 찾기
     *
     * @param answerNo : 문의사항 답변 고유번호
     * @return : 특정 문의사항 답변
     */
    InquiryAnswer findAnswer(Long answerNo);

    /**
     * 답변 열람 검증
     *
     * @param member : 현재 로그인 중인 회원
     * @param inquiryNo : 해당 문의사항 고유번호
     * @return : true/false
     */
    boolean validateAnswer(Member member, Long inquiryNo);

}
