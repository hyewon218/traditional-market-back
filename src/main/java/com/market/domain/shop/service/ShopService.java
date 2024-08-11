package com.market.domain.shop.service;

import com.market.domain.member.entity.Member;
import com.market.domain.shop.dto.ShopRequestDto;
import com.market.domain.shop.dto.ShopResponseDto;
import com.market.domain.shop.entity.CategoryEnum;
import com.market.domain.shop.entity.Shop;
import java.io.IOException;
import java.util.List;

import com.market.domain.shop.repository.ShopSearchCond;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ShopService {

    /**
     * 상점 생성
     *
     * @param requestDto : 상점 생성 요청정보
     * @param files      : 상점 생성 첨부파일
     * @return ShopResponseDto : 생성된 상점 정보
     */
    ShopResponseDto createShop(ShopRequestDto requestDto, List<MultipartFile> files) throws IOException;

    /**
     * 상점 목록 조회
     *
     * @return : 조회된 상점들 정보
     */
    Page<ShopResponseDto> getShops(Pageable pageable);

    /**
     * 키워드 검색 상점 목록 조회
     *
     * @param cond 조건
     * @return 검색한 키워드가 있는 상점 목록 조회
     */
    Page<ShopResponseDto> searchShops(ShopSearchCond cond, Pageable pageable);

    /**
     * 시장 내 상점 목록 조회
     *
     * @return : 조회된 상점들 정보
     */
    Page<ShopResponseDto> getShopsByMarketNo(Long marketNo, Pageable pageable);

    /**
     * 상점 카테고리별 목록 조회
     *
     * @return : 조회된 상점들 정보
     */
    Page<ShopResponseDto> getCategoryShop(CategoryEnum category, Pageable pageable);

    /**
     * 상품 카테고리에 해당하는 상품 조회
     *
     * @param marketNo : 찾을 시장 no
     * @param category : 찾을 상품 카테고리
     * @return ItemCategoryResponseDto : 특정 카테고리에 해당하는 상품 목록
     */
    Page<ShopResponseDto> getShopsByCategory(Long marketNo, CategoryEnum category, Pageable pageable);

    /**
     * 상점 단건 조회
     *
     * @param shopNo : 조회 할 상점 no
     * @return : 조회된 상점 단건 정보
     */
    ShopResponseDto getShop(Long shopNo, HttpServletRequest request);

    /**
     * 상점명 수정
     *
     * @param shopNo     : 수정할 상점 no
     * @param requestDto : 상점 수정 요청정보
     * @return ShopResponseDto : 수정된 상점 정보
     */
    ShopResponseDto updateShop(Long shopNo, ShopRequestDto requestDto, List<MultipartFile> files)
        throws IOException;

    /**
     * 상점 삭제
     *
     * @param shopNo : 삭제할 상점 no
     */
    void deleteShop(Long shopNo);

    /**
     * 좋아요 생성
     *
     * @param shopNo : 종아요할 상점 no
     * @param member : 좋아요 생성 요청자
     */
    void createShopLike(Long shopNo, Member member);

    /**
     * 좋아요 여부 확인
     *
     * @param shopNo : 종아요 확인 no
     * @param member   : 좋아요 확인 member
     */
    boolean checkShopLike(Long shopNo, Member member);

    /**
     * 좋아요 삭제
     *
     * @param shopNo : 종아요 삭제할 상점 no
     * @param member : 좋아요 삭제 요청자
     */
    void deleteShopLike(Long shopNo, Member member);

    /**
     * 상점 찾기
     *
     * @param shopNo : 찾을 상점 no
     * @return : 상점 Entity
     */
    Shop findShop(Long shopNo);

    /**
     * 총 상점 수
     *
     * @return : 총 상점 수
     */
    Long countShops();

    /**
     * 시장별 상점 수
     *
     * @return : 시장별 상점 수
     */
    Long countShopsByMarket(Long marketNo);
}
