package com.market.domain.item.service;

import com.market.domain.image.config.AwsS3upload;
import com.market.domain.image.config.ImageConfig;
import com.market.domain.image.entity.Image;
import com.market.domain.image.repository.ImageRepository;
import com.market.domain.item.dto.ItemCategoryResponseDto;
import com.market.domain.item.dto.ItemRequestDto;
import com.market.domain.item.dto.ItemResponseDto;
import com.market.domain.item.dto.ItemTop5ResponseDto;
import com.market.domain.item.entity.Item;
import com.market.domain.item.entity.ItemCategoryEnum;
import com.market.domain.item.itemLike.entity.ItemLike;
import com.market.domain.item.itemLike.repository.ItemLikeRepository;
import com.market.domain.item.repository.ItemRepository;
import com.market.domain.item.repository.ItemRepositoryQuery;
import com.market.domain.item.repository.ItemSearchCond;
import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.domain.notification.constant.NotificationType;
import com.market.domain.notification.entity.NotificationArgs;
import com.market.domain.notification.service.NotificationService;
import com.market.domain.shop.entity.Shop;
import com.market.domain.shop.repository.ShopRepository;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import com.market.global.ip.IpService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ShopRepository shopRepository;
    private final ImageRepository imageRepository;
    private final AwsS3upload awsS3upload;
    private final ItemLikeRepository itemLikeRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final ItemRepositoryQuery itemRepositoryQuery;
    private final IpService ipService;

    @Override
    @Transactional // 상품 생성
    public ItemResponseDto createItem(ItemRequestDto requestDto, List<MultipartFile> files)
        throws IOException {
        // 선택한 상점에 상품 등록
        Shop shop = shopRepository.findById(requestDto.getShopNo())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_SHOP));

        Item item = requestDto.toEntity(shop);

        itemRepository.save(item);

        if (files != null) {
            for (MultipartFile file : files) {
                String fileUrl = awsS3upload.upload(file, "item " + item.getNo());

                if (imageRepository.existsByImageUrlAndItem_No(fileUrl, item.getNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(item, fileUrl));
            }
        } else {
            // 상품 기본 이미지 추가
            addDefaultImageIfNotExists(item);
        }
        return ItemResponseDto.of(item);
    }

    @Override
    @Transactional(readOnly = true) // 상품 목록 조회
    public Page<ItemResponseDto> getItems(Pageable pageable) {
        Page<Item> itemList = itemRepository.findAll(pageable);
        return itemList.map(ItemResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 상점 내 상품 목록 조회
    public Page<ItemResponseDto> getItemsByShopNo(Long shopNo, Pageable pageable) {
        Page<Item> itemList = itemRepository.findAllByShop_No(shopNo, pageable);
        return itemList.map(ItemResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 시장 내 상품 목록 조회
    public Page<ItemResponseDto> getItemsByMarketNo(Long marketNo, Pageable pageable) {
        Page<Item> itemList = itemRepository.findAllByShop_Market_No(marketNo, pageable);
        return itemList.map(ItemResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 키워드 검색 상품 목록 조회
    public Page<ItemResponseDto> searchItems(ItemSearchCond cond, Pageable pageable) {
        return itemRepositoryQuery.searchItems(cond, pageable).map(ItemResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 상품별 가격 랭킹 조회 (5위까지)
    public List<ItemResponseDto> searchRankingFiveItems(ItemSearchCond cond) {
        return itemRepositoryQuery.searchRankingFiveItems(cond).stream().map(ItemResponseDto::of)
            .toList();
    }

    @Transactional // 상품 단건 조회 // IP 주소당 하루에 조회수 1회 증가
    public ItemResponseDto getItem(Long itemNo, HttpServletRequest request) {
        Item item = findItem(itemNo);

        String ipAddress = ipService.getIpAddress(request);

        if (!ipService.hasTypeBeenViewed(ipAddress, "item", item.getNo())) {
            ipService.markTypeAsViewed(ipAddress, "item", item.getNo());
            item.setViewCount(item.getViewCount() + 1);
        }
        return ItemResponseDto.of(item);
    }

    @Override
    @Transactional(readOnly = true) // 상품 카테고리 목록 조회
    public Page<ItemResponseDto> getCategoryItem(ItemCategoryEnum itemCategory, Pageable pageable) {
        Page<Item> itemsList = itemRepository.findByItemCategoryOrderByItemCategoryDesc(
            itemCategory, pageable);
        return itemsList.map(ItemResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 특정 상점 내 상품 카테고리별 조회
    public Page<ItemResponseDto> getItemsByCategoryAndShopNo(Long shopNo,
        ItemCategoryEnum itemCategory, Pageable pageable) {
        Page<Item> itemsList = itemRepository.findByShopNoAndItemCategory(shopNo, itemCategory,
            pageable);
        return itemsList.map(ItemResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 특정 시장 내 상품 카테고리별 조회
    public List<ItemCategoryResponseDto> getItemsByCategory(Long marketNo,
        ItemCategoryEnum itemCategory) {
        List<Item> items = itemRepository.findByShopMarketNoAndItemCategory(marketNo, itemCategory);
        List<Item> distinctItems = distinctByItemName(items);
        return distinctItems.stream().map(ItemCategoryResponseDto::of).toList();
    }

    private List<Item> distinctByItemName(List<Item> items) {  // 상품명이 같은 경우 중복을 제거하는 메서드
        return new ArrayList<>(items.stream().collect(
            Collectors.toMap(Item::getItemName, item -> item, (existing, replacement) -> existing
                // 이미 있는 상품명이면 기존 것을 유지
            )).values());
    }

    @Override
    @Transactional(readOnly = true) // (페이징) 특정 시장 내 상품 카테고리별 조회
    public Page<ItemResponseDto> getItemsByCategoryPaging(Long marketNo,
        ItemCategoryEnum itemCategory, Pageable pageable) {
        Page<Item> items = itemRepository.findByShopMarketNoAndItemCategory(marketNo, itemCategory,
            pageable);
        return items.map(ItemResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 상품 저렴한 순으로 5개 조회(-> Redis 저장-> Redis 에 존재하면 바로 반환)
    @Cacheable(cacheNames = "getTop5Items", key = "'market:' + #marketNo + ':item:' + #itemName + ':top5'", cacheManager = "ItemTop5CacheManager")
    public List<ItemTop5ResponseDto> getTop5ItemsInMarketByItemName(Long marketNo, String itemName) {
        // 해당 마켓의 상품 조회 및 DTO 변환
        List<ItemTop5ResponseDto> top5Items = itemRepositoryQuery.searchItemsByMarketNoAndItemName(
            marketNo, itemName);
        // 각 상품에 rank 추가
        for (int i = 0; i < top5Items.size(); i++) {
            top5Items.get(i).setRank(i + 1);
        }
        return top5Items;
    }

    @Override
    @Transactional(readOnly = true) // 상품 TOP5 내 특정 상품 정보 조회
    public ItemResponseDto getItemInShopByItemNo(Long shopNo, Long itemNo) {
        Item item = itemRepository.findByShopNoAndNo(shopNo, itemNo);
        return ItemResponseDto.of(item);
    }

    @Override
    @Transactional // 상품 수정
    public ItemResponseDto updateItem(Long itemNo, ItemRequestDto requestDto,
        List<MultipartFile> files) throws IOException {
        Item item = findItem(itemNo);
        item.updateItem(requestDto);

        List<String> imageUrls = requestDto.getImageUrls(); // 클라이언트로부터 받은 이미지 URL
        List<Image> existingImages = imageRepository.findByItem_No(itemNo); // DB 에서 가져온 기존 이미지들

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String fileUrl = awsS3upload.upload(file, "item " + itemNo);

                if (imageRepository.existsByImageUrlAndItem_No(fileUrl, itemNo)) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(item, fileUrl));
            }
            // 새 이미지(files)가 추가되면 기본 이미지를 삭제
            deleteDefaultImageIfExists(itemNo);
        } else if (imageUrls != null) { // 기존 이미지 중 클라이언트에서 제거된 이미지를 삭제
            for (Image existingImage : existingImages) {
                if (!imageUrls.contains(existingImage.getImageUrl())) {
                    deleteImage(existingImage);
                }
            }
        } else { // 파일과 이미지 URL 모두 없으면 기본 이미지 추가
            addDefaultImageIfNotExists(item);
        }
        // 클라이언트에서 모든 이미지 URL 을 제거한 경우 기존 이미지를 전부 삭제
        if (imageUrls == null || imageUrls.isEmpty()) {
            deleteAllImages(existingImages);
        }

        return ItemResponseDto.of(item);
    }

    private void deleteDefaultImageIfExists(Long itemNo) {
        if (imageRepository.existsByImageUrlAndItem_No(ImageConfig.DEFAULT_ITEM_IMAGE_URL, itemNo)) {
            imageRepository.deleteByImageUrlAndItem_No(ImageConfig.DEFAULT_ITEM_IMAGE_URL, itemNo);
        }
    }

    private void deleteAllImages(List<Image> images) {
        for (Image image : images) {
            deleteImage(image);
        }
    }

    private void deleteImage(Image image) {
        if (!image.getImageUrl().equals(ImageConfig.DEFAULT_ITEM_IMAGE_URL)) { // 기본이미지 제외
            awsS3upload.delete(image.getImageUrl()); // S3에서 이미지 삭제
        }
        imageRepository.delete(image); // DB 에서 이미지 삭제
    }

    private void addDefaultImageIfNotExists(Item item) {
        if (!imageRepository.existsByImageUrlAndItem_No(ImageConfig.DEFAULT_ITEM_IMAGE_URL,
            item.getNo())) {
            imageRepository.save(new Image(item, ImageConfig.DEFAULT_ITEM_IMAGE_URL));
        }
    }

    @Override
    @Transactional // 상품 삭제
    public void deleteItem(Long itemNo) {
        Item item = findItem(itemNo);
        List<Image> images = imageRepository.findByItem_No(itemNo);
        // 이미지가 존재하는 경우에만 S3 삭제 작업 수행
        if (images != null && !images.isEmpty()) {
            for (Image image : images) {
                if (!image.getImageUrl().equals(ImageConfig.DEFAULT_ITEM_IMAGE_URL)) {
                    awsS3upload.delete(image.getImageUrl()); // 기본이미지 제외 S3 에서도 이미지 삭제
                }
            }
        }
        itemRepository.delete(item);
    }

    @Override
    @Transactional // 좋아요 생성
    public void createItemLike(Long itemNo, Member member) {
        itemLikeRepository.findByItemNoAndMember(itemNo, member).ifPresent(itemLike -> {
            throw new BusinessException(ErrorCode.EXISTS_ITEM_LIKE);
        });

        Item item = findItem(itemNo);
        itemLikeRepository.save(new ItemLike(item, member));

        /*상점 판매자 번호가 등록되어 있지 않으면 관리자에게 알람*/
        // 상점의 판매자 확인
        Member seller = item.getShop().getSeller();
        if (seller == null) {
            // 판매자가 등록되어 있지 않은 경우, 모든 관리자에게 알림을 전송
            List<Member> adminList = memberRepository.findAllByRole(Role.ADMIN);
            // 관리자 리스트가 비어 있는지 확인
            if (adminList.isEmpty()) {
                throw new BusinessException(ErrorCode.NOT_EXISTS_ADMIN);
            }
            // 관리자에게 알림을 보낼 경우
            NotificationArgs notificationArgs = NotificationArgs.of(member.getMemberNo(),
                item.getShop().getNo());
            // 모든 관리자에게 알림 전송
            for (Member admin : adminList) {
                notificationService.send(NotificationType.NEW_LIKE_ON_ITEM, notificationArgs,
                    admin);
            }
        } else {
            // 판매자에게 알림을 보낼 경우
            NotificationArgs notificationArgs = NotificationArgs.of(member.getMemberNo(),
                item.getShop().getNo());
            notificationService.send(NotificationType.NEW_LIKE_ON_ITEM, notificationArgs, seller);
        }
    }

    @Override
    @Transactional // 좋아요 여부 확인
    public boolean checkItemLike(Long itemNo, Member member) {
        Optional<ItemLike> itemLike = itemLikeRepository.findByItemNoAndMember(itemNo, member);
        return itemLike.isPresent(); // 좋아요 존재하면 true
    }

    @Override
    @Transactional // 좋아요 삭제
    public void deleteItemLike(Long itemNo, Member member) {
        Optional<ItemLike> itemLike = itemLikeRepository.findByItemNoAndMember(itemNo, member);

        if (itemLike.isPresent()) {
            itemLikeRepository.delete(itemLike.get());
        } else {
            throw new BusinessException(ErrorCode.NOT_EXISTS_ITEM_LIKE);
        }
    }

    @Override // 좋아요 수 조회
    @Transactional(readOnly = true)
    public Long countItemLikes(Long itemNo) {
        return itemLikeRepository.countByItemNo(itemNo);
    }

    @Override // 상품 찾기
    @Transactional(readOnly = true)
    public Item findItem(Long itemNo) {
        return itemRepository.findById(itemNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ITEM));
    }
}
