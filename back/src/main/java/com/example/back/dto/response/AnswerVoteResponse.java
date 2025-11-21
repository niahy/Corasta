package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 回答投票响应
 */
@Data
@AllArgsConstructor
public class AnswerVoteResponse {

    private Integer upvoteCount;
    private Integer downvoteCount;
    private Boolean upvoted;
    private Boolean downvoted;
}

