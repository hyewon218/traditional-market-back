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
import com.market.domain.shop.shopLike.entity.ShopLike;
import com.market.domain.shop.shopLike.repository.ShopLikeRepository;
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
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final MarketRepository marketRepository;
    private final ImageRepository imageRepository;
    private final AwsS3upload awsS3upload;
    private final ShopLikeRepository shopLikeRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

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
            if (!imageRepository.existsByImageUrlAndShop_No(ImageConfig.DEFAULT_IMAGE_URL, market.getNo())) {
                imageRepository.save(new Image(market, ImageConfig.DEFAULT_IMAGE_URL));
            }
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
    @Transactional(readOnly = true) // 상점 목록 조회
    public Page<ShopResponseDto> getCategoryShop(CategoryEnum category, Pageable pageable) {
        Page<Shop> shopList = shopRepository.findByCategoryOrderByCategoryDesc(category, pageable);
        return shopList.map(ShopResponseDto::of);
    }

    @Transactional(readOnly = true) // 상점 단건 조회
    public ShopResponseDto getShop(Long shopNo) {
        Shop shop = findShop(shopNo);
        return ShopResponseDto.of(shop);
    }

    @Override
    @Transactional // 상점 수정
    public ShopResponseDto updateShop(Long shopNo, ShopRequestDto requestDto, List<MultipartFile> files)
        throws IOException {
        Shop shop = findShop(shopNo);

        if (requestDto.getSellerNo() != null) { // 사장님이 후에 가입한다면 상점 수정으로 사장님 member 정보 추가
            Member seller = memberRepository.findById(requestDto.getSellerNo()).orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_EXISTS_SELLER)
            );
            shop.updateShopSeller(requestDto, seller);
        } else {
            shop.updateShop(requestDto);
        }

        List<String> imageUrls = requestDto.getImageUrls(); // 클라이언트
        List<Image> existingImages = imageRepository.findByShop_No(shopNo); // DB


        if (files != null) {
            for (MultipartFile file : files) {
                String fileUrl = awsS3upload.upload(file, "market " + shop.getNo());

                if (imageRepository.existsByImageUrlAndShop_No(fileUrl, shop.getNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(shop, fileUrl));
            }
            // 기본이미지와 새로 등록하려는 이미지가 함깨 존재할 경우 기본이미지 삭제
            if (imageRepository.existsByImageUrlAndShop_No(ImageConfig.DEFAULT_IMAGE_URL, shop.getNo())) {
                imageRepository.deleteByImageUrlAndShop_No(ImageConfig.DEFAULT_IMAGE_URL, shop.getNo());
            }
        } else if (imageUrls != null) { // 기존 이미지 중 삭제되지 않은(남은) 이미지만 남도록
            // 이미지 URL 비교 및 삭제
            for (Image existingImage : existingImages) {
                if (!imageUrls.contains(existingImage.getImageUrl())) {
                    imageRepository.delete(existingImage); // 클라이언트에서 삭제된 데이터 DB 삭제
                }
            }
        } else { // 기본이미지와 파일이 모두 null 이면 기본이미지 추가
            imageRepository.save(new Image(shop, ImageConfig.DEFAULT_IMAGE_URL));
        }

        if (imageUrls == null) { // 기존 미리보기 이미지 전부 삭제 시 기존 DB image 삭제
            imageRepository.deleteAll(existingImages);
        }
        return ShopResponseDto.of(shop);
    }

    @Override
    @Transactional // 상점 삭제
    public void deleteShop(Long shopNo) {
        Shop shop = findShop(shopNo);
        shopRepository.delete(shop);
    }

    @Override
    @Transactional
    public void createShopLike(Long shopNo, Member member) { // 좋아요 생성
        Shop shop = findShop(shopNo);

        shopLikeRepository.findByShopAndMember(shop, member).ifPresent(shopLike -> {
            throw new BusinessException(ErrorCode.EXISTS_SHOP_LIKE);
        });
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
        notificationService.send(
            NotificationType.NEW_LIKE_ON_SHOP,
            new NotificationArgs(member.getMemberNo(), shop.getNo()), receiver);
    }

    @Override
    @Transactional
    public void deleteShopLike(Long shopNo, Member member) { // 좋아요 삭제
        Shop shop = findShop(shopNo);
        Optional<ShopLike> shopLike = shopLikeRepository.findByShopAndMember(shop, member);

        if (shopLike.isPresent()) {
            shopLikeRepository.delete(shopLike.get());
        } else {
            throw new BusinessException(ErrorCode.NOT_EXISTS_SHOP_LIKE);
        }
    }

    @Override // 시장 찾기
    public Shop findShop(Long shopNo) {
        return shopRepository.findById(shopNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_SHOP));
    }
}
