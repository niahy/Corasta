package com.example.back.entity;

import com.example.back.entity.base.BaseEntity;
import com.example.back.enums.AnswerVoteType;
import com.example.back.enums.converter.AnswerVoteTypeConverter;
import jakarta.persistence.*;
import lombok.*;

/**
 * 回答投票记录
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "answer_votes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_answer_votes_answer_user", columnNames = {"answer_id", "user_id"})
})
public class AnswerVote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "answer_id")
    private Answer answer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Convert(converter = AnswerVoteTypeConverter.class)
    @Column(name = "vote_type", nullable = false)
    private AnswerVoteType voteType;
}

