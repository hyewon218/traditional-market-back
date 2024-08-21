package com.market.domain.item.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.image.entity.Image;
import com.market.domain.item.constant.ItemSellStatus;
import com.market.domain.item.dto.ItemRequestDto;
import com.market.domain.item.itemComment.entity.ItemComment;
import com.market.domain.item.itemLike.entity.ItemLike;
import com.market.domain.shop.entity.Shop;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String itemDetail; // 상품 상세 설명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategoryEnum itemCategory; // 상품 분류

    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus; // 상품 판매 상태

    private Long viewCount = 0L; // 조회수

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_no")
    private Shop shop;

    @Builder.Default
    @OneToMany(mappedBy = "item", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Image> imageList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "item", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ItemLike> itemLikeList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "item", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ItemComment> itemCommentList = new ArrayList<>();

    public void updateItem(ItemRequestDto requestDto) {
        this.itemName = requestDto.getItemName();
        this.price = requestDto.getPrice();
        this.stockNumber = requestDto.getStockNumber();
        this.itemDetail = requestDto.getItemDetail();
        this.itemSellStatus = requestDto.getItemSellStatus();
    }

    public void decreaseStock(int quantity) { // 주문 수량만큼 재고 차감
        int restStock = this.stockNumber - quantity;

        if (restStock < 0) {
            throw new BusinessException(ErrorCode.NOT_LESS_THAN_ZERO);
        }

        this.stockNumber = restStock; // 재고 수량 업데이트

        if (this.stockNumber == 0) {
            this.itemSellStatus = ItemSellStatus.SOLD_OUT; // 재고가 0이면 'SOLD_OUT'으로 상태 변경
        }
    }

    public void addStock(int quantity) { // 주문 취소 시 재고 증가
        this.stockNumber += quantity;

        if (this.stockNumber > 0) {
            this.itemSellStatus = ItemSellStatus.SELL; // 재고가 생기면 상태를 'SELL' 로 변경
        }
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }
}
