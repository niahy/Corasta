package com.example.back.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 回答投票请求
 */
@Data
public class AnswerVoteRequest {

    @NotBlank(message = "投票类型不能为空")
    private String type; // upvote / downvote
}

