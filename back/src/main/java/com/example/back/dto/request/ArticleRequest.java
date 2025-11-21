package com.example.back.dto.request;

import com.example.back.common.Constants;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 文章创建/更新请求
 */
@Data
public class ArticleRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = Constants.ARTICLE_TITLE_MAX_LENGTH, message = "标题长度不能超过100字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @Size(max = Constants.ARTICLE_SUMMARY_MAX_LENGTH, message = "摘要长度不能超过200字符")
    private String summary;

    @Size(max = 500, message = "封面图URL过长")
    private String coverImage;

    @Size(max = 100, message = "Slug长度不能超过100字符")
    private String slug;

    private Long categoryId;

    @Size(max = 10, message = "标签最多10个")
    private List<@NotBlank(message = "标签不能为空")
            @Size(max = 50, message = "标签长度不能超过50字符") String> tags;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值错误")
    @Max(value = 2, message = "状态值错误")
    private Integer status;
}

