package com.market.domain.market.marketComment.controller;

import com.market.domain.market.marketComment.dto.MarketCommentRequestDto;
import com.market.domain.market.marketComment.service.MarketCommentService;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/markets")
@RequiredArgsConstructor
public class MarketCommentController {

    private final MarketCommentService marketCommentService;

    @PostMapping("/comments")
    public ResponseEntity<ApiResponse> createMarketComment(
        @RequestBody MarketCommentRequestDto marketCommentRequestDto,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {

        marketCommentService.createMarketComment(marketCommentRequestDto, userDetails.getMember());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse("댓글 생성 완료!", HttpStatus.CREATED.value()));
    }

    @PutMapping("/comments/{commentNo}")
    public ResponseEntity<ApiResponse> updateMarketComment(
        @PathVariable Long commentNo,
        @RequestBody MarketCommentRequestDto marketCommentRequestDto,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        marketCommentService.updateMarketComment(commentNo, marketCommentRequestDto,
            userDetails.getMember());
        return ResponseEntity.ok().body(new ApiResponse("댓글 수정 완료!", HttpStatus.OK.value()));
    }

    @DeleteMapping("/comments/{commentNo}")
    public ResponseEntity<ApiResponse> deleteMarketComment(
        @PathVariable Long commentNo,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        marketCommentService.deleteMarketComment(commentNo, userDetails.getMember());
        return ResponseEntity.ok().body(new ApiResponse("댓글 삭제 완료!", HttpStatus.OK.value()));
    }
}