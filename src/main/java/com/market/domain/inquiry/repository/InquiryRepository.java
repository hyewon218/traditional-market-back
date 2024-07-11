package com.market.domain.inquiry.repository;

import com.market.domain.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    void deleteAllByInquiryWriter(String memberId);
    List<Inquiry> findAllByMemberNo(long memberNo);

    void deleteAllByMemberNo(long memberNo);
}
