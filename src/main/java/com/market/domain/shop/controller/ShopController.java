package com.market.domain.shop.controller;

import com.market.domain.shop.dto.ShopRequestDto;
import com.market.domain.shop.dto.ShopResponseDto;
import com.market.domain.shop.entity.CategoryEnum;
import com.market.domain.shop.repository.ShopSearchCond;
import com.market.domain.shop.service.ShopService;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ShopController {

    private final ShopService shopService;

    @PostMapping("/shops") // 상점 생성
    public ResponseEntity<ShopResponseDto> createShop(
        @ModelAttribute ShopRequestDto requestDto,
        @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files)
        throws IOException {
        ShopResponseDto result = shopService.createShop(requestDto, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/shops") // 상점 목록 조회
    public ResponseEntity<Page<ShopResponseDto>> getShops(Pageable pageable) {
        Page<ShopResponseDto> result = shopService.getShops(pageable);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/shops/search") // 키워드 검색 상점 목록 조회
    public ResponseEntity<Page<ShopResponseDto>> searchShops(ShopSearchCond cond,
        @PageableDefault(size = 20, sort = "shopName", direction = Sort.Direction.ASC)
        Pageable pageable) {
        Page<ShopResponseDto> result = shopService.searchShops(cond, pageable);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/{marketNo}/shops") // 시장 내 상점 목록 조회
    public ResponseEntity<Page<ShopResponseDto>> getShopsByMarketNo(
        @PathVariable("marketNo") Long marketNo,
        Pageable pageable) {
        Page<ShopResponseDto> result = shopService.getShopsByMarketNo(marketNo, pageable);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/shops/category") // 상점 카테고리별 조회
    public ResponseEntity<Page<ShopResponseDto>> getCategoryShop(
        @RequestParam("category") CategoryEnum category,
        Pageable pageable) {
        Page<ShopResponseDto> result = shopService.getCategoryShop(category, pageable);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/{marketNo}/shops/category") // 시장 번호와 상점 카테고리로 해당하는 상점 목록 조회
    public ResponseEntity<Page<ShopResponseDto>> getShopsByCategory(
        @PathVariable Long marketNo, CategoryEnum category, Pageable pageable) {
        Page<ShopResponseDto> categories = shopService.getShopsByCategory(marketNo,
            category, pageable);
        return ResponseEntity.ok().body(categories);
    }

    @GetMapping("/shops/{shopNo}") // 상점 단건 조회
    public ResponseEntity<ShopResponseDto> getShop(
        @PathVariable Long shopNo, HttpServletRequest request) {
        return ResponseEntity.ok(shopService.getShop(shopNo, request));
    }

    @PutMapping("/shops/{shopNo}") // 상점 수정
    public ResponseEntity<ShopResponseDto> updateShopName(
        @PathVariable Long shopNo,
        @ModelAttribute ShopRequestDto requestDto,
        @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files)
        throws IOException {
        ShopResponseDto result = shopService.updateShop(shopNo, requestDto, files);
        return ResponseEntity.ok().body(result);
    }

    @DeleteMapping("/shops/{shopNo}") // 상점 삭제
    public ResponseEntity<ApiResponse> deleteShop(
        @PathVariable Long shopNo) {
        shopService.deleteShop(shopNo);
        return ResponseEntity.ok().body(new ApiResponse("상점 삭제 완료!", HttpStatus.OK.value()));
    }

    @PostMapping("/shops/{shopNo}/likes") // 좋아요 생성
    public ResponseEntity<ApiResponse> createPostLike(
        @PathVariable Long shopNo, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        shopService.createShopLike(shopNo, userDetails.getMember());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse("해당 상점에 좋아요를 눌렀습니다", HttpStatus.CREATED.value()));
    }

    @GetMapping("/shops/{shopNo}/likes") // 좋아요 여부 확인
    public ResponseEntity<Boolean> getShopLike(
        @PathVariable Long shopNo, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        boolean hasLiked = shopService.checkShopLike(shopNo, userDetails.getMember());
        return ResponseEntity.ok(hasLiked);
    }

    @DeleteMapping("/shops/{shopNo}/likes") // 좋아요 삭제
    public ResponseEntity<ApiResponse> deletePostLike(
        @PathVariable Long shopNo, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        shopService.deleteShopLike(shopNo, userDetails.getMember());
        return ResponseEntity.ok()
            .body(new ApiResponse("해당 삼점에 좋아요를 취소하였습니다", HttpStatus.OK.value()));
    }

    @GetMapping("/shops/{shopNo}/likes-count") // 좋아요 수 조회
    public ResponseEntity<Long> getShopLikesCount(HttpServletRequest request,
        @PathVariable Long shopNo) {
        return ResponseEntity.ok(shopService.countShopLikes(request, shopNo));
    }

    @GetMapping("/admin/shops/count")
    public ResponseEntity<Long> countShops() { // 총 상점 수
        return ResponseEntity.ok().body(shopService.countShops());
    }

    @GetMapping("/admin/{marketNo}/shops/count") // 시장별 상점 수
    public ResponseEntity<Long> countShopsByMarket(@PathVariable Long marketNo) {
        return ResponseEntity.ok().body(shopService.countShopsByMarket(marketNo));
    }

    // /api/shops 로 시작하는 get 요청은 토큰 검증 안하고 통과되기 때문에 userDetails 생성 X(userDetails 객체 null)
    // 위와 같은 이유로 url 경로를 기존 /api/shops~ 에서 아래와 같이 수정
    @GetMapping("/seller/shops/mine") // 판매자가 소유한 상점 목록 조회 (판매자 본인이 본인의 상점 목록 조회)
    public ResponseEntity<Page<ShopResponseDto>> getShopsBySeller(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        Pageable pageable) {
        return ResponseEntity.ok()
            .body(shopService.getShopsBySellerNo(userDetails.getMember(), pageable));
    }

    @GetMapping("/admin/shops/{sellerNo}") // 판매자가 소유한 상점 목록 조회 (관리자가 특정 판매자의 상점 목록 조회)
    public ResponseEntity<Page<ShopResponseDto>> getShopsBySellerAdmin(
        @AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long sellerNo,
        Pageable pageable) {
        return ResponseEntity.ok()
            .body(shopService.getShopsBySellerNoAdmin(userDetails.getMember(), sellerNo, pageable));
    }
}