package com.market.domain.shop.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.image.entity.Image;
import com.market.domain.item.entity.Item;
import com.market.domain.market.entity.Market;
import com.market.domain.member.entity.Member;
import com.market.domain.shop.dto.ShopRequestDto;
import com.market.domain.shop.shopComment.entity.ShopComment;
import com.market.domain.shop.shopLike.entity.ShopLike;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "shop")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shop extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_no")
    private Long no;

    @Column(nullable = false)
    private String shopName;

    @Column(nullable = false)
    private String tel;

    @Column(nullable = false)
    private String sellerName;

    private String shopAddr;

    private String shopLat; // 상점의 위도값(해당값 이용해서 지도에 핀 설정)

    private String shopLng; // 상점의 경도값(해당값 이용해서 지도에 핀 설정)
    
    private Long viewCount; // 조회수

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoryEnum category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_no")
    private Market market;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_no")
    private Member seller;

    @Builder.Default
    @OneToMany(mappedBy = "shop", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<Item> itemList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "shop", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<Image> imageList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "shop", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<ShopLike> shopLikeList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "shop", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<ShopComment> shopCommentList = new ArrayList<>();

    public void updateShop(ShopRequestDto requestDto) {
        this.shopName = requestDto.getShopName();
        this.tel = requestDto.getTel();
        this.sellerName = requestDto.getSellerName();
        this.shopAddr = requestDto.getShopAddr();
        this.shopLat = requestDto.getShopLat();
        this.shopLng = requestDto.getShopLng();
        this.category = requestDto.getCategory();
    }
    public void updateShopSeller(ShopRequestDto requestDto, Member seller) {
        this.seller = seller;
        this.shopName = requestDto.getShopName();
        this.tel = requestDto.getTel();
        this.sellerName = requestDto.getSellerName();
        this.shopAddr = requestDto.getShopAddr();
        this.shopLat = requestDto.getShopLat();
        this.shopLng = requestDto.getShopLng();
        this.category = requestDto.getCategory();
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }
}
