package com.example.back.service;

import com.example.back.common.PageResult;
import com.example.back.dto.request.AnswerQueryRequest;
import com.example.back.dto.request.AnswerRequest;
import com.example.back.dto.request.AnswerVoteRequest;
import com.example.back.dto.response.AnswerDetailResponse;
import com.example.back.dto.response.AnswerResponse;
import com.example.back.dto.response.AnswerVoteResponse;

/**
 * 回答服务
 */
public interface AnswerService {

    AnswerResponse createAnswer(Long questionId, AnswerRequest request);

    AnswerResponse updateAnswer(Long answerId, AnswerRequest request);

    void deleteAnswer(Long answerId);

    PageResult<AnswerDetailResponse> getAnswers(Long questionId, AnswerQueryRequest request);

    AnswerVoteResponse voteAnswer(Long answerId, AnswerVoteRequest request);

    void cancelVote(Long answerId);
}

