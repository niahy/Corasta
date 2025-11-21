package com.example.back.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 回答投票类型
 */
@Getter
public enum AnswerVoteType {

    UPVOTE(1),
    DOWNVOTE(2);

    private final int code;

    AnswerVoteType(int code) {
        this.code = code;
    }

    public static AnswerVoteType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知的投票类型"));
    }
}

