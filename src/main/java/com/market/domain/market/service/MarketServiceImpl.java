package com.market.domain.market.service;

import com.market.domain.image.config.AwsS3upload;
import com.market.domain.image.config.ImageConfig;
import com.market.domain.image.entity.Image;
import com.market.domain.image.repository.ImageRepository;
import com.market.domain.market.dto.MarketRequestDto;
import com.market.domain.market.dto.MarketResponseDto;
import com.market.domain.market.entity.CategoryEnum;
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

import com.market.global.ip.IpService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final IpService ipService;

    @Override
    @Transactional // 시장 생성
    public MarketResponseDto createMarket(MarketRequestDto requestDto, List<MultipartFile> files)
        throws IOException {

        Market market = requestDto.toEntity();

        if (marketRepository.existsMarketByMarketName(requestDto.getMarketName())) {
            throw new BusinessException(ErrorCode.EXISTED_MARKET);
        }
        marketRepository.save(market);

        if (files != null) {
            for (MultipartFile file : files) {
                String fileUrl = awsS3upload.upload(file, "market " + market.getNo());

                if (imageRepository.existsByImageUrlAndMarket_No(fileUrl, market.getNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(market, fileUrl));
            }
        } else {
            // 시장 기본 이미지 추가
            if (!imageRepository.existsByImageUrlAndMarket_No(ImageConfig.DEFAULT_IMAGE_URL, market.getNo())) {
                imageRepository.save(new Image(market, ImageConfig.DEFAULT_IMAGE_URL));
            }
        }
        return MarketResponseDto.of(market);
    }

    @Override
    @Transactional(readOnly = true) // 시장 목록 조회
    public Page<MarketResponseDto> getMarkets(Pageable pageable) {
        Page<Market> marketList = marketRepository.findAll(pageable);
        return marketList.map(MarketResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 키워드 검색 시장 목록 조회
    public Page<MarketResponseDto> searchMarkets(MarketSearchCond cond, Pageable pageable) {
        return marketRepositoryQuery.searchMarkets(cond, pageable).map(MarketResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 카테고리별 상점 목록 조회
    public Page<MarketResponseDto> getCategoryMarkets(CategoryEnum category, Pageable pageable) {
        Page<Market> marketList = marketRepository.findByCategoryOrderByMarketName(category, pageable);
        return marketList.map(MarketResponseDto::of);
    }

    @Transactional // 시장 단건 조회 // IP 주소당 하루에 조회수 1회 증가
    public MarketResponseDto getMarket(Long marketNo, HttpServletRequest request) {
        Market market = findMarket(marketNo);

        String ipAddress = ipService.getIpAddress(request);

        if (!ipService.hasTypeBeenViewed(ipAddress, "market", marketNo)) {
            ipService.markTypeAsViewed(ipAddress, "market", marketNo);
            market.setViewCount(market.getViewCount() + 1);
        }
        return MarketResponseDto.of(market);
    }

    @Override
    @Transactional // 시장 수정
    public MarketResponseDto updateMarket(Long marketNo, MarketRequestDto requestDto,
        List<MultipartFile> files)
        throws IOException {
        Market market = findMarket(marketNo);

        market.update(requestDto);

        List<String> imageUrls = requestDto.getImageUrls(); // 클라이언트
        List<Image> existingImages = imageRepository.findByMarket_No(marketNo); // DB

        if (files != null) {
            for (MultipartFile file : files) {
                String fileUrl = awsS3upload.upload(file, "market " + market.getNo());

                if (imageRepository.existsByImageUrlAndMarket_No(fileUrl, market.getNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(market, fileUrl));
            }
            // 기본이미지와 새로 등록하려는 이미지가 함깨 존재할 경우 기본이미지 삭제
            if (imageRepository.existsByImageUrlAndMarket_No(ImageConfig.DEFAULT_IMAGE_URL, market.getNo())) {
                imageRepository.deleteByImageUrlAndMarket_No(ImageConfig.DEFAULT_IMAGE_URL, market.getNo());
            }
        } else if (imageUrls != null) { // 기존 이미지 중 삭제되지 않은(남은) 이미지만 남도록
            // 이미지 URL 비교 및 삭제
            for (Image existingImage : existingImages) {
                if (!imageUrls.contains(existingImage.getImageUrl())) {
                    imageRepository.delete(existingImage); // 클라이언트에서 삭제된 데이터 DB 삭제
                }
            }
        } else { // 기본이미지와 파일이 모두 null 이면 기본이미지 추가
            imageRepository.save(new Image(market, ImageConfig.DEFAULT_IMAGE_URL));
        }

        if (imageUrls == null) { // 기존 미리보기 이미지 전부 삭제 시 기존 DB image 삭제
            imageRepository.deleteAll(existingImages);
        }
        return MarketResponseDto.of(market);
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
    public boolean checkMarketLike(Long marketNo, Member member) { // 좋아요 여부 확인
        Market market = findMarket(marketNo);
        Optional<MarketLike> marketLike = marketLikeRepository.findByMarketAndMember(market, member);
        return marketLike.isPresent(); // 좋아요 존재하면 true
    }

    @Override
    @Transactional
    public void deleteMarketLike(Long marketNo, Member member) { // 좋아요 삭제
        Market market = findMarket(marketNo);
        Optional<MarketLike> marketLike = marketLikeRepository.findByMarketAndMember(market, member);

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
