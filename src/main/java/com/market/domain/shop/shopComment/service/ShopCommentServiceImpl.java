package com.market.domain.shop.shopComment.service;

import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.domain.notification.constant.NotificationType;
import com.market.domain.notification.entity.NotificationArgs;
import com.market.domain.notification.service.NotificationService;
import com.market.domain.shop.entity.Shop;
import com.market.domain.shop.repository.ShopRepository;
import com.market.domain.shop.shopComment.dto.ShopCommentRequestDto;
import com.market.domain.shop.shopComment.dto.ShopCommentResponseDto;
import com.market.domain.shop.shopComment.entity.ShopComment;
import com.market.domain.shop.shopComment.repository.ShopCommentRepository;
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
public class ShopCommentServiceImpl implements ShopCommentService {

    private final ShopCommentRepository shopCommentRepository;
    private final ShopRepository shopRepository;
    private final NotificationService notificationService;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void createShopComment(ShopCommentRequestDto shopCommentRequestsDto,
        Member member) {
        // 댓글에 비속어 포함되어있는지 검증
        validationProfanity(shopCommentRequestsDto.getComment());
        // 만약 회원 제재 여부가 true 면 댓글 작성 불가
        if (member.isWarning()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        Shop shop = shopRepository.findById(shopCommentRequestsDto.getShopNo()).orElseThrow(
            () -> new BusinessException(ErrorCode.NOT_FOUND_SHOP)
        );
        // 댓글 저장
        shopCommentRepository.save(shopCommentRequestsDto.toEntity(shop, member));

        /*상점 판매자 번호가 등록되어 있지 않으면 관리자에게 알람*/
        // 상점의 판매자 확인
        Member seller = shop.getSeller();
        if (seller == null) {
            // 판매자가 등록되어 있지 않은 경우, 모든 관리자에게 알림을 전송
            List<Member> adminList = memberRepository.findAllByRole(Role.ADMIN);
            // 관리자 리스트가 비어 있는지 확인
            if (adminList.isEmpty()) {
                throw new BusinessException(ErrorCode.NOT_EXISTS_ADMIN);
            }
            // 관리자에게 알림을 보낼 경우
            NotificationArgs notificationArgs = NotificationArgs.of(member.getMemberNo(),
                shop.getNo());
            // 모든 관리자에게 알림 전송
            for (Member admin : adminList) {
                notificationService.send(NotificationType.NEW_COMMENT_ON_SHOP, notificationArgs,
                    admin);
            }
        } else {
            // 판매자에게 알림을 보낼 경우
            NotificationArgs notificationArgs = NotificationArgs.of(member.getMemberNo(),
                shop.getNo());
            notificationService.send(NotificationType.NEW_COMMENT_ON_SHOP, notificationArgs,
                seller);
        }
    }

    @Override
    @Transactional(readOnly = true) // 상점 댓글 목록 조회
    public Page<ShopCommentResponseDto> getShopComments(Long shopNo, Pageable pageable) {
        Page<ShopComment> shopList = shopCommentRepository.findAllByShop_NoOrderByCreateTimeDesc(
            shopNo, pageable);
        return shopList.map(ShopCommentResponseDto::of);
    }

    @Override
    @Transactional
    public void updateShopComment(Long commentId, ShopCommentRequestDto shopCommentRequestsDto,
        Member member) {
        ShopComment shopComment = findShopComment(commentId);

        // 댓글에 비속어 포함되어있는지 검증
        validationProfanity(shopCommentRequestsDto.getComment());

        // 만약 회원 제재 여부가 true면 댓글 작성 불가
        if (member.isWarning()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        if (!member.getMemberNo().equals(shopComment.getMember().getMemberNo())) {
            throw new BusinessException(ErrorCode.NOT_USER_SHOP_UPDATE);
        }
        shopComment.updateComment(shopCommentRequestsDto);
    }

    @Override
    @Transactional
    public void deleteShopComment(Long commentId, Member member) {
        ShopComment postComment = findShopComment(commentId);

        if (!member.getMemberNo().equals(postComment.getMember().getMemberNo())) {
            throw new BusinessException(ErrorCode.NOT_USER_SHOP_DELETE);
        }
        shopCommentRepository.delete(postComment);
    }

    @Override
    public ShopComment findShopComment(Long no) {
        return shopCommentRepository.findById(no)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_SHOP_COMMENT));
    }

    @Override
    public void validationProfanity(String comment) { // 댓글에 비속어 포함되어있는지 검증
        if (ProfanityFilter.containsProfanity(comment)) {
            throw new BusinessException(ErrorCode.NOT_ALLOW_PROFANITY_SHOP);
        }
    }
}
