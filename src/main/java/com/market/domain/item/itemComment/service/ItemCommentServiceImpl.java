package com.market.domain.item.itemComment.service;

import com.market.domain.item.entity.Item;
import com.market.domain.item.itemComment.dto.ItemCommentRequestDto;
import com.market.domain.item.itemComment.dto.ItemCommentResponseDto;
import com.market.domain.item.itemComment.entity.ItemComment;
import com.market.domain.item.itemComment.repository.ItemCommentRepository;
import com.market.domain.item.repository.ItemRepository;
import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.domain.notification.constant.NotificationType;
import com.market.domain.notification.entity.NotificationArgs;
import com.market.domain.notification.service.NotificationService;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemCommentServiceImpl implements ItemCommentService {

    private final ItemCommentRepository itemCommentRepository;
    private final ItemRepository itemRepository;
    private final NotificationService notificationService;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void createItemComment(ItemCommentRequestDto itemCommentRequestDto,
        Member member) {
        // 선택한 상품에 댓글 등록
        Item item = itemRepository.findById(itemCommentRequestDto.getItemNo()).orElseThrow(
            () -> new BusinessException(ErrorCode.NOT_FOUND_ITEM)
        );

        itemCommentRepository.save(itemCommentRequestDto.toEntity(item, member));

        // create alarm
        Member receiver;
        if (item.getShop().getSeller() == null) { // 사장님이 등록되어 있지 않으면 관리자에게 알람이 가도록
            receiver = memberRepository.findByRole(Role.ADMIN).orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_EXISTS_ADMIN)
            );
        } else {
            receiver = item.getShop().getSeller();
        }
        NotificationArgs notificationArgs = NotificationArgs.builder()
            .fromMemberNo(member.getMemberNo())
            .targetId(item.getShop().getNo())
            .build();
        notificationService.send(
            NotificationType.NEW_COMMENT_ON_ITEM, notificationArgs, receiver);
    }

    @Override
    @Transactional(readOnly = true) // 상품 댓글 목록 조회
    public Page<ItemCommentResponseDto> getItemComments(Long itemNo, Pageable pageable) {
        Page<ItemComment> itemList = itemCommentRepository.findAllByItem_NoOrderByCreateTimeDesc(
            itemNo, pageable);
        return itemList.map(ItemCommentResponseDto::of);
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
