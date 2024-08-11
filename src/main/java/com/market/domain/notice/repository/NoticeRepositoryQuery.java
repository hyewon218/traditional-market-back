package com.market.domain.notice.repository;

import com.market.domain.member.entity.Member;
import com.market.domain.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoticeRepositoryQuery {

    Page<Notice> searchNotices(NoticeSearchCond cond, Pageable pageable);
}