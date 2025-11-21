package com.example.back.entity;

import com.example.back.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 问题实体
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "questions")
@SQLRestriction("deleted_at IS NULL")
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "best_answer_id")
    private Answer bestAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_article_id")
    private Article relatedArticle;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Builder.Default
    @Column(name = "answer_count", nullable = false)
    private Integer answerCount = 0;

    @Builder.Default
    @Column(name = "follow_count", nullable = false)
    private Integer followCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer status = 1;

    @Builder.Default
    @Column(name = "audit_status", nullable = false)
    private Integer auditStatus = 1;

    @Column(name = "audit_remark", length = 500)
    private String auditRemark;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "question_tags",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    public void increaseAnswerCount() {
        this.answerCount = this.answerCount + 1;
    }

    public void decreaseAnswerCount() {
        this.answerCount = Math.max(0, this.answerCount - 1);
    }

    public void increaseViewCount() {
        this.viewCount = this.viewCount + 1;
    }

    public void increaseFollowCount() {
        this.followCount = this.followCount + 1;
    }

    public void decreaseFollowCount() {
        this.followCount = Math.max(0, this.followCount - 1);
    }
}

