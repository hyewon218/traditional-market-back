package com.market.domain.market.dto;

import com.market.domain.market.entity.CategoryEnum;
import com.market.domain.market.entity.Market;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class MarketRequestDto {

    @NotBlank(message = "시장명은 필수 입력 값입니다.")
    private String marketName;

    @NotBlank(message = "시장 주소는 필수 입력 값입니다.")
    private String marketAddr;

    @NotBlank(message = "카테고리는 필수 입력 값입니다.")
    private CategoryEnum category;

    @NotBlank(message = "시장 상세는 필수 입력 값입니다.")
    private String marketDetail;

    private List<String> imageUrls; // 시장 수정 화면에서 남은 기존 이미지들

    private String parkingInfo1; // 주차장 주소 1

    private String parkingInfo2; // 주차장 주소 2

    private String busInfo; // 가까운 버스 정류장 정보(정류장 이름과 버스 번호)

//    private String busAddr; // 가까운 버스 정류장 주소(이 정보를 이용해 지도 위에 핀 설정)
    private String busLat; // 버스 정류장의 위도값

    private String busLng; // 버스 정류장의 경도값

    private String subwayInfo; // 가까운 지하철역 정보(지하철역 이름과 시장과 가까운 출구)

//    private String subwayAddr; // 가까운 지하철역 출구 주소(이 정보를 이용해 지도 위에 핀 설정)
    private String subwayLat; // 지하철역 출구의 위도값
    
    private String subwayLng; // 지하철역 출구의 경도값

    public Market toEntity() {
        return Market.builder()
            .marketName(this.marketName)
            .marketAddr(this.marketAddr)
            .category(this.category)
            .marketDetail(this.marketDetail)
            .parkingInfo1(this.parkingInfo1)
            .parkingInfo2(this.parkingInfo2)
            .busInfo(this.busInfo)
            .busLat(this.busLat)
            .busLng(this.busLng)
            .subwayInfo(this.subwayInfo)
            .subwayLat(this.subwayLat)
            .subwayLng(this.subwayLng)
            .totalSalesPrice(0L)
            .viewCount(0L)
            .build();
    }
}