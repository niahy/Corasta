package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 密保问题响应
 */
@Data
@AllArgsConstructor
public class SecurityQuestionResponse {

    private Long id;

    private String question;

    private Integer sortOrder;
}

