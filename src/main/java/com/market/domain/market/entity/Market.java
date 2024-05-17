package com.market.domain.market.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.image.entity.Image;
import com.market.domain.market.dto.MarketRequestDto;
import com.market.domain.market.marketComment.entity.MarketComment;
import com.market.domain.market.marketLike.entity.MarketLike;
import com.market.domain.shop.entity.Shop;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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

    @OneToMany(mappedBy = "market", orphanRemoval = true)
    private List<Image> imageList = new ArrayList<>();

    @OneToMany(mappedBy = "market", orphanRemoval = true)
    private List<Shop> shopList = new ArrayList<>();

    @OneToMany(mappedBy = "market", orphanRemoval = true)
    private List<MarketLike> marketLikeList = new ArrayList<>();

    @OneToMany(mappedBy = "market", orphanRemoval = true)
    private List<MarketComment> marketCommentList = new ArrayList<>();

    public void update(MarketRequestDto requestDto){
        this.marketName = requestDto.getMarketName();
        this.marketAddr = requestDto.getMarketAddr();
        this.marketDetail = requestDto.getMarketDetail();
    }
}
