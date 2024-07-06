package com.market.domain.item.service;

import com.market.domain.image.config.AwsS3upload;
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
import com.market.domain.market.entity.Market;
import com.market.domain.market.repository.MarketRepository;
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
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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
    private final MarketRepository marketRepository;
    private final RedisTemplate<String, List<ItemTop5ResponseDto>> redisTemplate;

    @Override
    @Transactional // 상품 생성
    public ItemResponseDto createItem(ItemRequestDto requestDto, List<MultipartFile> files)
        throws IOException {
        // 선택한 상점에 상품 등록
        Shop shop = shopRepository.findById(requestDto.getShopNo()).orElseThrow(
            () -> new BusinessException(ErrorCode.NOT_FOUND_SHOP)
        );

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

    @Transactional(readOnly = true) // 상품 단건 조회
    public ItemResponseDto getItem(Long itemNo) {
        Item item = findItem(itemNo);
        return ItemResponseDto.of(item);
    }

    @Override
    @Transactional // 상품 수정
    public ItemResponseDto updateItem(Long itemNo, ItemRequestDto requestDto, List<MultipartFile> files)
        throws IOException {
        Item item = findItem(itemNo);

        item.updateItem(requestDto);

        List<String> imageUrls = requestDto.getImageUrls(); // 클라이언트
        List<Image> existingImages = imageRepository.findByItem_No(itemNo); // DB

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
        }

        if (files != null) {
            for (MultipartFile file : files) {
                String fileUrl = awsS3upload.upload(file, "item " + item.getNo());

                if (imageRepository.existsByImageUrlAndItem_No(fileUrl, item.getNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(item, fileUrl));
            }
        }
        return ItemResponseDto.of(item);
    }

    @Override
    @Transactional // 시장 삭제
    public void deleteItem(Long itemNo) {
        Item item = findItem(itemNo);
        itemRepository.delete(item);
    }

    @Override
    @Transactional
    public void createItemLike(Long itemNo, Member member) { // 좋아요 생성
        Item item = findItem(itemNo);

        itemLikeRepository.findByItemAndMember(item, member).ifPresent(itemLike -> {
            throw new BusinessException(ErrorCode.EXISTS_ITEM_LIKE);
        });
        itemLikeRepository.save(new ItemLike(item, member));

        // create alarm
        Member receiver;
        if (item.getShop().getSeller() == null) { // 사장님이 등록되어 있지 않으면 관리자에게 알람이 가도록
            receiver = memberRepository.findByRole(Role.ADMIN).orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_EXISTS_ADMIN)
            );
        } else {
            receiver = item.getShop().getSeller();
        }
        notificationService.send(
            NotificationType.NEW_LIKE_ON_SHOP,
            new NotificationArgs(member.getMemberNo(), item.getShop().getNo()), receiver);
    }

    @Override
    @Transactional
    public void deleteItemLike(Long itemNo, Member member) { // 좋아요 삭제
        Item item = findItem(itemNo);
        Optional<ItemLike> itemLike = itemLikeRepository.findByItemAndMember(item, member);

        if (itemLike.isPresent()) {
            itemLikeRepository.delete(itemLike.get());
        } else {
            throw new BusinessException(ErrorCode.NOT_EXISTS_ITEM_LIKE);
        }
    }

    @Override // 시장 찾기
    public Item findItem(Long itemNo) {
        return itemRepository.findById(itemNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ITEM));
    }

    // 상품 카테고리별 조회
    @Override
    @Transactional(readOnly = true)
    public List<ItemCategoryResponseDto> getItemsByCategory(Long marketNo, ItemCategoryEnum itemCategory) {
        // 시장 고유번호와 상품 카테고리로 해당하는 상품 조회
        List<Item> items = itemRepository.findByShopMarketNoAndItemCategory(marketNo, itemCategory);

        // 아이템을 찾지 못한 경우 예외 처리
        if (items.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ITEM);
        }

        // 상품명이 같은 경우 중복을 제거하기 위해 distinctByItemName 메서드 사용
        List<Item> distinctItems = distinctByItemName(items);

        return distinctItems.stream()
                .map(ItemCategoryResponseDto::of)
                .collect(Collectors.toList());
    }

    // 상품명이 같은 경우 중복을 제거하는 메서드
    private List<Item> distinctByItemName(List<Item> items) {
        return items.stream()
                .collect(Collectors.toMap(
                        Item::getItemName,
                        item -> item,
                        (existing, replacement) -> existing // 이미 있는 상품명이면 기존 것을 유지
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
    }

    // 상품 저렴한 순으로 5개 조회(redis로 저장하고 반환)
    @Override
    @Transactional(readOnly = true)
    public List<ItemTop5ResponseDto> getTop5ItemsInMarketByItemName(Long marketNo, String itemName) {

        List<ItemTop5ResponseDto> top5Items = itemRepositoryQuery.searchItemsByShopNoAndItemName(marketNo, itemName)
                .stream().map(ItemTop5ResponseDto::of).toList();

        // 각 상품에 rank 추가
        for (int i = 0; i < top5Items.size(); i++) {
            top5Items.get(i).setRank(i + 1);
        }

        // redis에 저장할때 필요한 정보
        Market market = marketRepository.findById(marketNo).orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_FOUND_MARKET));
        String marketName = market.getMarketName();

        // redis에 저장 후 반환
        return saveRedisAndReturn(marketName, itemName, top5Items);
    }

    // redis 저장 후 반환하는 메서드
    private List<ItemTop5ResponseDto> saveRedisAndReturn(String marketName, String itemName, List<ItemTop5ResponseDto> top5ItemsResponse) {

        // 시장 고유번호와 상품 이름으로 상품을 찾아서 만약 해당하는 상품이 없다면 오류 출력하고 redis에 저장하지않음.
        String findItemName = "";
        Market market = marketRepository.findByMarketName(marketName);
        List<Item> findItems = itemRepository.findByShopMarketNoAndItemName(market.getNo(), itemName);
        if(findItems.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ITEM);
        } else {
            for(Item item : findItems) {
                findItemName = item.getItemName();
            }
        }

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = marketName + "_" + findItemName + "_" + today;

        // redis에 데이터를 저장할 때 사용할 객체
        ValueOperations<String, List<ItemTop5ResponseDto>> valueOps = redisTemplate.opsForValue();
        valueOps.set(key, top5ItemsResponse);

        // 현재 시간에서 자정까지의 시간 간격을 계산하여 만료 시간을 설정
        LocalDateTime midnight = LocalDateTime.now().plusDays(1).with(LocalTime.MIDNIGHT);
        long secondsUntilExpiration = Duration.between(LocalDateTime.now(), midnight).getSeconds();
        redisTemplate.expire(key, secondsUntilExpiration, TimeUnit.SECONDS);

        return valueOps.get(key);
    }
}
