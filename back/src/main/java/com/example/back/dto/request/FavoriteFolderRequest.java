package com.example.back.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 收藏夹请求
 */
@Data
public class FavoriteFolderRequest {

    @NotBlank(message = "收藏夹名称不能为空")
    @Size(max = 50, message = "收藏夹名称不能超过50字符")
    private String name;

    @Size(max = 200, message = "收藏夹描述不能超过200字符")
    private String description;
}

