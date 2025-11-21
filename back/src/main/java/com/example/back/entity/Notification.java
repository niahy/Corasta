package com.example.back.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 通知实体
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String content;

    @Column(name = "target_type", length = 20)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean read = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void markRead() {
        if (Boolean.TRUE.equals(this.read)) {
            return;
        }
        this.read = true;
        this.readAt = LocalDateTime.now();
    }
}

