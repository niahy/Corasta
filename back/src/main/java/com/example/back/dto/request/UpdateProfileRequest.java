package com.example.back.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新个人资料请求
 */
@Data
public class UpdateProfileRequest {

    @Size(min = 1, max = 20, message = "昵称长度需在1-20之间")
    private String nickname;

    @Size(max = 200, message = "简介长度不能超过200字符")
    private String bio;

    @Size(max = 500, message = "头像地址过长")
    private String avatar;
}

