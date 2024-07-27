package com.market.domain.inquiryAnswer.repository;

import com.market.domain.inquiryAnswer.entity.InquiryAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {

    InquiryAnswer findByInquiryNo(Long inquiryNo);

    boolean existsByInquiryWriterNoAndInquiryNo(Long memberNo, Long inquiryNo);
}
