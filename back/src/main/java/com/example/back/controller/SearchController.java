package com.example.back.controller;

import com.example.back.common.Result;
import com.example.back.dto.request.SearchRequest;
import com.example.back.dto.response.SearchHistoryResponse;
import com.example.back.dto.response.SearchHotKeywordResponse;
import com.example.back.dto.response.SearchResponse;
import com.example.back.dto.response.SearchSuggestionResponse;
import com.example.back.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索控制器
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public Result<SearchResponse> search(@Valid SearchRequest request) {
        return Result.success(searchService.search(request));
    }

    @GetMapping("/suggestions")
    public Result<SearchSuggestionResponse> suggestions(@RequestParam(required = false) String keyword) {
        return Result.success(searchService.suggestions(keyword));
    }

    @GetMapping("/hot")
    public Result<SearchHotKeywordResponse> hotKeywords() {
        return Result.success(searchService.hotKeywords());
    }

    @GetMapping("/history")
    public Result<SearchHistoryResponse> history() {
        return Result.success(searchService.history());
    }

    @DeleteMapping("/history")
    public Result<Void> clearHistory() {
        searchService.clearHistory();
        return Result.success("清除成功", null);
    }
}

