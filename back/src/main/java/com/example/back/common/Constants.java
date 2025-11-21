package com.example.back.common;

/**
 * 常量类
 *
 * @author Corasta Team
 * @since 1.0
 */
public class Constants {
    
    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 20;
    
    /**
     * 最大分页大小
     */
    public static final int MAX_PAGE_SIZE = 100;
    
    /**
     * 用户名最小长度
     */
    public static final int USERNAME_MIN_LENGTH = 4;
    
    /**
     * 用户名最大长度
     */
    public static final int USERNAME_MAX_LENGTH = 20;
    
    /**
     * 密码最小长度
     */
    public static final int PASSWORD_MIN_LENGTH = 8;
    
    /**
     * 密码最大长度
     */
    public static final int PASSWORD_MAX_LENGTH = 32;
    
    /**
     * 文章标题最大长度
     */
    public static final int ARTICLE_TITLE_MAX_LENGTH = 100;
    
    /**
     * 文章摘要最大长度
     */
    public static final int ARTICLE_SUMMARY_MAX_LENGTH = 200;
    
    /**
     * 内容状态：草稿
     */
    public static final int STATUS_DRAFT = 0;
    
    /**
     * 内容状态：已发布
     */
    public static final int STATUS_PUBLISHED = 1;
    
    /**
     * 内容状态：私密
     */
    public static final int STATUS_PRIVATE = 2;
    
    /**
     * 用户状态：正常
     */
    public static final int USER_STATUS_NORMAL = 1;
    
    /**
     * 用户状态：封禁
     */
    public static final int USER_STATUS_BANNED = 0;
    
    /**
     * 用户角色：普通用户
     */
    public static final int USER_ROLE_NORMAL = 0;
    
    /**
     * 用户角色：超级管理员
     */
    public static final int USER_ROLE_ADMIN = 1;
}

