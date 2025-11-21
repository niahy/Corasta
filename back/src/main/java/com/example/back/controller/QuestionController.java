package com.example.back.controller;

import com.example.back.common.PageResult;
import com.example.back.common.Result;
import com.example.back.dto.request.QuestionQueryRequest;
import com.example.back.dto.request.QuestionRequest;
import com.example.back.dto.response.QuestionDetailResponse;
import com.example.back.dto.response.QuestionListItemResponse;
import com.example.back.dto.response.QuestionResponse;
import com.example.back.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 问答控制器
 */
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<QuestionResponse> createQuestion(@Valid @RequestBody QuestionRequest request) {
        
        return Result.created("创建成功", questionService.createQuestion(request));
    }

    @PutMapping("/{id}")
    public Result<QuestionResponse> updateQuestion(@PathVariable Long id,
                                                   @Valid @RequestBody QuestionRequest request) {
        return Result.success("更新成功", questionService.updateQuestion(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/{id}")
    public Result<QuestionDetailResponse> getQuestionDetail(@PathVariable Long id) {
        return Result.success(questionService.getQuestionDetail(id));
    }

    @GetMapping
    public Result<PageResult<QuestionListItemResponse>> listQuestions(@Valid QuestionQueryRequest request) {
        return Result.success(questionService.getQuestionList(request));
    }

    @PostMapping("/{id}/follow")
    public Result<Void> followQuestion(@PathVariable Long id) {
        questionService.followQuestion(id);
        return Result.success("关注成功", null);
    }

    @DeleteMapping("/{id}/follow")
    public Result<Void> unfollowQuestion(@PathVariable Long id) {
        questionService.unfollowQuestion(id);
        return Result.success("取消关注成功", null);
    }

    @PutMapping("/{questionId}/best-answer/{answerId}")
    public Result<Void> markBestAnswer(@PathVariable Long questionId, @PathVariable Long answerId) {
        questionService.markBestAnswer(questionId, answerId);
        return Result.success("标记成功", null);
    }
}

