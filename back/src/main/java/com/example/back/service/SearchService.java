package com.example.back.service;

import com.example.back.dto.request.SearchRequest;
import com.example.back.dto.response.SearchHistoryResponse;
import com.example.back.dto.response.SearchHotKeywordResponse;
import com.example.back.dto.response.SearchResponse;
import com.example.back.dto.response.SearchSuggestionResponse;

/**
 * 搜索服务
 */
public interface SearchService {

    SearchResponse search(SearchRequest request);

    SearchSuggestionResponse suggestions(String keyword);

    SearchHotKeywordResponse hotKeywords();

    SearchHistoryResponse history();

    void clearHistory();
}

