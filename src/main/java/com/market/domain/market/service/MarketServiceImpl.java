package com.market.domain.market.service;

import com.market.domain.image.config.AwsS3upload;
import com.market.domain.image.entity.Image;
import com.market.domain.image.repository.ImageRepository;
import com.market.domain.market.dto.MarketRequestDto;
import com.market.domain.market.dto.MarketResponseDto;
import com.market.domain.market.entity.Market;
import com.market.domain.market.repository.MarketRepository;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MarketServiceImpl implements MarketService {

    private final MarketRepository marketRepository;
    private final ImageRepository imageRepository;
    private final AwsS3upload awsS3upload;

    @Override
    @Transactional // 시장 생성
    public void createMarket(MarketRequestDto requestDto, List<MultipartFile> files)
        throws IOException {

        Market market = requestDto.toEntity();

        if (marketRepository.existsMarketByMarketName(requestDto.getMarketName())) {
            throw new BusinessException(ErrorCode.EXISTED_MARKET);
        }
        marketRepository.save(market);

        if (files != null) {
            for (MultipartFile file : files) {
                String fileUrl = awsS3upload.upload(file, "market " + market.getNo());
                if (imageRepository.existsByImageUrlAndNo(fileUrl, market.getNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(market, fileUrl));
            }
        }
    }

    @Override
    @Transactional(readOnly = true) // 시장 목록 조회
    public Page<MarketResponseDto> getMarkets(int page, int size, String sortBy, boolean isAsc) {
        Direction direction = isAsc ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Market> marketList = marketRepository.findAll(pageable);
        return marketList.map(MarketResponseDto::of);
    }

    @Transactional(readOnly = true) // 시장 단건 조회
    public MarketResponseDto getMarket(Long marketNo) {
        Market market = findMarket(marketNo);
        return MarketResponseDto.of(market);
    }

    @Override
    @Transactional // 시장 수정
    public void updateMarket(Long marketNo, MarketRequestDto requestDto, List<MultipartFile> files)
        throws IOException {
        Market market = findMarket(marketNo);

        market.update(requestDto);
        if (files != null) {
            for (MultipartFile file : files) {
                String fileUrl = awsS3upload.upload(file, "market " + market.getNo());
                if (imageRepository.existsByImageUrlAndNo(fileUrl, market.getNo())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(market, fileUrl));
            }
        }
    }

    @Override
    @Transactional // 시장 삭제
    public void deleteMarket(Long marketNo) {
        Market market = findMarket(marketNo);
        marketRepository.delete(market);
    }

    @Override // 시장 찾기
    public Market findMarket(Long marketNo) {
        return marketRepository.findById(marketNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MARKET));
    }
}
