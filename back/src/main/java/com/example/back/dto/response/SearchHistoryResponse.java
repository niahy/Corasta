package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 搜索历史响应
 */
@Data
@AllArgsConstructor
public class SearchHistoryResponse {

    private List<String> items;
}

