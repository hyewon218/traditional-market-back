package com.market.domain.market.controller;

import com.market.domain.market.dto.MarketRequestDto;
import com.market.domain.market.dto.MarketResponseDto;
import com.market.domain.market.service.MarketService;
import com.market.global.response.ApiResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse> createMarket( // 시장 생성
        @ModelAttribute MarketRequestDto requestDto,
        @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files)
        throws IOException {
        marketService.createMarket(requestDto, files);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse("시장 생성 성공!", HttpStatus.CREATED.value()));
    }

    @GetMapping("/markets") // 시장 목록 조회
    public ResponseEntity<Page<MarketResponseDto>> getMarkets(@RequestParam("page") int page,
                                                              @RequestParam("size") int size,
                                                              @RequestParam("sortBy") String sortBy,
                                                              @RequestParam("isAsc") boolean isAsc) {
        Page<MarketResponseDto> result = marketService.getMarkets(page, size, sortBy, isAsc);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/markets/{marketNo}") // 시장 단건 조회
    public ResponseEntity<MarketResponseDto> getMarket(
        @PathVariable Long marketNo) {
        return ResponseEntity.ok(marketService.getMarket(marketNo));
    }

    @PutMapping("/markets/{marketNo}")
    public ResponseEntity<ApiResponse> updateMarket( // 시장 수정
        @PathVariable Long marketNo,
        @ModelAttribute MarketRequestDto requestDto,
        @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files)
        throws IOException {
        marketService.updateMarket(marketNo, requestDto, files);
        return ResponseEntity.ok().body(new ApiResponse("시장 수정 성공!", HttpStatus.OK.value()));
    }

    @DeleteMapping("/markets/{marketNo}")
    public ResponseEntity<ApiResponse> deleteMarket( // 시장 삭제
        @PathVariable Long marketNo) {
        marketService.deleteMarket(marketNo);
        return ResponseEntity.ok().body(new ApiResponse("시장 삭제 완료!", HttpStatus.OK.value()));
    }
}