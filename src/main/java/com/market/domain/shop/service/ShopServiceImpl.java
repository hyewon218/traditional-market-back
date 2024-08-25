package com.market.domain.shop.service;

import com.market.domain.image.config.AwsS3upload;
import com.market.domain.image.config.ImageConfig;
import com.market.domain.image.entity.Image;
import com.market.domain.image.repository.ImageRepository;
import com.market.domain.market.entity.Market;
import com.market.domain.market.repository.MarketRepository;
import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.domain.notification.constant.NotificationType;
import com.market.domain.notification.entity.NotificationArgs;
import com.market.domain.notification.service.NotificationService;
import com.market.domain.shop.dto.ShopRequestDto;
import com.market.domain.shop.dto.ShopResponseDto;
import com.market.domain.shop.entity.CategoryEnum;
import com.market.domain.shop.entity.Shop;
import com.market.domain.shop.repository.ShopRepository;
import com.market.domain.shop.repository.ShopRepositoryQuery;
import com.market.domain.shop.repository.ShopSearchCond;
import com.market.domain.shop.shopLike.entity.ShopLike;
import com.market.domain.shop.shopLike.repository.ShopLikeRepository;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import com.market.global.ip.IpService;
import jakarta.servlet.http.HttpServletRequest;
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
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final MarketRepository marketRepository;
    private final ImageRepository imageRepository;
    private final AwsS3upload awsS3upload;
    private final ShopLikeRepository shopLikeRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final IpService ipService;
    private final ShopRepositoryQuery shopRepositoryQuery;

    @Override
    @Transactional // 상점 생성
    public ShopResponseDto createShop(ShopRequestDto requestDto, List<MultipartFile> files)
        throws IOException {
        // 선택한 시장에 상점 등록
        Market market = marketRepository.findById(requestDto.getMarketNo()).orElseThrow(
            () -> new BusinessException(ErrorCode.NOT_FOUND_MARKET)
        );
        // 상점 등록 시 사장님이 가입되어 있다면 사장님 member 정보 추가
        Shop shop;
        if (requestDto.getSellerNo() != null) {
            Member seller = memberRepository.findById(requestDto.getSellerNo()).orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_EXISTS_SELLER)
            );
            shop = requestDto.toEntity(market, seller);
        } else {
            shop = requestDto.toEntity(market);
        }
        shopRepository.save(shop);

        if (files != null) {
            for (MultipartFile file : files) {
                String fileUrl = awsS3upload.upload(file, "shop " + shop.getNo());
                if (imageRepository.existsByImageUrlAndShop_No(fileUrl, shop.getNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(shop, fileUrl));
            }
        } else {
            // 시장 기본 이미지 추가
            addDefaultImageIfNotExists(shop);
        }
        return ShopResponseDto.of(shop);
    }

    @Override
    @Transactional(readOnly = true) // 상점 목록 조회
    public Page<ShopResponseDto> getShops(Pageable pageable) {
        Page<Shop> shopList = shopRepository.findAll(pageable);
        return shopList.map(ShopResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 시장 내 상점 목록 조회
    public Page<ShopResponseDto> getShopsByMarketNo(Long marketNo, Pageable pageable) {
        Page<Shop> shopList = shopRepository.findAllByMarket_No(marketNo, pageable);
        return shopList.map(ShopResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 상점 카테고리 목록 조회
    public Page<ShopResponseDto> getCategoryShop(CategoryEnum category, Pageable pageable) {
        Page<Shop> shopList = shopRepository.findByCategoryOrderByCategoryDesc(category, pageable);
        return shopList.map(ShopResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 키워드 검색 상점 목록 조회
    public Page<ShopResponseDto> searchShops(ShopSearchCond cond, Pageable pageable) {
        return shopRepositoryQuery.searchShops(cond, pageable).map(ShopResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 특정 시장 내 상점 카테고리별 조회
    public Page<ShopResponseDto> getShopsByCategory(Long marketNo,
        CategoryEnum category, Pageable pageable) {
        Page<Shop> shopList = shopRepository.findByMarketNoAndCategory(marketNo, category,
            pageable);
        return shopList.map(ShopResponseDto::of);
    }

    @Transactional // 상점 단건 조회 // IP 주소당 하루에 조회수 1회 증가
    public ShopResponseDto getShop(Long shopNo, HttpServletRequest request) {
        Shop shop = findShop(shopNo);

        String ipAddress = ipService.getIpAddress(request);

        if (!ipService.hasTypeBeenViewed(ipAddress, "shop", shopNo)) {
            ipService.markTypeAsViewed(ipAddress, "shop", shopNo);
            shop.setViewCount(shop.getViewCount() + 1);
        }
        return ShopResponseDto.of(shop);
    }

    @Override
    @Transactional // 상점 수정
    public ShopResponseDto updateShop(Long shopNo, ShopRequestDto requestDto,
        List<MultipartFile> files) throws IOException {
        Shop shop = findShop(shopNo);

        // 사장님 정보 추가 또는 상점 정보 업데이트
        if (requestDto.getSellerNo() != null) {
            // 사장님이 후에 가입한다면 상점 수정으로 사장님 member 정보 추가
            Member seller = memberRepository.findById(requestDto.getSellerNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_SELLER));
            shop.updateShopSeller(requestDto, seller);
        } else {
            shop.updateShop(requestDto);
        }

        List<String> imageUrls = requestDto.getImageUrls(); // 클라이언트로부터 받은 이미지 URL
        List<Image> existingImages = imageRepository.findByShop_No(shopNo); // DB 에서 가져온 기존 이미지들

        // 새 파일이 업로드된 경우
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String fileUrl = awsS3upload.upload(file, "shop " + shop.getNo());

                if (imageRepository.existsByImageUrlAndShop_No(fileUrl, shop.getNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(shop, fileUrl));
            }
            // 새 이미지가 추가되면 기본 이미지를 삭제
            deleteDefaultImageIfExists(shop.getNo());
        } else if (imageUrls != null) { // 기존 이미지 중 클라이언트에서 제거된 이미지를 삭제
            for (Image existingImage : existingImages) {
                if (!imageUrls.contains(existingImage.getImageUrl())) {
                    deleteImage(existingImage);
                }
            }
        } else { // 파일과 이미지 URL 모두 없으면 기본 이미지 추가
            addDefaultImageIfNotExists(shop);
        }

        // 클라이언트에서 모든 이미지 URL 을 제거한 경우 기존 이미지를 전부 삭제
        if (imageUrls == null || imageUrls.isEmpty()) {
            deleteAllImages(existingImages);
        }
        return ShopResponseDto.of(shop);
    }

    private void deleteDefaultImageIfExists(Long shopNo) {
        if (imageRepository.existsByImageUrlAndShop_No(ImageConfig.DEFAULT_IMAGE_URL, shopNo)) {
            imageRepository.deleteByImageUrlAndShop_No(ImageConfig.DEFAULT_IMAGE_URL, shopNo);
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
        imageRepository.delete(image); // DB 에서 이미지 삭제
    }

    private void addDefaultImageIfNotExists(Shop shop) {
        if (!imageRepository.existsByImageUrlAndShop_No(ImageConfig.DEFAULT_IMAGE_URL,
            shop.getNo())) {
            imageRepository.save(new Image(shop, ImageConfig.DEFAULT_IMAGE_URL));
        }
    }

    @Override
    @Transactional // 상점 삭제
    public void deleteShop(Long shopNo) {
        Shop shop = findShop(shopNo);
        List<Image> images = imageRepository.findByShop_No(shopNo);
        // 이미지가 존재하는 경우에만 S3 삭제 작업 수행
        if (images != null && !images.isEmpty()) {
            for (Image image : images) {
                if (!image.getImageUrl().equals(ImageConfig.DEFAULT_IMAGE_URL)) {
                    awsS3upload.delete(image.getImageUrl()); // 기본이미지 제외 S3 에서도 이미지 삭제
                }
            }
        }
        shopRepository.delete(shop);
    }

    @Override
    @Transactional // 좋아요 생성
    public void createShopLike(Long shopNo, Member member) {
        shopLikeRepository.findByShopNoAndMember(shopNo, member).ifPresent(shopLike -> {
            throw new BusinessException(ErrorCode.EXISTS_SHOP_LIKE);
        });

        Shop shop = findShop(shopNo);
        shopLikeRepository.save(new ShopLike(shop, member));

        // create alarm
        Member receiver;
        if (shop.getSeller() == null) { // 사장님이 등록되어 있지 않으면 관리자에게 알람이 가도록
            receiver = memberRepository.findByRole(Role.ADMIN).orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_EXISTS_ADMIN)
            );
        } else {
            receiver = shop.getSeller();
        }
        NotificationArgs notificationArgs = NotificationArgs.builder()
            .fromMemberNo(member.getMemberNo())
            .targetId(shop.getNo())
            .build();
        notificationService.send(
            NotificationType.NEW_LIKE_ON_SHOP, notificationArgs, receiver);
    }

    @Override
    @Transactional // 좋아요 여부 확인
    public boolean checkShopLike(Long shopNo, Member member) {
        Optional<ShopLike> shopLike = shopLikeRepository.findByShopNoAndMember(shopNo, member);
        return shopLike.isPresent(); // 좋아요 존재하면 true
    }

    @Override
    @Transactional // 좋아요 삭제
    public void deleteShopLike(Long shopNo, Member member) {
        Optional<ShopLike> shopLike = shopLikeRepository.findByShopNoAndMember(shopNo, member);

        if (shopLike.isPresent()) {
            shopLikeRepository.delete(shopLike.get());
        } else {
            throw new BusinessException(ErrorCode.NOT_EXISTS_SHOP_LIKE);
        }
    }

    @Override // 좋아요 수 조회
    @Transactional(readOnly = true)
    public Long countShopLikes(Long shopNo) {
        return shopLikeRepository.countByShopNo(shopNo);
    }

    @Override // 시장 찾기
    public Shop findShop(Long shopNo) {
        return shopRepository.findById(shopNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_SHOP));
    }

    @Override // 총 상점 수
    @Transactional(readOnly = true)
    public Long countShops() {
        return shopRepository.count();
    }

    @Override // 시장별 상점 수
    @Transactional(readOnly = true)
    public Long countShopsByMarket(Long marketNo) {
        return shopRepository.countByMarket_No(marketNo);
    }

    @Override
    @Transactional(readOnly = true) // 판매자가 소유한 상점 목록 조회 (판매자 본인이 본인의 상점 목록 조회)
    public Page<ShopResponseDto> getShopsBySellerNo(Member seller, Pageable pageable) {
        if (!seller.getRole().equals(Role.SELLER)) { // 로그인한 유저가 SELLER가 아니면 접근 차단
            throw new BusinessException(ErrorCode.NOT_EXISTS_SELLER);
        }
        Page<Shop> shops = shopRepository.findBySeller_MemberNo(seller.getMemberNo(), pageable);
        return shops.map(ShopResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 판매자가 소유한 상점 목록 조회 (관리자가 특정 판매자의 상점 목록 조회)
    public Page<ShopResponseDto> getShopsBySellerNoAdmin(Member member, Long sellerNo, Pageable pageable) {
        if (!member.getRole().equals(Role.ADMIN)) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_ADMIN);
        }
        Page<Shop> shops = shopRepository.findBySeller_MemberNo(sellerNo, pageable);
        return shops.map(ShopResponseDto::of);
    }
}
