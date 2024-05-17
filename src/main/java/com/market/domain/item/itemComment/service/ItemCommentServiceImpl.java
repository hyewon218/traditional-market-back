package com.market.domain.item.itemComment.service;

import com.market.domain.item.entity.Item;
import com.market.domain.item.itemComment.dto.ItemCommentRequestDto;
import com.market.domain.item.itemComment.entity.ItemComment;
import com.market.domain.item.itemComment.repository.ItemCommentRepository;
import com.market.domain.item.repository.ItemRepository;
import com.market.domain.member.entity.Member;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemCommentServiceImpl implements ItemCommentService {

    private final ItemCommentRepository itemCommentRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public void createItemComment(ItemCommentRequestDto itemCommentRequestDto,
        Member member) {
        // 선택한 상품에 댓글 등록
        Item item = itemRepository.findById(itemCommentRequestDto.getItemNo()).orElseThrow(
            () -> new BusinessException(ErrorCode.NOT_FOUND_ITEM)
        );

        itemCommentRepository.save(itemCommentRequestDto.toEntity(item, member));
    }

    @Override
    @Transactional
    public void updateItemComment(Long commentId, ItemCommentRequestDto itemCommentRequestDto,
        Member member) {
        ItemComment itemComment = findItemComment(commentId);

        if (!member.getMemberNo().equals(itemComment.getMember().getMemberNo())) {
            throw new BusinessException(ErrorCode.NOT_USER_ITEM_UPDATE);
        }
        itemComment.updateComment(itemCommentRequestDto);
    }

    @Override
    @Transactional
    public void deleteItemComment(Long commentId, Member member) {
        ItemComment itemComment = findItemComment(commentId);

        if (!member.getMemberNo().equals(itemComment.getMember().getMemberNo())) {
            throw new BusinessException(ErrorCode.NOT_USER_ITEM_DELETE);
        }
        itemCommentRepository.delete(itemComment);
    }

    @Override
    public ItemComment findItemComment(Long no) {
        return itemCommentRepository.findById(no)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ITEM_COMMENT));
    }
}
