package com.market.domain.market.service;

import com.market.domain.market.dto.MarketRequestDto;
import com.market.domain.market.dto.MarketResponseDto;
import com.market.domain.market.entity.Market;
import java.io.IOException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface MarketService {

    /**
     * 시장 생성
     *
     * @param requestDto : 시장 생성 요청정보
     * @param files      : 시장 생성 첨부파일
     */
    void createMarket(MarketRequestDto requestDto, List<MultipartFile> files) throws IOException;

    /**
     * 시장 목록 조회
     *
     * @return : 조회된 시장들 정보
     */
    Page<MarketResponseDto> getMarkets(int page, int size, String sortBy, boolean isAsc);

    /**
     * 시장 단건 조회
     *
     * @param marketNo : 조회 할 시장 no
     * @return : 조회된 시장 단건 정보
     */
    MarketResponseDto getMarket(Long marketNo);

    /**
     * 시장 수정
     *
     * @param marketNo   : 수정할 시장 no
     * @param requestDto : 시장 수정 요청정보
     * @param files      : 시장 수정 첨부파일
     */
    void updateMarket(Long marketNo, MarketRequestDto requestDto, List<MultipartFile> files)
        throws IOException;

    /**
     * 시장 삭제
     *
     * @param marketNo : 삭제할 시장 no
     */
    void deleteMarket(Long marketNo);

    /**
     * 시장 찾기
     *
     * @param marketNo : 찾을 시장 no
     * @return : 시장 Entity
     */
    Market findMarket(Long marketNo);
}
