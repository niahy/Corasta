package com.example.back.entity;

import com.example.back.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * 标签实体
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tags")
@SQLRestriction("deleted_at IS NULL")
public class Tag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @Builder.Default
    @Column(name = "use_count", nullable = false)
    private Integer useCount = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

