package com.market.domain.item.dto;

import com.market.domain.image.dto.ImageResponseDto;
import com.market.domain.item.constant.ItemSellStatus;
import com.market.domain.item.entity.Item;
import com.market.domain.item.entity.ItemCategoryEnum;
import com.market.domain.item.itemComment.dto.ItemCommentResponseDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ItemResponseDto {
    private Long itemNo;

    private String itemName;

    private int price;

    private int stockNumber;

    private String itemDetail;

    private Integer likes;

    private ItemCategoryEnum itemCategory;

    private ItemSellStatus itemSellStatus;

    private List<ImageResponseDto> imageList;

    private List<ItemCommentResponseDto> itemCommentList;

    public static ItemResponseDto of(Item item) {
        return ItemResponseDto.builder()
            .itemNo(item.getNo())
            .itemName(item.getItemName())
            .price(item.getPrice())
            .stockNumber(item.getStockNumber())
            .itemDetail(item.getItemDetail())
            .itemCategory(item.getItemCategory())
            .itemSellStatus(item.getItemSellStatus())
            .likes(item.getItemLikeList().size())
            .imageList(item.getImageList().stream().map(ImageResponseDto::of).toList())
            .itemCommentList(
                item.getItemCommentList().stream().map(ItemCommentResponseDto::of).toList())
            .build();
    }
}
