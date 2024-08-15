package com.market.domain.item.itemComment.dto;

import com.market.domain.item.itemComment.entity.ItemComment;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemCommentResponseDto {

    private Long id;

    private String comment;

    private String itemName;

    private String username;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static ItemCommentResponseDto of(ItemComment itemComment) {
        return ItemCommentResponseDto.builder()
            .id(itemComment.getNo())
            .comment(itemComment.getComment())
            .itemName(itemComment.getItem().getItemName())
            .username(itemComment.getMember().getMemberId())
            .createTime(itemComment.getCreateTime())
            .updateTime(itemComment.getUpdateTime())
            .build();
    }
}
