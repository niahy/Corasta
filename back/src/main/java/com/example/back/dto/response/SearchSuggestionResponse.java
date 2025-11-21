package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 搜索建议响应
 */
@Data
@AllArgsConstructor
public class SearchSuggestionResponse {

    private List<String> suggestions;
}

