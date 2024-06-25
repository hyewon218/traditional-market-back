package com.market.domain.item.service;

import com.market.domain.item.dto.ItemRequestDto;
import com.market.domain.item.dto.ItemResponseDto;
import com.market.domain.item.entity.Item;
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
     */
    void createItem(ItemRequestDto requestDto, List<MultipartFile> files) throws IOException;

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
     */
    void updateItem(Long itemNo, ItemRequestDto requestDto, List<MultipartFile> files)
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
}
