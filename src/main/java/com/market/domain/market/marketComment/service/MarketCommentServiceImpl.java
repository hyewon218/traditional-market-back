package com.market.domain.market.marketComment.service;

import com.market.domain.market.entity.Market;
import com.market.domain.market.marketComment.dto.MarketCommentRequestDto;
import com.market.domain.market.marketComment.dto.MarketCommentResponseDto;
import com.market.domain.market.marketComment.entity.MarketComment;
import com.market.domain.market.marketComment.repository.MarketCommentRepository;
import com.market.domain.market.repository.MarketRepository;
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
public class MarketCommentServiceImpl implements MarketCommentService {

    private final MarketCommentRepository marketCommentRepository;
    private final MarketRepository marketRepository;
    private final NotificationService notificationService;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void createMarketComment(MarketCommentRequestDto marketCommentRequestDto,
        Member member) {
        // 선택한 시장에 댓글 등록
        Market market = marketRepository.findById(marketCommentRequestDto.getMarketNo())
            .orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_FOUND_MARKET)
            );
        marketCommentRepository.save(marketCommentRequestDto.toEntity(market, member));

        // create alarm
        Member receiver = memberRepository.findByRole(Role.ADMIN).orElseThrow(
            () -> new BusinessException(ErrorCode.NOT_EXISTS_ADMIN));

        NotificationArgs notificationArgs = NotificationArgs.builder()
            .fromMemberNo(member.getMemberNo())
            .targetId(market.getNo())
            .build();
        notificationService.send(
            NotificationType.NEW_COMMENT_ON_MARKET, notificationArgs, receiver);
    }

    @Override
    @Transactional(readOnly = true) // 시장 댓글 목록 조회
    public Page<MarketCommentResponseDto> getMarketComments(Long marketNo, Pageable pageable) {
        Page<MarketComment> marketList = marketCommentRepository.findAllByMarket_NoOrderByCreateTimeDesc(
            marketNo, pageable);
        return marketList.map(MarketCommentResponseDto::of);
    }


    @Override
    @Transactional
    public void updateMarketComment(Long commentId, MarketCommentRequestDto marketCommentRequestDto,
        Member member) {
        MarketComment marketComment = findMarketComment(commentId);

        if (!member.getMemberNo().equals(marketComment.getMember().getMemberNo())) {
            throw new BusinessException(ErrorCode.NOT_USER_MARKET_UPDATE);
        }
        marketComment.updateComment(marketCommentRequestDto);
    }

    @Override
    @Transactional
    public void deleteMarketComment(Long commentId, Member member) {
        MarketComment postComment = findMarketComment(commentId);

        if (!member.getMemberNo().equals(postComment.getMember().getMemberNo())) {
            throw new BusinessException(ErrorCode.NOT_USER_MARKET_DELETE);
        }
        marketCommentRepository.delete(postComment);
    }

    @Override
    public MarketComment findMarketComment(Long no) {
        return marketCommentRepository.findById(no)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MARKET_COMMENT));
    }
}