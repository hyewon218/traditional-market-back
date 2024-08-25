package com.market.domain.market.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.image.entity.Image;
import com.market.domain.market.dto.MarketRequestDto;
import com.market.domain.market.marketComment.entity.MarketComment;
import com.market.domain.market.marketLike.entity.MarketLike;
import com.market.domain.shop.entity.Shop;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "market")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Market extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "market_no")
    private Long no;

    @Column(nullable = false)
    private String marketName;

    @Column(nullable = false)
    private String marketAddr;

    @Column(nullable = false)
    private String marketDetail; // 시장 상세 설명

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoryEnum category;
    
    private Long totalSalesPrice; // 해당 시장에 속한 상점들의 총 매출액 합계

    private Long viewCount; // 조회수

    private String parkingInfo1; // 주차장 주소 1
    private String parkingInfo2; // 주차장 주소 2
    private String busInfo; // 가까운 버스 정류장 정보(정류장 이름과 버스 번호)
    private String busLat; // 버스 정류장의 위도값
    private String busLng; // 버스 정류장의 경도값
    private String subwayInfo; // 가까운 지하철역 정보(지하철역 이름과 시장과 가까운 출구)
    private String subwayLat; // 지하철역 출구의 위도값
    private String subwayLng; // 지하철역 출구의 경도값

    @Builder.Default
    @OneToMany(mappedBy = "market", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<Shop> shopList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "market", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<Image> imageList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "market", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<MarketLike> marketLikeList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "market", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<MarketComment> marketCommentList = new ArrayList<>();

    public void update(MarketRequestDto requestDto){
        this.marketName = requestDto.getMarketName();
        this.marketAddr = requestDto.getMarketAddr();
        this.category = requestDto.getCategory();
        this.marketDetail = requestDto.getMarketDetail();
        this.parkingInfo1 = requestDto.getParkingInfo1();
        this.parkingInfo2 = requestDto.getParkingInfo2();
        this.busInfo = requestDto.getBusInfo();
        this.busLat = requestDto.getBusLat();
        this.busLng = requestDto.getBusLng();
        this.subwayInfo = requestDto.getSubwayInfo();
        this.subwayLat = requestDto.getSubwayLat();
        this.subwayLng = requestDto.getSubwayLng();
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public void setTotalSalesPrice(int itemSalesPrice) { // 모든 상품의 총 매출액 합계 설정
        this.totalSalesPrice += itemSalesPrice;
    }

    public void minusTotalSalesPrice(int itemSalesPrice) { // 모든 상품의 총 매출액 합계 설정
        this.totalSalesPrice -= itemSalesPrice;
    }
}
