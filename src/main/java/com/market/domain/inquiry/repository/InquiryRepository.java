package com.market.domain.inquiry.repository;

import com.market.domain.inquiry.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    Page<Inquiry> findAllByMemberNo(long memberNo, Pageable pageable);

    void deleteAllByMemberNo(long memberNo);

    Page<Inquiry> findAll(Pageable pageable);
    // 하루동안 작성한 문의사항 개수 조회
    int countByMemberNoAndCreateTimeBetween(long memberNo, LocalDateTime startOfDay, LocalDateTime endOfDay); // 하루동안 작성한 문의사항 개수 조회
}
