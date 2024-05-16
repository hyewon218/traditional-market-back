package com.market.domain.shop.controller;

import com.market.domain.shop.dto.ShopRequestDto;
import com.market.domain.shop.dto.ShopResponseDto;
import com.market.domain.shop.service.ShopService;
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
public class ShopController {

    private final ShopService shopService;

    @PostMapping("/shops")
    public ResponseEntity<ApiResponse> createShop( // 상점 생성
        @ModelAttribute ShopRequestDto requestDto,
        @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files)
        throws IOException {
        shopService.createShop(requestDto, files);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse("상점 생성 성공!", HttpStatus.CREATED.value()));
    }

    @GetMapping("/shops") // 상점 목록 조회
    public ResponseEntity<Page<ShopResponseDto>> getShops(@RequestParam("page") int page,
        @RequestParam("size") int size,
        @RequestParam("sortBy") String sortBy,
        @RequestParam("isAsc") boolean isAsc) {
        Page<ShopResponseDto> result = shopService.getShops(page, size, sortBy, isAsc);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/shops/{shopNo}") // 상점 단건 조회
    public ResponseEntity<ShopResponseDto> getShop(
        @PathVariable Long shopNo) {
        return ResponseEntity.ok(shopService.getShop(shopNo));
    }

    @PutMapping("/shops/{shopNo}")
    public ResponseEntity<ApiResponse> updateShopName( // 상점 수정
        @PathVariable Long shopNo,
        @ModelAttribute ShopRequestDto requestDto,
        @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files)
        throws IOException {
        shopService.updateShop(shopNo, requestDto, files);
        return ResponseEntity.ok().body(new ApiResponse("상점명 수정 성공!", HttpStatus.OK.value()));
    }

    @DeleteMapping("/shops/{shopNo}")
    public ResponseEntity<ApiResponse> deleteShop( // 상점 삭제
        @PathVariable Long shopNo) {
        shopService.deleteShop(shopNo);
        return ResponseEntity.ok().body(new ApiResponse("상점 삭제 완료!", HttpStatus.OK.value()));
    }
}