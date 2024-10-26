package com.market.domain.market.service;

import com.market.domain.image.config.AwsS3upload;
import com.market.domain.image.config.ImageConfig;
import com.market.domain.image.entity.Image;
import com.market.domain.image.repository.ImageRepository;
import com.market.domain.kafka.producer.NotificationProducer;
import com.market.domain.market.dto.MarketLikeResponseDto;
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
import com.market.global.ip.IpService;
import com.market.global.redis.RestPage;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final MemberRepository memberRepository;
    private final MarketRepositoryQuery marketRepositoryQuery;
    private final IpService ipService;
    private final NotificationService notificationService;
    private final NotificationProducer notificationProducer;

    @Override
    @Transactional // 시장 생성
    @CacheEvict(cacheNames = "markets", allEntries = true, cacheManager = "marketCacheManager") // 시장 생성 시 전체 시장 캐시도 삭제해서 동기화 문제 해결
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
            addDefaultImageIfNotExists(market);
        }
        return MarketResponseDto.of(market);
    }

    @Override
    @Transactional(readOnly = true) // 시장 목록 조회
    @Cacheable(cacheNames = "markets",
        key = "#pageable.pageNumber + '-' + #pageable.pageSize",
        cacheManager = "marketCacheManager"
    )
    public RestPage<MarketResponseDto> getMarkets(Pageable pageable) {
        Page<Market> marketPage = marketRepository.findAll(pageable);
        List<MarketResponseDto> dtoList = marketPage.getContent().stream()
            .map(MarketResponseDto::of)
            .toList();
        return new RestPage<>(dtoList, pageable.getPageNumber(), pageable.getPageSize(),
            marketPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true) // 키워드 검색 시장 목록 조회
    public Page<MarketResponseDto> searchMarkets(MarketSearchCond cond, Pageable pageable) {
        return marketRepositoryQuery.searchMarkets(cond, pageable).map(MarketResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 카테고리별 시장 목록 조회
    @Cacheable(
        cacheNames = "categoryMarkets",
        key = "#category.name + '-' + #pageable.pageNumber + '-' + #pageable.pageSize",
        cacheManager = "marketCacheManager",
        unless = "#result.content.isEmpty()"
    )
    public RestPage<MarketResponseDto> getCategoryMarkets(CategoryEnum category, Pageable pageable) {
        Page<Market> marketList = marketRepository.findByCategoryOrderByMarketName(category, pageable);

        // 만약 레디스에 데이터가 없을 경우 (캐시 미스 상황)
        if (marketList.isEmpty()) {
            return new RestPage<>(Collections.emptyList(), pageable.getPageNumber(),
                pageable.getPageSize(), 0);
        }

        List<MarketResponseDto> dtoList = marketList.getContent().stream()
            .map(MarketResponseDto::of)
            .toList();
        return new RestPage<>(dtoList, pageable.getPageNumber(), pageable.getPageSize(),
            marketList.getTotalElements());
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
    @CacheEvict(cacheNames = "markets", allEntries = true, cacheManager = "marketCacheManager") // 특정 시장 수정 시 전체 시장 캐시도 삭제해서 동기화 문제 해결
    public MarketResponseDto updateMarket(Long marketNo, MarketRequestDto requestDto,
        List<MultipartFile> files) throws IOException {
        Market market = findMarket(marketNo);
        market.update(requestDto);

        List<String> imageUrls = requestDto.getImageUrls(); // 클라이언트로부터 받은 이미지 URL
        List<Image> existingImages = imageRepository.findByMarket_No(marketNo); // DB 에서 가져온 기존 이미지들

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String fileUrl = awsS3upload.upload(file, "market " + marketNo);

                if (imageRepository.existsByImageUrlAndMarket_No(fileUrl, marketNo)) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(market, fileUrl));
            }
            // 새 이미지가 추가되면 기본 이미지를 삭제
            deleteDefaultImageIfExists(marketNo);
        } else if (imageUrls != null) { // 기존 이미지 중 클라이언트에서 제거된 이미지를 삭제
            for (Image existingImage : existingImages) {
                if (!imageUrls.contains(existingImage.getImageUrl())) {
                    deleteImage(existingImage);
                }
            }
        } else { // 파일과 이미지 URL 모두 없으면 기본 이미지 추가
            addDefaultImageIfNotExists(market);
        }

        // 클라이언트에서 모든 이미지 URL 을 제거한 경우 기존 이미지를 전부 삭제
        if (imageUrls == null || imageUrls.isEmpty()) {
            deleteAllImages(existingImages);
        }
        return MarketResponseDto.of(market);
    }

    private void deleteDefaultImageIfExists(Long marketNo) {
        if (imageRepository.existsByImageUrlAndMarket_No(ImageConfig.DEFAULT_IMAGE_URL, marketNo)) {
            imageRepository.deleteByImageUrlAndMarket_No(ImageConfig.DEFAULT_IMAGE_URL, marketNo);
        }
    }

    private void deleteAllImages(List<Image> images) {
        for (Image image : images) {
            deleteImage(image);
        }
    }

    private void deleteImage(Image image) {
        if (!image.getImageUrl().equals(ImageConfig.DEFAULT_IMAGE_URL)) {
            awsS3upload.delete(image.getImageUrl()); // S3에서 이미지 삭제
        }
        imageRepository.delete(image); // 클라이언트에서 저거된 데이터 DB 삭제
    }

    private void addDefaultImageIfNotExists(Market market) {
        if (!imageRepository.existsByImageUrlAndMarket_No(ImageConfig.DEFAULT_IMAGE_URL,
            market.getNo())) {
            imageRepository.save(new Image(market, ImageConfig.DEFAULT_IMAGE_URL));
        }
    }

    @Override
    @Transactional // 시장 삭제
    @CacheEvict(cacheNames = "markets", allEntries = true, cacheManager = "marketCacheManager") // 특정 시장 삭제 시 전체 시장 캐시도 삭제해서 동기화 문제 해결
    public void deleteMarket(Long marketNo) {
        Market market = findMarket(marketNo);
        List<Image> images = imageRepository.findByMarket_No(marketNo);

        // 이미지가 존재하는 경우에만 S3 삭제 작업 수행
        if (images != null && !images.isEmpty()) {
            for (Image image : images) {
                if (!image.getImageUrl().equals(ImageConfig.DEFAULT_IMAGE_URL)) {
                    awsS3upload.delete(image.getImageUrl()); // 기본이미지 제외 S3 에서도 이미지 삭제
                }
            }
        }
        marketRepository.delete(market);
    }

    @Override
    @Transactional // 좋아요 생성
    public void createMarketLike(Long marketNo, Member member) {
        marketLikeRepository.findByMarketNoAndMember(marketNo, member).ifPresent(itemLike -> {
            throw new BusinessException(ErrorCode.EXISTS_ITEM_LIKE);
        });

        Market market = findMarket(marketNo);
        marketLikeRepository.save(new MarketLike(market, member));

        /*관리자에게 알람*/
        List<Member> adminList = memberRepository.findAllByRole(Role.ADMIN);
        // 관리자 리스트가 비어있지 않은지 확인
        if (adminList.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_ADMIN);
        }
        NotificationArgs notificationArgs = NotificationArgs.of(member.getMemberNo(), marketNo);
        // 모든 관리자에게 알림 전송
        for (Member admin : adminList) {
            notificationService.send(
                NotificationType.NEW_LIKE_ON_MARKET, notificationArgs, admin.getMemberNo());
           /* notificationProducer.send(
                new NotificationEvent(NotificationType.NEW_LIKE_ON_MARKET, notificationArgs,
                    admin.getMemberNo()));*/
        }
    }

    @Override
    @Transactional // 좋아요 여부 확인
    public boolean checkMarketLike(Long marketNo, Member member) {
        Optional<MarketLike> marketLike = marketLikeRepository.findByMarketNoAndMember(marketNo,
            member);
        return marketLike.isPresent(); // 좋아요 존재하면 true
    }

    @Override
    @Transactional // 좋아요 삭제
    public void deleteMarketLike(Long marketNo, Member member) {
        Optional<MarketLike> marketLike = marketLikeRepository.findByMarketNoAndMember(marketNo,
            member);

        if (marketLike.isPresent()) {
            marketLikeRepository.delete(marketLike.get());
        } else {
            throw new BusinessException(ErrorCode.NOT_EXISTS_ITEM_LIKE);
        }
    }

    @Override
    @Transactional // 좋아요 수 조회
    public Long countMarketLikes(HttpServletRequest request, Long marketNo) {
        // 시장 조회수 증가 (특정 시장 조회 시 좋아요 수 조회는 무조건 일어나므로 여기에 시장 조회수 증가 로직 추가)
        addViewCount(request, marketNo);
        return marketLikeRepository.countByMarketNo(marketNo);
    }

    @Override
    @Transactional(readOnly = true) // 시장 좋아요 많은 순 조회
    public Page<MarketLikeResponseDto> getMarketsSortedByLikes(Pageable pageable) {
        return marketRepositoryQuery.findMarketsSortedByLikes(pageable);
    }

    @Override
    @Transactional(readOnly = true) // 시장 찾기
    public Market findMarket(Long marketNo) {
        return marketRepository.findById(marketNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MARKET));
    }

    @Override // 관리자인지 확인
    public void validateIsAdmin(Member member) {
        if (!member.getRole().equals(Role.ADMIN)) {
            throw new BusinessException(ErrorCode.ONLY_ADMIN_HAVE_AUTHORITY);
        }
    }

    @Override
    @Transactional(readOnly = true) // 총 시장 수 조회
    public Long getCountMarket() {
        return marketRepository.count();
    }

    @Override
    @Transactional(readOnly = true) // 시장별 총매출액 조회
    public Long getTotalSalesPrice(Long marketNo) {
        Market market = findMarket(marketNo);
        return market.getTotalSalesPrice();
    }

    @Override
    @Transactional(readOnly = true) // 모든 시장의 총매출액 합계 조회
    public Long getMarketSalesSum() {
        Long marketSalesSum = 0L;
        List<Market> marketList = marketRepository.findAll();
        for (Market market : marketList) {
            marketSalesSum += market.getTotalSalesPrice();
        }
        return marketSalesSum;
    }

    @Override
    @Transactional // 조회수 증가 로직
    public void addViewCount(HttpServletRequest request, Long marketNo) {
        Market market = findMarket(marketNo);
        String ipAddr = ipService.getIpAddress(request);
        if (!ipService.hasTypeBeenViewed(ipAddr, "market", marketNo)) {
            ipService.markTypeAsViewed(ipAddr, "market", marketNo);
            market.setViewCount(market.getViewCount() + 1);
        }
    }
}
