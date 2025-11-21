package com.example.back.controller;

import com.example.back.common.PageResult;
import com.example.back.common.Result;
import com.example.back.dto.request.AnswerQueryRequest;
import com.example.back.dto.request.AnswerRequest;
import com.example.back.dto.request.AnswerVoteRequest;
import com.example.back.dto.response.AnswerDetailResponse;
import com.example.back.dto.response.AnswerResponse;
import com.example.back.dto.response.AnswerVoteResponse;
import com.example.back.service.AnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 回答控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping("/questions/{questionId}/answers")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<AnswerResponse> createAnswer(@PathVariable Long questionId,
                                               @Valid @RequestBody AnswerRequest request) {
        return Result.created("回答成功", answerService.createAnswer(questionId, request));
    }

    @PutMapping("/answers/{id}")
    public Result<AnswerResponse> updateAnswer(@PathVariable Long id,
                                               @Valid @RequestBody AnswerRequest request) {
        return Result.success("更新成功", answerService.updateAnswer(id, request));
    }

    @DeleteMapping("/answers/{id}")
    public Result<Void> deleteAnswer(@PathVariable Long id) {
        answerService.deleteAnswer(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/questions/{questionId}/answers")
    public Result<PageResult<AnswerDetailResponse>> getAnswers(@PathVariable Long questionId,
                                                               @Valid AnswerQueryRequest request) {
        return Result.success(answerService.getAnswers(questionId, request));
    }

    @PostMapping("/answers/{id}/vote")
    public Result<AnswerVoteResponse> voteAnswer(@PathVariable Long id,
                                                 @Valid @RequestBody AnswerVoteRequest request) {
        return Result.success("投票成功", answerService.voteAnswer(id, request));
    }

    @DeleteMapping("/answers/{id}/vote")
    public Result<Void> cancelVote(@PathVariable Long id) {
        answerService.cancelVote(id);
        return Result.success("取消投票成功", null);
    }
}

