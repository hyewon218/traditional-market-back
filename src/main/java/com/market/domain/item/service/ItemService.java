package com.market.domain.item.service;

import com.market.domain.item.dto.ItemCategoryResponseDto;
import com.market.domain.item.dto.ItemRequestDto;
import com.market.domain.item.dto.ItemResponseDto;
import com.market.domain.item.dto.ItemTop5ResponseDto;
import com.market.domain.item.entity.Item;
import com.market.domain.item.entity.ItemCategoryEnum;
import com.market.domain.item.repository.ItemSearchCond;
import com.market.domain.member.entity.Member;
import java.io.IOException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ItemService {

    /**
     * 상점 생성
     *
     * @param requestDto : 상품 생성 요청정보
     * @param files      : 상품 생성 첨부파일
     * @return ItemResponseDto : 생성된 상품 정보
     */
    ItemResponseDto createItem(ItemRequestDto requestDto, List<MultipartFile> files) throws IOException;

    /**
     * 상품 목록 조회
     *
     * @return : 조회된 상품들 정보
     */
    Page<ItemResponseDto> getItems(Pageable pageable);

    /**
     * 상점 내 상품 목록 조회
     *
     * @return : 조회된 상품들 정보
     */
    Page<ItemResponseDto> getItemsByShopNo(Long shopNo, Pageable pageable);

    /**
     * 키워드 검색 상품 목록 조회
     *
     * @param cond 조건
     * @return 검색한 키워드가 있는 상품 목록 조회
     */
    Page<ItemResponseDto> searchItems(ItemSearchCond cond, Pageable pageable);

    /**
     * 상품별 가격 랭킹 조회
     *
     * @param cond 조건
     * @return 검색한 키워드가 있는 상품별 가격 랭킹 조회
     */
    List<ItemResponseDto> searchRankingFiveItems(ItemSearchCond cond);

    /**
     * 상점 단건 조회
     *
     * @param itemNo : 조회 할 상품 no
     * @return : 조회된 상품 단건 정보
     */
    ItemResponseDto getItem(Long itemNo);

    /**
     * 상품 수정
     *
     * @param itemNo   : 수정할 상품 no
     * @param requestDto : 상품 수정 요청정보
     * @param files      : 상품 수정 첨부파일
     * @return ItemResponseDto : 생성된 상품 정보
     */
    ItemResponseDto updateItem(Long itemNo, ItemRequestDto requestDto, List<MultipartFile> files)
        throws IOException;

    /**
     * 상점 삭제
     *
     * @param itemNo : 삭제할 상품 no
     */
    void deleteItem(Long itemNo);

    /**
     * 좋아요 생성
     *
     * @param itemNo : 종아요할 상품 no
     * @param member : 좋아요 생성 요청자
     */
    void createItemLike(Long itemNo, Member member);

    /**
     * 좋아요 삭제
     *
     * @param itemNo : 종아요 삭제할 상품 no
     * @param member : 좋아요 삭제 요청자
     */
    void deleteItemLike(Long itemNo, Member member);

    /**
     * 상점 찾기
     *
     * @param itemNo : 찾을 상품 no
     * @return : 상품 Entity
     */
    Item findItem(Long itemNo);

    /**
     * 상품 카테고리에 해당하는 상품 조회
     *
     * @param marketNo : 찾을 시장 no
     * @param itemCategory : 찾을 상품 카테고리
     * @return ItemCategoryResponseDto : 특정 카테고리에 해당하는 상품 목록
     */
    List<ItemCategoryResponseDto> getItemsByCategory(Long marketNo, ItemCategoryEnum itemCategory);

    /**
     * 상품 저렴한 순으로 5개 조회
     *
     * @param marketNo : 찾을 시장 no
     * @param itemName : 찾을 상품명
     * @return ItemTop5ResponseDto : 상품 가격 오름차순으로 정렬한 5개 상품의 목록
     */
    List<ItemTop5ResponseDto> getTop5ItemsInMarketByItemName(Long marketNo, String itemName);
}
