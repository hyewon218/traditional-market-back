package com.market.domain.market.service;

import com.market.domain.image.config.AwsS3upload;
import com.market.domain.image.entity.Image;
import com.market.domain.image.repository.ImageRepository;
import com.market.domain.market.dto.MarketRequestDto;
import com.market.domain.market.dto.MarketResponseDto;
import com.market.domain.market.entity.Market;
import com.market.domain.market.marketLike.entity.MarketLike;
import com.market.domain.market.marketLike.repository.MarketLikeRepository;
import com.market.domain.market.repository.MarketRepository;
import com.market.domain.market.repository.MarketRepositoryQuery;
import com.market.domain.market.repository.MarketSearchCond;
import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.domain.notification.constant.NotificationType;
import com.market.domain.notification.entity.NotificationArgs;
import com.market.domain.notification.service.NotificationService;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MarketServiceImpl implements MarketService {

    private final MarketRepository marketRepository;
    private final ImageRepository imageRepository;
    private final AwsS3upload awsS3upload;
    private final MarketLikeRepository marketLikeRepository;
    private final NotificationService notificationService;
    private final MemberRepository memberRepository;
    private final MarketRepositoryQuery marketRepositoryQuery;

    String defaultImageUrl = "https://upgrade-aws-config-storage.s3.ap-northeast-2.amazonaws.com/%E1%84%89%E1%85%B5%E1%84%8C%E1%85%A1%E1%86%BC%E1%84%80%E1%85%B5%E1%84%87%E1%85%A9%E1%86%AB%E1%84%8B%E1%85%B5%E1%84%86%E1%85%B5%E1%84%8C%E1%85%B5.jpg";

    @Override
    @Transactional // 시장 생성
    public void createMarket(MarketRequestDto requestDto, List<MultipartFile> files)
        throws IOException {

        Market market = requestDto.toEntity();

        if (marketRepository.existsMarketByMarketName(requestDto.getMarketName())) {
            throw new BusinessException(ErrorCode.EXISTED_MARKET);
        }
        marketRepository.save(market);

        if (files != null) {
            for (MultipartFile file : files) {
                String fileUrl = awsS3upload.upload(file, "market " + market.getNo());

                if (imageRepository.existsByImageUrlAndNo(fileUrl, market.getNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(market, fileUrl));
            }
        }
    }

    @Override
    @Transactional(readOnly = true) // 시장 목록 조회
    public Page<MarketResponseDto> getMarkets(Pageable pageable) {
        Page<Market> marketList = marketRepository.findAll(pageable);
        return marketList.map(MarketResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 키워드 검색 게시글 목록 조회
    public Page<MarketResponseDto> searchMarkets(MarketSearchCond cond, Pageable pageable) {
        return marketRepositoryQuery.searchMarkets(cond, pageable).map(MarketResponseDto::of);
    }

    @Transactional(readOnly = true) // 시장 단건 조회
    public MarketResponseDto getMarket(Long marketNo) {
        Market market = findMarket(marketNo);
        return MarketResponseDto.of(market);
    }

    @Override
    @Transactional // 시장 수정
    public void updateMarket(Long marketNo, MarketRequestDto requestDto, List<MultipartFile> files)
        throws IOException {
        Market market = findMarket(marketNo);

        market.update(requestDto);

        List<String> imageUrls = requestDto.getImageUrls(); // 클라이언트
        List<Image> existingImages = imageRepository.findByMarket_No(marketNo); // DB

        // 기존 이미지 중 삭제되지 않은(남은) 이미지만 남도록
        if (imageUrls != null) {
            // 이미지 URL 비교 및 삭제
            for (Image existingImage : existingImages) {
                if (!imageUrls.contains(existingImage.getImageUrl())) {
                    imageRepository.delete(existingImage); // 클라이언트에서 삭제된 데이터 DB 삭제
                }
            }
        } else { // 기존 이미지 전부 삭제 시(imageUrls = null) 기존 DB image 삭제
            imageRepository.deleteAll(existingImages);

            // 기본 이미지 추가
            if (!imageRepository.existsByImageUrlAndNo(defaultImageUrl, market.getNo())) {
                imageRepository.save(new Image(market, defaultImageUrl));
            }
        }

        if (files != null) {
            for (MultipartFile file : files) {
                String fileUrl = awsS3upload.upload(file, "market " + market.getNo());

                if (imageRepository.existsByImageUrlAndNo(fileUrl, market.getNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(market, fileUrl));
            }
        }
    }

    @Override
    @Transactional // 시장 삭제
    public void deleteMarket(Long marketNo) {
        Market market = findMarket(marketNo);
        marketRepository.delete(market);
    }

    @Override
    @Transactional
    public void createMarketLike(Long marketNo, Member member) { // 좋아요 생성
        Market market = findMarket(marketNo);

        marketLikeRepository.findByMarketAndMember(market, member).ifPresent(itemLike -> {
            throw new BusinessException(ErrorCode.EXISTS_ITEM_LIKE);
        });
        marketLikeRepository.save(new MarketLike(market, member));

        // create alarm
        Member receiver = memberRepository.findByRole(Role.ADMIN).orElseThrow(
            () -> new BusinessException(ErrorCode.NOT_EXISTS_ADMIN));
        notificationService.send(
            NotificationType.NEW_LIKE_ON_MARKET,
            new NotificationArgs(member.getMemberNo(), market.getNo()), receiver);
    }

    @Override
    @Transactional
    public void deleteMarketLike(Long marketNo, Member member) { // 좋아요 삭제
        Market market = findMarket(marketNo);
        Optional<MarketLike> marketLike = marketLikeRepository.findByMarketAndMember(market,
            member);

        if (marketLike.isPresent()) {
            marketLikeRepository.delete(marketLike.get());
        } else {
            throw new BusinessException(ErrorCode.NOT_EXISTS_ITEM_LIKE);
        }
    }

    @Override // 시장 찾기
    public Market findMarket(Long marketNo) {
        return marketRepository.findById(marketNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MARKET));
    }
}
