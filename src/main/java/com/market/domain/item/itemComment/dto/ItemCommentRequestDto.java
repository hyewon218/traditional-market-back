package com.market.domain.item.itemComment.dto;

import com.market.domain.item.entity.Item;
import com.market.domain.item.itemComment.entity.ItemComment;
import com.market.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemCommentRequestDto {

    private long itemNo; // 댓글 남길 상품 no
    private String comment;

    public ItemComment toEntity(Item item, Member member) {
        return ItemComment.builder()
            .comment(this.comment)
            .item(item)
            .member(member)
            .build();
    }
}
