package com.market.domain.chatRoom.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.chat.entity.Chat;
import com.market.domain.member.entity.Member;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
    @Column(name = "chat_room_no")
    private Long no;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_no")
    private Member member; //sender

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "chat_room_receivers",
        joinColumns = @JoinColumn(name = "chat_room_no"),
        inverseJoinColumns = @JoinColumn(name = "receiver_no")
    )
    private List<Member> receivers = new ArrayList<>(); // 여러 명의 receiver(관리자)

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

    // 보낸 사람에 따라 받는 사람(들)을 결정
    public List<Member> getRecipients(Member sender) {
        if (this.member.getMemberNo().equals(sender.getMemberNo())) {
            return this.receivers; // sender 가 방을 생성한 사람인 경우 receivers(관리자) 목록 반환
        } else {
            // sender 가 receiver 중 한 명인지 확인 (no 필드를 비교)
            boolean isReceiver = this.receivers.stream()
                .anyMatch(receiver -> receiver.getMemberNo().equals(sender.getMemberNo()));

            if (isReceiver) {
                // 방을 생성한 사람만 포함된 리스트 반환
                return List.of(this.member); // 방 생성자를 포함하는 리스트 반환
            } else {
                throw new BusinessException(ErrorCode.SENDER_NOT_FOUND);
            }
        }
    }
}