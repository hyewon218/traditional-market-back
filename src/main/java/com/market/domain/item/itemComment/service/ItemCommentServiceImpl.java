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
import com.market.global.profanityFilter.ProfanityFilter;
import java.util.List;
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
        // 댓글에 비속어 포함되어있는지 검증
        validationProfanity(itemCommentRequestDto.getComment());
        // 만약 회원 제재 여부가 true 면 댓글 작성 불가
        if (member.isWarning()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        Item item = itemRepository.findById(itemCommentRequestDto.getItemNo()).orElseThrow(
            () -> new BusinessException(ErrorCode.NOT_FOUND_ITEM)
        );
        // 댓글 저장
        itemCommentRepository.save(itemCommentRequestDto.toEntity(item, member));

        /*상품에 대한 판매자 번호가 등록되어 있지 않으면 관리자에게 알람*/
        // 상품의 판매자 확인
        Member seller = item.getShop().getSeller();
        if (seller == null) {
            // 판매자가 등록되어 있지 않은 경우, 모든 관리자에게 알림을 전송
            List<Member> adminList = memberRepository.findAllByRole(Role.ADMIN);
            // 관리자 리스트가 비어있는지 확인
            if (adminList.isEmpty()) {
                throw new BusinessException(ErrorCode.NOT_EXISTS_ADMIN);
            }
            // 관리자에게 알림을 보낼 경우
            NotificationArgs notificationArgs = NotificationArgs.of(member.getMemberNo(),
                item.getNo());
            // 모든 관리자에게 알림 전송
            for (Member admin : adminList) {
                notificationService.send(NotificationType.NEW_COMMENT_ON_ITEM, notificationArgs,
                    admin);
            }
        } else {
            // 판매자에게 알림을 보낼 경우
            NotificationArgs notificationArgs = NotificationArgs.of(member.getMemberNo(),
                item.getNo());
            notificationService.send(NotificationType.NEW_COMMENT_ON_ITEM, notificationArgs,
                seller);
        }
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

        // 댓글에 비속어 포함되어있는지 검증
        validationProfanity(itemCommentRequestDto.getComment());

        // 만약 회원 제재 여부가 true면 댓글 작성 불가
        if (member.isWarning()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACTION);
        }

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

    @Override
    public void validationProfanity(String comment) { // 댓글에 비속어 포함되어있는지 검증
        if (ProfanityFilter.containsProfanity(comment)) {
            throw new BusinessException(ErrorCode.NOT_ALLOW_PROFANITY_ITEM);
        }
    }
}
