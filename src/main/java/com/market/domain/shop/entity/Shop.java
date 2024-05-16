package com.market.domain.shop.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.image.entity.Image;
import com.market.domain.item.entity.Item;
import com.market.domain.market.entity.Market;
import com.market.domain.shop.dto.ShopRequestDto;
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

    private String shopName;

    private String tel;

    private String owner;

    private String postCode; // 주소 찾기 api 이용 시 필요, 우편번호

    private String streetAddr; // 주소 찾기 api 이용 시 필요, 도로명 주소

    private String detailAddr; // 주소 찾기 api 이용 시 필요, 상세 주소

    @ManyToOne
    @JoinColumn(name = "market_no")
    private Market market;

    @OneToMany(mappedBy = "shop", orphanRemoval = true)
    private List<Item> itemList = new ArrayList<>();

    @OneToMany(mappedBy = "shop", orphanRemoval = true)
    private List<Image> image = new ArrayList<>();

    public void update(ShopRequestDto requestDto) {
        this.shopName = requestDto.getShopName();
        this.tel = requestDto.getTel();
        this.owner = requestDto.getOwner();
        this.postCode = requestDto.getPostCode();
        this.streetAddr = requestDto.getStreetAddr();
        this.detailAddr = requestDto.getDetailAddr();
    }
}
