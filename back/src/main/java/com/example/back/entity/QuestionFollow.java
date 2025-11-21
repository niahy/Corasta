package com.example.back.entity;

import com.example.back.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 问题关注关系
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "question_follows", uniqueConstraints = {
        @UniqueConstraint(name = "uk_question_follow_user", columnNames = {"question_id", "user_id"})
})
public class QuestionFollow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}

