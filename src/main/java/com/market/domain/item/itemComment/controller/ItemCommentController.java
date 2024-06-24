package com.market.domain.item.itemComment.controller;

import com.market.domain.item.itemComment.dto.ItemCommentRequestDto;
import com.market.domain.item.itemComment.dto.ItemCommentResponseDto;
import com.market.domain.item.itemComment.service.ItemCommentService;
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
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemCommentController {

    private final ItemCommentService itemCommentService;

    @PostMapping("/comments")
    public ResponseEntity<ApiResponse> createItemComment(
        @RequestBody ItemCommentRequestDto itemCommentRequestDto,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {

        itemCommentService.createItemComment(itemCommentRequestDto, userDetails.getMember());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse("댓글 생성 완료!", HttpStatus.CREATED.value()));
    }

    @GetMapping("/{itemNo}/comments")
    public ResponseEntity<Page<ItemCommentResponseDto>> getItemComment(
        @PathVariable("itemNo") Long itemNo, Pageable pageable) {
        Page<ItemCommentResponseDto> result = itemCommentService.getItemComments(itemNo, pageable);
        return ResponseEntity.ok().body(result);
    }

    @PutMapping("/comments/{commentNo}")
    public ResponseEntity<ApiResponse> updateItemComment(
        @PathVariable Long commentNo,
        @RequestBody ItemCommentRequestDto itemCommentRequestDto,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        itemCommentService.updateItemComment(commentNo, itemCommentRequestDto, userDetails.getMember());
        return ResponseEntity.ok().body(new ApiResponse("댓글 수정 완료!", HttpStatus.OK.value()));
    }

    @DeleteMapping("/comments/{commentNo}")
    public ResponseEntity<ApiResponse> deleteItemComment(
        @PathVariable Long commentNo,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        itemCommentService.deleteItemComment(commentNo, userDetails.getMember());
        return ResponseEntity.ok().body(new ApiResponse("댓글 삭제 완료!", HttpStatus.OK.value()));
    }
}
