package com.market.domain.item.service;

import com.market.domain.image.config.AwsS3upload;
import com.market.domain.image.entity.Image;
import com.market.domain.image.repository.ImageRepository;
import com.market.domain.item.dto.ItemRequestDto;
import com.market.domain.item.dto.ItemResponseDto;
import com.market.domain.item.entity.Item;
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
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ShopRepository shopRepository;
    private final ImageRepository imageRepository;
    private final AwsS3upload awsS3upload;
    private final ItemLikeRepository itemLikeRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final ItemRepositoryQuery itemRepositoryQuery;

    @Override
    @Transactional // 상품 생성
    public void createItem(ItemRequestDto requestDto, List<MultipartFile> files)
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

                if (imageRepository.existsByImageUrlAndNo(fileUrl, item.getNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(item, fileUrl));
            }
        }
    }

    @Override
    @Transactional(readOnly = true) // 상품 목록 조회
    public Page<ItemResponseDto> getItems(Pageable pageable) {
        Page<Item> itemList = itemRepository.findAll(pageable);
        return itemList.map(ItemResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true) // 키워드 검색 상품 목록 조회
    public Page<ItemResponseDto> searchItems(ItemSearchCond cond, Pageable pageable) {
        return itemRepositoryQuery.searchItems(cond, pageable).map(ItemResponseDto::of);
    }

    @Transactional(readOnly = true) // 상품 단건 조회
    public ItemResponseDto getItem(Long itemNo) {
        Item item = findItem(itemNo);
        return ItemResponseDto.of(item);
    }

    @Override
    @Transactional // 상품 수정
    public void updateItem(Long itemNo, ItemRequestDto requestDto, List<MultipartFile> files)
        throws IOException {
        Item item = findItem(itemNo);

        item.updateItem(requestDto);

        if (files != null) {
            for (MultipartFile file : files) {
                String fileUrl = awsS3upload.upload(file, "item " + item.getNo());

                if (imageRepository.existsByImageUrlAndNo(fileUrl, item.getNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(item, fileUrl));
            }
        }
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
}
