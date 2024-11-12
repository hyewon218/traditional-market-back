package com.market.domain.notification.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.notification.constant.NotificationType;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

//@Setter
@Getter
@Entity
@Table(name = "notification")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_no")
    private Long no;

    private Long receiverNo;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private NotificationArgs args;

    @Column(name = "removed_at")
    private Timestamp removedAt;

    @Builder.Default
    @Column(name = "is_read")
    private boolean isRead = false; // 알람 읽음 여부

    @Builder.Default
    @Column(name = "is_sent")
    private boolean isSent = false; // true if the notification has been sent to the client

    // 알람을 읽은 상태로 변경
    public void markAsRead() {
        this.isRead = true;
    }

    // 알람을 읽지 않은 상태로 변경
    public void markAsUnread() {
        this.isRead = false;
    }

    public void setIsSent() {
        this.isSent = true;
    }

    public static Notification toEntity(NotificationType notificationType, NotificationArgs args,
        Long receiverNo) {

        return Notification.builder()
            .notificationType(notificationType)
            .args(args)
            .receiverNo(receiverNo)
            .build();
    }
}