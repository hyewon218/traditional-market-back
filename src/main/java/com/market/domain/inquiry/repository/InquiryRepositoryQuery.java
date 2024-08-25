package com.market.domain.inquiry.repository;

import com.market.domain.inquiry.entity.Inquiry;
import com.market.domain.market.entity.Market;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InquiryRepositoryQuery {

    Page<Inquiry> searchInquiries(InquirySearchCond cond, Pageable pageable);
}