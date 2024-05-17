package com.market.domain.item.itemComment.repository;

import com.market.domain.item.itemComment.entity.ItemComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemCommentRepository extends JpaRepository<ItemComment, Long> {
}
