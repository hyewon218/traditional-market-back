package com.market.domain.item.itemComment.dto;

import com.market.domain.item.itemComment.entity.ItemComment;
import com.market.domain.member.constant.Role;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCommentResponseDto {

    private Long id;

    private String comment;

    private String itemName;

    private String username;

    private String adminId; // 관리자일 경우 실제 본인의 memberId와 비교 위해 설정

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static ItemCommentResponseDto of(ItemComment itemComment) {
        String username = itemComment.getMember().getRole().equals(Role.ADMIN) ? "관리자" : itemComment
            .getMember().getMemberId();

        return ItemCommentResponseDto.builder()
                .id(itemComment.getNo())
                .comment(itemComment.getComment())
                .itemName(itemComment.getItem().getItemName())
                .username(username)
                .adminId(itemComment.getMember().getMemberId())
                .createTime(itemComment.getCreateTime())
                .updateTime(itemComment.getUpdateTime())
                .build();
    }

    // 마스킹 아이디 사용 시 해제하기
//    public static ItemCommentResponseDto of(ItemComment itemComment){
//        String maskingUsername = idMasking(itemComment.getMember().getMemberId());
//
//        return ItemCommentResponseDto.builder()
//            .id(itemComment.getNo())
//            .comment(itemComment.getComment())
//            .itemName(itemComment.getItem().getItemName())
//            .adminId(itemComment.getMember().getMemberId())
//            .username(maskingUsername)
//            .build();
//    }

    // 4 범위 뒤로는 모두 마스킹 처리
    public static String idMasking(String username) {
        return username.replaceAll("(?<=.{4}).", "*");
    }
}
