package com.example.back.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应结果类
 *
 * @author Corasta Team
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    
    /**
     * 数据列表
     */
    private List<T> items;
    
    /**
     * 分页信息
     */
    private Pagination pagination;
    
    /**
     * 分页信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pagination {
        /**
         * 当前页码
         */
        private Integer page;
        
        /**
         * 每页数量
         */
        private Integer pageSize;
        
        /**
         * 总记录数
         */
        private Long total;
        
        /**
         * 总页数
         */
        private Integer totalPages;
    }
    
    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> items, Integer page, Integer pageSize, Long total) {
        Integer totalPages = (int) Math.ceil((double) total / pageSize);
        Pagination pagination = new Pagination(page, pageSize, total, totalPages);
        return new PageResult<>(items, pagination);
    }
}

