package com.market.domain.item.itemComment.service;

import com.market.domain.item.itemComment.dto.ItemCommentRequestDto;
import com.market.domain.item.itemComment.entity.ItemComment;
import com.market.domain.member.entity.Member;

public interface ItemCommentService {

    void createItemComment(ItemCommentRequestDto itemCommentRequestDto, Member member);

    void updateItemComment(Long commentNo, ItemCommentRequestDto itemCommentRequestDto,
        Member member);

    void deleteItemComment(Long commentNo, Member member);

    ItemComment findItemComment(Long no);
}
