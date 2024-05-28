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
import com.market.domain.shop.shopComment.entity.ShopComment;
import com.market.domain.shop.shopComment.repository.ShopCommentRepository;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
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
        // 선택한 상점에 댓글 등록
        Shop shop = shopRepository.findById(shopCommentRequestsDto.getShopNo()).orElseThrow(
            () -> new BusinessException(ErrorCode.NOT_FOUND_SHOP)
        );

        shopCommentRepository.save(shopCommentRequestsDto.toEntity(shop, member));

        // create alarm
        Member receiver;
        if (shop.getSeller() == null) { // 사장님이 등록되어 있지 않으면 관리자에게 알람이 가도록
            receiver = memberRepository.findByRole(Role.ADMIN).orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_EXISTS_ADMIN)
            );
        } else {
            receiver = shop.getSeller();
        }
        notificationService.send(
            NotificationType.NEW_COMMENT_ON_SHOP,
            new NotificationArgs(member.getMemberNo(), shop.getNo()), receiver);
    }

    @Override
    @Transactional
    public void updateShopComment(Long commentId, ShopCommentRequestDto shopCommentRequestsDto,
        Member member) {
        ShopComment shopComment = findShopComment(commentId);

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
}
