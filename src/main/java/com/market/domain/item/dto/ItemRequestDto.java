package com.market.domain.item.dto;

import com.market.domain.item.constant.ItemSellStatus;
import com.market.domain.item.entity.Item;
import com.market.domain.item.entity.ItemCategoryEnum;
import com.market.domain.shop.entity.Shop;
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
public class ItemRequestDto {

    private Long shopNo; // 소속 상점 no

    @NotBlank(message = "상품명은 필수 입력 값입니다.")
    private String itemName;

    @NotBlank(message = "상품가격은 필수 입력 값입니다.")
    private int price;

    @NotBlank(message = "상품 상세는 필수 입력 값입니다.")
    private String itemDetail;

    @NotBlank(message = "재고는 필수 입력 값입니다.")
    private int stockNumber;

    @NotBlank(message = "상품 분류는 필수 입력 값입니다.")
    private ItemCategoryEnum itemCategory; // 프론트에서 드롭다운으로 구현하기

    private ItemSellStatus itemSellStatus;

    private List<String> imageUrls; // 상품 수정 화면에서 남은 기존 이미지들

    public Item toEntity(Shop shop) {
        return Item.builder()
            .shop(shop)
            .itemName(this.itemName)
            .price(this.price)
            .stockNumber(this.stockNumber)
            .itemDetail(this.itemDetail)
            .itemCategory(this.itemCategory)
            .itemSellStatus(this.itemSellStatus)
            .build();
    }
}
