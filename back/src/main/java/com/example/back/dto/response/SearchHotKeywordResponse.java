package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 热门搜索响应
 */
@Data
@AllArgsConstructor
public class SearchHotKeywordResponse {

    private List<HotKeyword> keywords;

    @Data
    @AllArgsConstructor
    public static class HotKeyword {
        private String keyword;
        private Long count;
    }
}

