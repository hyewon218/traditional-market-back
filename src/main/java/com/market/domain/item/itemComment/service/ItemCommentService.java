package com.market.domain.item.itemComment.service;

import com.market.domain.item.itemComment.dto.ItemCommentRequestDto;
import com.market.domain.item.itemComment.dto.ItemCommentResponseDto;
import com.market.domain.item.itemComment.entity.ItemComment;
import com.market.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemCommentService {

    void createItemComment(ItemCommentRequestDto itemCommentRequestDto, Member member);

    Page<ItemCommentResponseDto> getItemComments(Long itemNo, Pageable pageable);

    void updateItemComment(Long commentNo, ItemCommentRequestDto itemCommentRequestDto,
        Member member);

    void deleteItemComment(Long commentNo, Member member);

    ItemComment findItemComment(Long no);
}
