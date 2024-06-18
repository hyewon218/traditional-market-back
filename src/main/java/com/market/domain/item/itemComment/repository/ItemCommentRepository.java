package com.market.domain.item.itemComment.repository;

import com.market.domain.item.itemComment.entity.ItemComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemCommentRepository extends JpaRepository<ItemComment, Long> {
    Page<ItemComment> findAllByItem_No(Long no, Pageable pageable);
}
