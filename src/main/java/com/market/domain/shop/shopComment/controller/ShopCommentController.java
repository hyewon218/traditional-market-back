package com.market.domain.shop.shopComment.controller;

import com.market.domain.shop.shopComment.dto.ShopCommentRequestDto;
import com.market.domain.shop.shopComment.dto.ShopCommentResponseDto;
import com.market.domain.shop.shopComment.service.ShopCommentService;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopCommentController {

    private final ShopCommentService shopCommentService;

    @PostMapping("/comments")
    public ResponseEntity<ApiResponse> createShopComment(
        @RequestBody ShopCommentRequestDto shopCommentRequestDto,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        shopCommentService.createShopComment(shopCommentRequestDto, userDetails.getMember());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse("댓글 생성 완료!", HttpStatus.CREATED.value()));
    }

    @GetMapping("/{shopNo}/comments")
    public ResponseEntity<Page<ShopCommentResponseDto>> getShopComment(
        @PathVariable Long shopNo, Pageable pageable) {
        Page<ShopCommentResponseDto> result = shopCommentService.getShopComments(shopNo, pageable);
        return ResponseEntity.ok().body(result);
    }

    @PutMapping("/comments/{commentNo}")
    public ResponseEntity<ApiResponse> updateShopComment(
        @PathVariable Long commentNo,
        @RequestBody ShopCommentRequestDto shopCommentRequestDto,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        shopCommentService.updateShopComment(commentNo, shopCommentRequestDto, userDetails.getMember());
        return ResponseEntity.ok().body(new ApiResponse("댓글 수정 완료!", HttpStatus.OK.value()));
    }

    @DeleteMapping("/comments/{commentNo}")
    public ResponseEntity<ApiResponse> deleteShopComment(
        @PathVariable Long commentNo,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        shopCommentService.deleteShopComment(commentNo, userDetails.getMember());
        return ResponseEntity.ok().body(new ApiResponse("댓글 삭제 완료!", HttpStatus.OK.value()));
    }
}
