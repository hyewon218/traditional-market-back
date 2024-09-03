package com.market.domain.item.dto;

import com.market.domain.image.dto.ImageResponseDto;
import com.market.domain.item.constant.ItemSellStatus;
import com.market.domain.item.entity.Item;
import com.market.domain.item.entity.ItemCategoryEnum;
import com.market.domain.item.itemComment.dto.ItemCommentResponseDto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ItemResponseDto {

    private Long shopNo; // 소속 상점 불러오기 위해 설정

    private Long itemNo;

    private String itemName;

    private int price;

    private int stockNumber;

    private String itemDetail;

    private Integer likes;

    private Long countSales; // 판매량

    private Long totalSalesPrice; // 총 매출액, 판매량 * 가격

    private Long viewCount;

    private ItemCategoryEnum itemCategory;

    private ItemSellStatus itemSellStatus;

    private List<ImageResponseDto> imageList;

    private List<ItemCommentResponseDto> itemCommentList;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
 
    public static ItemResponseDto of(Item item) {
        return ItemResponseDto.builder()
            .shopNo(item.getShop().getNo())
            .itemNo(item.getNo())
            .itemName(item.getItemName())
            .price(item.getPrice())
            .stockNumber(item.getStockNumber())
            .itemDetail(item.getItemDetail())
            .itemCategory(item.getItemCategory())
            .itemSellStatus(item.getItemSellStatus())
            .likes(item.getItemLikeList().size())
            .countSales(item.getCountSales())
            .totalSalesPrice(item.getTotalSalesPrice())
            .viewCount(item.getViewCount())
            .imageList(item.getImageList().stream().map(ImageResponseDto::of).toList())
            .itemCommentList(
                item.getItemCommentList().stream().map(ItemCommentResponseDto::of).toList())
            .createTime(item.getCreateTime())
            .updateTime(item.getUpdateTime())
            .build();
    }
}
