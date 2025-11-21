
package com.example.back.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 内容审核请求
 */
@Data
public class AdminContentReviewRequest {

    @NotBlank(message = "操作类型不能为空")
    @Pattern(regexp = "approve|reject", message = "操作类型不合法")
    private String action;

    private String remark;
}

