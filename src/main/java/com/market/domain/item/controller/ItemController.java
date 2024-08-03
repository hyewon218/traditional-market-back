package com.market.domain.item.controller;

import com.market.domain.item.dto.ItemCategoryResponseDto;
import com.market.domain.item.dto.ItemRequestDto;
import com.market.domain.item.dto.ItemResponseDto;
import com.market.domain.item.dto.ItemTop5ResponseDto;
import com.market.domain.item.entity.ItemCategoryEnum;
import com.market.domain.item.repository.ItemSearchCond;
import com.market.domain.item.service.ItemService;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ItemController {

    private final ItemService itemService;

    @PostMapping("/items")
    public ResponseEntity<ItemResponseDto> createItem( // 상품 생성
        @ModelAttribute ItemRequestDto requestDto,
        @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files)
        throws IOException {
        ItemResponseDto result = itemService.createItem(requestDto, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/items") // 상품 목록 조회
    public ResponseEntity<Page<ItemResponseDto>> getItemsByShopNo(Pageable pageable) {
        Page<ItemResponseDto> result = itemService.getItems(pageable);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/{shopNo}/items") // 상점 내 상품 목록 조회
    public ResponseEntity<Page<ItemResponseDto>> getItems(@PathVariable("shopNo") Long shopNo,
        Pageable pageable) {
        Page<ItemResponseDto> result = itemService.getItemsByShopNo(shopNo, pageable);
        return ResponseEntity.ok().body(result);
    }


    @GetMapping("/items/search") // 키워드 검색 상품 목록 조회
    public ResponseEntity<Page<ItemResponseDto>> searchItems(ItemSearchCond cond,
        Pageable pageable) {
        Page<ItemResponseDto> result = itemService.searchItems(cond, pageable);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/market/{marketNo}/items") // 시장 내 상품 목록 조회
    public ResponseEntity<Page<ItemResponseDto>> getItemsByMarketNo(
        @PathVariable("marketNo") Long marketNo,
        Pageable pageable) {
        Page<ItemResponseDto> result = itemService.getItemsByMarketNo(marketNo, pageable);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/items/ranking") // 상품 가격 랭킹 조회
    public ResponseEntity<List<ItemResponseDto>> searchPriceRankFiveItems(ItemSearchCond cond) {
        List<ItemResponseDto> result = itemService.searchRankingFiveItems(cond);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/items/{itemNo}") // 상품 단건 조회
    public ResponseEntity<ItemResponseDto> getItem(
        @PathVariable Long itemNo, HttpServletRequest request) {
        return ResponseEntity.ok(itemService.getItem(itemNo, request));
    }

    @GetMapping("/items/category") // 상품 카테고리별 조회
    public ResponseEntity<Page<ItemResponseDto>> getCategoryShop(ItemCategoryEnum itemCategory,
        Pageable pageable) {
        Page<ItemResponseDto> result = itemService.getCategoryItem(itemCategory, pageable);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/{marketNo}/items/category") // 시장 번호와 상품 카테고리로 해당하는 상품 목록 조회
    public ResponseEntity<List<ItemCategoryResponseDto>> getItemsByCategory(
        @PathVariable Long marketNo, ItemCategoryEnum itemCategory) {
        List<ItemCategoryResponseDto> categories = itemService.getItemsByCategory(marketNo,
            itemCategory);
        return ResponseEntity.ok().body(categories);
    }

    @GetMapping("/{marketNo}/items/category/paging") // (페이징)시장 번호와 상품 카테고리로 해당하는 상품 목록 조회
    public ResponseEntity<Page<ItemResponseDto>> getItemsByCategory(
        @PathVariable Long marketNo, ItemCategoryEnum itemCategory, Pageable pageable) {
        Page<ItemResponseDto> categories = itemService.getItemsByCategoryPaging(marketNo,
            itemCategory, pageable);
        return ResponseEntity.ok().body(categories);
    }

    @GetMapping("/{marketNo}/items/rank") // 상품 저렴한 순으로 5개 조회
    public ResponseEntity<?> getTop5ItemsInMarketByCategory(
        @PathVariable Long marketNo, String itemName) {
        List<ItemTop5ResponseDto> top5Items = itemService.getTop5ItemsInMarketByItemName(marketNo,
            itemName);
        return ResponseEntity.ok().body(top5Items);
    }

    @PutMapping("/items/{itemNo}")
    public ResponseEntity<ItemResponseDto> updateItem( // 상품 수정
        @PathVariable Long itemNo,
        @ModelAttribute ItemRequestDto requestDto,
        @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files)
        throws IOException {
        ItemResponseDto result = itemService.updateItem(itemNo, requestDto, files);
        return ResponseEntity.ok().body(result);
    }

    @DeleteMapping("/items/{itemNo}")
    public ResponseEntity<ApiResponse> deleteItem( // 상품 삭제
        @PathVariable Long itemNo) {
        itemService.deleteItem(itemNo);
        return ResponseEntity.ok().body(new ApiResponse("상품 삭제 완료!", HttpStatus.OK.value()));
    }

    @PostMapping("/items/{itemNo}/likes")
    public ResponseEntity<ApiResponse> createPostLike( // 좋아요 생성
        @PathVariable Long itemNo, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        itemService.createItemLike(itemNo, userDetails.getMember());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse("해당 상품에 좋아요를 눌렀습니다", HttpStatus.CREATED.value()));
    }

    @GetMapping("/items/{itemNo}/likes")
    public ResponseEntity<Integer> getItemLike( // 좋아요 갯수 조회
        @PathVariable Long itemNo, HttpServletRequest request) {
        return ResponseEntity.ok(itemService.getItem(itemNo, request).getLikes());
    }

    @DeleteMapping("/items/{itemNo}/likes")
    public ResponseEntity<ApiResponse> deletePostLike( // 좋아요 삭제
        @PathVariable Long itemNo, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        itemService.deleteItemLike(itemNo, userDetails.getMember());
        return ResponseEntity.ok()
            .body(new ApiResponse("해당 상품에 좋아요를 취소하였습니다", HttpStatus.OK.value()));
    }
}