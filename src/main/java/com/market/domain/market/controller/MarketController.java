package com.market.domain.market.controller;

import com.market.domain.market.dto.MarketRequestDto;
import com.market.domain.market.dto.MarketResponseDto;
import com.market.domain.market.entity.CategoryEnum;
import com.market.domain.market.repository.MarketSearchCond;
import com.market.domain.market.service.MarketService;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class MarketController {

    private final MarketService marketService;

    @PostMapping("/markets")
    public ResponseEntity<MarketResponseDto> createMarket( // 시장 생성
        @ModelAttribute MarketRequestDto requestDto,
        @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files)
        throws IOException {
        MarketResponseDto result = marketService.createMarket(requestDto, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/markets") // 시장 목록 조회
    public ResponseEntity<Page<MarketResponseDto>> getMarkets(Pageable pageable) {
        Page<MarketResponseDto> result = marketService.getMarkets(pageable);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/markets/search") // 키워드 검색 시장 목록 조회
    public ResponseEntity<Page<MarketResponseDto>> searchMarkets(MarketSearchCond cond,
        Pageable pageable) {
        Page<MarketResponseDto> result = marketService.searchMarkets(cond, pageable);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/markets/category") // 시장 카테고리별 조회
    public ResponseEntity<Page<MarketResponseDto>> getCategoryMarkets(
        @RequestParam("category") CategoryEnum category,
        Pageable pageable) {
        Page<MarketResponseDto> result = marketService.getCategoryMarkets(category, pageable);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/markets/{marketNo}") // 시장 단건 조회
    public ResponseEntity<MarketResponseDto> getMarket(
        @PathVariable("marketNo") Long marketNo, HttpServletRequest request) {
        return ResponseEntity.ok(marketService.getMarket(marketNo, request));
    }

    @PutMapping("/markets/{marketNo}")
    public ResponseEntity<MarketResponseDto> updateMarket( // 시장 수정
        @PathVariable("marketNo") Long marketNo,
        @ModelAttribute MarketRequestDto requestDto,
        @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files)
        throws IOException {
        MarketResponseDto result = marketService.updateMarket(marketNo, requestDto, files);
        return ResponseEntity.ok().body(result);
    }

    @DeleteMapping("/markets/{marketNo}")
    public ResponseEntity<ApiResponse> deleteMarket( // 시장 삭제
        @PathVariable("marketNo") Long marketNo) {
        marketService.deleteMarket(marketNo);
        return ResponseEntity.ok().body(new ApiResponse("시장 삭제 완료!", HttpStatus.OK.value()));
    }

    @PostMapping("/markets/{marketNo}/likes")
    public ResponseEntity<ApiResponse> createMarketLike( // 좋아요 생성
        @PathVariable Long marketNo, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        marketService.createMarketLike(marketNo, userDetails.getMember());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse("해당 시장에 좋아요를 눌렀습니다", HttpStatus.CREATED.value()));
    }

    @GetMapping("/markets/{marketNo}/likes")
    public ResponseEntity<Boolean> getMarketLike( // 좋아요 여부 확인
        @PathVariable Long marketNo, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        boolean hasLiked = marketService.checkMarketLike(marketNo, userDetails.getMember());
        return ResponseEntity.ok(hasLiked);
    }

    @DeleteMapping("/markets/{marketNo}/likes")
    public ResponseEntity<ApiResponse> deleteMarketLike( // 좋아요 삭제
        @PathVariable Long marketNo, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        marketService.deleteMarketLike(marketNo, userDetails.getMember());
        return ResponseEntity.ok()
            .body(new ApiResponse("해당 시장에 좋아요를 취소하였습니다", HttpStatus.OK.value()));
    }

    @GetMapping("/markets/{marketNo}/likes-count")
    public ResponseEntity<Long> getMarketLike(@PathVariable Long marketNo) { // 좋아요 수 조회
        return ResponseEntity.ok(marketService.countMarketLikes(marketNo));
    }

    @GetMapping("/admin/markets/count") // 총 시장 수, 관리자만 가능
    public ResponseEntity<Long> getCountMarket() {
        return ResponseEntity.ok().body(marketService.getCountMarket());
    }

    @GetMapping("/admin/markets/{marketNo}/sales") // 시장별 총매출액, 관리자만 가능
    public ResponseEntity<Long> getTotalSalesPrice(@PathVariable Long marketNo) {
        return ResponseEntity.ok().body(marketService.getTotalSalesPrice(marketNo));
    }

    @GetMapping("/admin/markets/sum") // 모든 시장의 총매출액 합계, 관리자만 가능
    public ResponseEntity<Long> getMarketSalesSum() {
        return ResponseEntity.ok().body(marketService.getMarketSalesSum());
    }
}