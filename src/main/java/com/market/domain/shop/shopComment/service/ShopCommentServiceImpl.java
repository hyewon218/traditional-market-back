package com.market.domain.shop.shopComment.service;

import com.market.domain.member.entity.Member;
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

    @Override
    @Transactional
    public void createShopComment(ShopCommentRequestDto shopCommentRequestsDto,
        Member member) {
        // 선택한 상점에 댓글 등록
        Shop shop = shopRepository.findById(shopCommentRequestsDto.getShopNo()).orElseThrow(
            () -> new BusinessException(ErrorCode.NOT_FOUND_SHOP)
        );

        shopCommentRepository.save(shopCommentRequestsDto.toEntity(shop, member));
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
