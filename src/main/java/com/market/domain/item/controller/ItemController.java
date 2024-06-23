package com.market.domain.item.controller;

import com.market.domain.item.dto.ItemRequestDto;
import com.market.domain.item.dto.ItemResponseDto;
import com.market.domain.item.repository.ItemSearchCond;
import com.market.domain.item.service.ItemService;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ItemController {

    private final ItemService itemService;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse> createItem( // 상품 생성
        @ModelAttribute ItemRequestDto requestDto,
        @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files)
        throws IOException {
        itemService.createItem(requestDto, files);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse("상품 생성 성공!", HttpStatus.CREATED.value()));
    }

    @GetMapping("/items") // 상품 목록 조회
    public ResponseEntity<Page<ItemResponseDto>> getItems(Pageable pageable) {
        Page<ItemResponseDto> result = itemService.getItems(pageable);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/items/search") // 키워드 검색 상품 목록 조회
    public ResponseEntity<Page<ItemResponseDto>> searchItems(ItemSearchCond cond, Pageable pageable){
        Page<ItemResponseDto> result = itemService.searchItems(cond, pageable);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/items/ranking") // 상품 가격 랭킹 조회
    public ResponseEntity<List<ItemResponseDto>> searchPriceRankFiveItems(ItemSearchCond cond){
        List<ItemResponseDto> result = itemService.searchRankingFiveItems(cond);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/items/{itemNo}") // 상품 단건 조회
    public ResponseEntity<ItemResponseDto> getItem(
        @PathVariable Long itemNo) {
        return ResponseEntity.ok(itemService.getItem(itemNo));
    }

    @PutMapping("/items/{itemNo}")
    public ResponseEntity<ApiResponse> updateItem( // 상품 수정
        @PathVariable Long itemNo,
        @ModelAttribute ItemRequestDto requestDto,
        @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files)
        throws IOException {
        itemService.updateItem(itemNo, requestDto, files);
        return ResponseEntity.ok().body(new ApiResponse("상품 수정 성공!", HttpStatus.OK.value()));
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
        @PathVariable Long itemNo) {
        return ResponseEntity.ok(itemService.getItem(itemNo).getLikes());
    }

    @DeleteMapping("/items/{itemNo}/likes")
    public ResponseEntity<ApiResponse> deletePostLike( // 좋아요 삭제
        @PathVariable Long itemNo, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        itemService.deleteItemLike(itemNo, userDetails.getMember());
        return ResponseEntity.ok()
            .body(new ApiResponse("해당 상품에 좋아요를 취소하였습니다", HttpStatus.OK.value()));
    }
}