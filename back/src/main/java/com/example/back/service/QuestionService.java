package com.example.back.service;

import com.example.back.common.PageResult;
import com.example.back.dto.request.QuestionQueryRequest;
import com.example.back.dto.request.QuestionRequest;
import com.example.back.dto.response.QuestionDetailResponse;
import com.example.back.dto.response.QuestionListItemResponse;
import com.example.back.dto.response.QuestionResponse;

/**
 * 问题服务
 */
public interface QuestionService {

    QuestionResponse createQuestion(QuestionRequest request);

    QuestionResponse updateQuestion(Long questionId, QuestionRequest request);

    void deleteQuestion(Long questionId);

    QuestionDetailResponse getQuestionDetail(Long questionId);

    PageResult<QuestionListItemResponse> getQuestionList(QuestionQueryRequest request);

    void followQuestion(Long questionId);

    void unfollowQuestion(Long questionId);

    void markBestAnswer(Long questionId, Long answerId);
}

