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

    @Column(nullable = false)
    private String postCode; // 주소 찾기 api 이용 시 필요, 우편번호

    @Column(nullable = false)
    private String streetAddr; // 주소 찾기 api 이용 시 필요, 도로명 주소

    @Column(nullable = false)
    private String detailAddr; // 주소 찾기 api 이용 시 필요, 상세 주소

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_no")
    private Market market;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_no")
    private Member seller;

    @Builder.Default
    @OneToMany(mappedBy = "shop", orphanRemoval = true)
    private List<Item> itemList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "shop", orphanRemoval = true)
    private List<Image> imageList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "shop", orphanRemoval = true)
    private List<ShopLike> shopLikeList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "shop", orphanRemoval = true)
    private List<ShopComment> shopCommentList = new ArrayList<>();


    public void updateShop(ShopRequestDto requestDto) {
        this.shopName = requestDto.getShopName();
        this.tel = requestDto.getTel();
        this.sellerName = requestDto.getSellerName();
        this.postCode = requestDto.getPostCode();
        this.streetAddr = requestDto.getStreetAddr();
        this.detailAddr = requestDto.getDetailAddr();
    }
    public void updateShopSeller(ShopRequestDto requestDto, Member seller) {
        this.seller = seller;
        this.shopName = requestDto.getShopName();
        this.tel = requestDto.getTel();
        this.sellerName = requestDto.getSellerName();
        this.postCode = requestDto.getPostCode();
        this.streetAddr = requestDto.getStreetAddr();
        this.detailAddr = requestDto.getDetailAddr();
    }
}
