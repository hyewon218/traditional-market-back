package com.market.domain.market.marketComment.service;

import com.market.domain.kafka.producer.NotificationProducer;
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
import com.market.global.profanityFilter.ProfanityFilter;
import java.util.List;
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
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final NotificationProducer notificationProducer;

    @Override
    @Transactional
    public void createMarketComment(MarketCommentRequestDto marketCommentRequestDto,
        Member member) {
        // 댓글에 비속어 포함되어있는지 검증
        validationProfanity(marketCommentRequestDto.getComment());
        // 만약 회원 제재 여부가 true 면 댓글 작성 불가
        if (member.isWarning()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        // 선택한 시장에 댓글 등록
        Market market = marketRepository.findById(marketCommentRequestDto.getMarketNo())
            .orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_FOUND_MARKET)
            );
        marketCommentRepository.save(marketCommentRequestDto.toEntity(market, member));

        /*관리자에게 알람*/
        List<Member> adminList = memberRepository.findAllByRole(Role.ADMIN);
        // 관리자 리스트가 비어있지 않은지 확인
        if (adminList.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_ADMIN);
        }
        NotificationArgs notificationArgs = NotificationArgs.of(member.getMemberNo(),
            market.getNo());
        // 모든 관리자에게 알림 전송
        for (Member admin : adminList) {
            notificationService.send(
                NotificationType.NEW_COMMENT_ON_MARKET, notificationArgs, admin.getMemberNo());
           /* notificationProducer.send(
                new NotificationEvent(NotificationType.NEW_COMMENT_ON_MARKET, notificationArgs,
                    admin.getMemberNo()));*/
        }
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

        // 댓글에 비속어 포함되어있는지 검증
        validationProfanity(marketCommentRequestDto.getComment());

        // 만약 회원 제재 여부가 true면 댓글 작성 불가
        if (member.isWarning()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACTION);
        }

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

    @Override
    public void validationProfanity(String comment) { // 댓글에 비속어 포함되어있는지 검증
        if (ProfanityFilter.containsProfanity(comment)) {
            throw new BusinessException(ErrorCode.NOT_ALLOW_PROFANITY_MARKET);
        }
    }
}