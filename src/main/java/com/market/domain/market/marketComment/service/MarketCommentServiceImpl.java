package com.market.domain.market.marketComment.service;

import com.market.domain.market.entity.Market;
import com.market.domain.market.marketComment.dto.MarketCommentRequestDto;
import com.market.domain.market.marketComment.entity.MarketComment;
import com.market.domain.market.marketComment.repository.MarketCommentRepository;
import com.market.domain.market.repository.MarketRepository;
import com.market.domain.member.entity.Member;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarketCommentServiceImpl implements MarketCommentService {

    private final MarketCommentRepository marketCommentRepository;
    private final MarketRepository marketRepository;

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