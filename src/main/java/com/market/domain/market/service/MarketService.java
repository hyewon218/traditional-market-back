package com.market.domain.market.service;

import com.market.domain.market.dto.MarketRequestDto;
import com.market.domain.market.dto.MarketResponseDto;
import com.market.domain.market.entity.CategoryEnum;
import com.market.domain.market.entity.Market;
import com.market.domain.market.repository.MarketSearchCond;
import com.market.domain.member.entity.Member;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface MarketService {

    /**
     * 시장 생성
     *
     * @param requestDto : 시장 생성 요청정보
     * @param files      : 시장 생성 첨부파일
     * @return MarketResponseDto : 생성된 시장 정보
     */
    MarketResponseDto createMarket(MarketRequestDto requestDto, List<MultipartFile> files)
        throws IOException;

    /**
     * 시장 목록 조회
     *
     * @return : 조회된 시장들 정보
     */
    Page<MarketResponseDto> getMarkets(Pageable pageable);

    /**
     * 키워드 검색 시장 목록 조회
     *
     * @param cond 조건
     * @return 검색한 키워드가 있는 시장 목록 조회
     */
    Page<MarketResponseDto> searchMarkets(MarketSearchCond cond, Pageable pageable);

    /**
     * 시장 카테고리별 목록 조회
     *
     * @return : 조회된 시장들 정보
     */
    Page<MarketResponseDto> getCategoryMarkets(CategoryEnum category, Pageable pageable);

    /**
     * 시장 단건 조회
     *
     * @param marketNo : 조회 할 시장 no
     * @return : 조회된 시장 단건 정보
     */
    MarketResponseDto getMarket(Long marketNo, HttpServletRequest request);

    /**
     * 시장 수정
     *
     * @param marketNo   : 수정할 시장 no
     * @param requestDto : 시장 수정 요청정보
     * @param files      : 시장 수정 첨부파일
     * @return MarketResponseDto : 수정된 시장 정보
     */
    MarketResponseDto updateMarket(Long marketNo, MarketRequestDto requestDto,
        List<MultipartFile> files)
        throws IOException;

    /**
     * 시장 삭제
     *
     * @param marketNo : 삭제할 시장 no
     */
    void deleteMarket(Long marketNo);

    /**
     * 좋아요 생성
     *
     * @param marketNo : 종아요할 시장 no
     * @param member   : 좋아요 생성 요청자
     */
    void createMarketLike(Long marketNo, Member member);

    /**
     * 좋아요 여부 확인
     *
     * @param marketNo : 종아요 확인 no
     * @param member   : 좋아요 확인 member
     */
    boolean checkMarketLike(Long marketNo, Member member);

    /**
     * 좋아요 삭제
     *
     * @param marketNo : 종아요 삭제할 시장 no
     * @param member   : 좋아요 삭제 요청자
     */
    void deleteMarketLike(Long marketNo, Member member);

    /**
     * 좋아요 수
     *
     * @return : 좋아요 수
     */
    Long countMarketLikes(Long marketNo);

    /**
     * 시장 찾기
     *
     * @param marketNo : 찾을 시장 no
     * @return : 시장 Entity
     */
    Market findMarket(Long marketNo);


    /**
     * 총 시장 수
     *
     * @return : 총 시장 수
     */
    Long countMarkets();

    /**
     * 관리자인지 확인
     *
     * @param member : 로그인한 사용자
     */
    void validateIsAdmin(Member member);
}
