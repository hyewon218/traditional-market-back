package com.market.domain.chatRoom.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.chat.entity.Chat;
import com.market.domain.member.entity.Member;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "chat_room")
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long no;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_no")
    private Member member; //sender

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receiver_no")
    private Member receiver;

    private String title; // 채팅방 이름

    @Builder.Default
    @OneToMany(mappedBy = "chatRoom", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<Chat> ChatMessages = new ArrayList<>();

    @Builder.Default
    private boolean isRead = false; // 채팅방 읽음 여부

    // 채팅방을 읽은 상태로 변경
    public void markAsRead() {
        this.isRead = true;
    }

    // 채팅방을 읽지 않은 상태로 변경
    public void markAsUnread() {
        this.isRead = false;
    }

    // 보낸 사람을 기준으로 받는 사람을 결정
    public Member getRecipient(Member sender) {
        if (this.member.getMemberId().equals(sender.getMemberId())) {
            return this.receiver;
        } else if (this.receiver.getMemberId().equals(sender.getMemberId())) {
            return this.member;
        } else {
            throw new BusinessException(ErrorCode.SENDER_NOT_FOUND);
        }
    }
}