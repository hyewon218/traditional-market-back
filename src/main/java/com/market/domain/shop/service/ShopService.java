package com.market.domain.shop.service;

import com.market.domain.member.entity.Member;
import com.market.domain.shop.dto.ShopRequestDto;
import com.market.domain.shop.dto.ShopResponseDto;
import com.market.domain.shop.entity.Shop;
import java.io.IOException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ShopService {

    /**
     * 상점 생성
     *
     * @param requestDto : 상점 생성 요청정보
     * @param files      : 상점 생성 첨부파일
     */
    void createShop(ShopRequestDto requestDto, List<MultipartFile> files) throws IOException;

    /**
     * 상점 목록 조회
     *
     * @return : 조회된 상점들 정보
     */
    Page<ShopResponseDto> getShops(Pageable pageable);

    /**
     * 상점 단건 조회
     *
     * @param shopNo : 조회 할 상점 no
     * @return : 조회된 상점 단건 정보
     */
    ShopResponseDto getShop(Long shopNo);

    /**
     * 상점명 수정
     *
     * @param shopNo     : 수정할 상점 no
     * @param requestDto : 상점 수정 요청정보
     */
    void updateShop(Long shopNo, ShopRequestDto requestDto, List<MultipartFile> files)
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
}
