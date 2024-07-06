package com.market.domain.item.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.image.entity.Image;
import com.market.domain.item.constant.ItemSellStatus;
import com.market.domain.item.dto.ItemRequestDto;
import com.market.domain.item.itemComment.entity.ItemComment;
import com.market.domain.item.itemLike.entity.ItemLike;
import com.market.domain.shop.entity.Shop;
import com.market.global.exception.ErrorCode;
import com.market.global.exception.OutOfStockException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_no")
    private Long no;

    @Column(nullable = false, length = 50)
    private String itemName; // 상품명

    @Column(nullable = false)
    private int price; // 가격

    @Column(nullable = false)
    private int stockNumber; // 재고 수량

    @Column(nullable = false)
    private String itemDetail; // 상품 상세 설명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategoryEnum itemCategory; // 상품 분류

    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus; // 상품 판매 상태

    @ManyToOne
    @JoinColumn(name = "shop_no")
    private Shop shop;

    @Builder.Default
    @OneToMany(mappedBy = "item", orphanRemoval = true)
    private List<Image> imageList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "item", orphanRemoval = true)
    private List<ItemLike> itemLikeList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "item", orphanRemoval = true)
    private List<ItemComment> itemCommentList = new ArrayList<>();

    public void updateItem(ItemRequestDto requestDto) {
        this.itemName = requestDto.getItemName();
        this.price = requestDto.getPrice();
        this.stockNumber = requestDto.getStockNumber();
        this.itemDetail = requestDto.getItemDetail();
        this.itemSellStatus = requestDto.getItemSellStatus();
    }

    public void removeStock(int stockNumber) { // 주문 수량 갯수만큼 재고 차감
        int restStock = this.stockNumber - stockNumber;
        if (restStock < 0) {
            throw new OutOfStockException(
                ErrorCode.OUT_OF_STOCK.getMessage() + String.format("(현재 재고 수량: %d)",
                    this.stockNumber));
        }
        this.stockNumber = restStock;
    }

    public void addStock(int stockNumber) { // 주문 취소 시 상품의 재고를 증가
        this.stockNumber += stockNumber;
    }
}
