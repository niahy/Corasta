-- =====================================================
-- Corasta 数据库初始化脚本
-- 数据库名称: corasta
-- MySQL版本: 8.0+
-- 创建时间: 2024
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `corasta` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `corasta`;

-- =====================================================
-- 1. 用户模块
-- =====================================================

-- 1.1 用户表
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(20) NOT NULL COMMENT '用户名（唯一）',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（明文存储）',
    `nickname` VARCHAR(20) NULL COMMENT '昵称',
    `avatar` VARCHAR(500) NULL COMMENT '头像URL',
    `bio` VARCHAR(200) NULL COMMENT '个人简介',
    `email` VARCHAR(100) NULL COMMENT '邮箱（预留）',
    `phone` VARCHAR(20) NULL COMMENT '手机号（预留）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-封禁，1-正常（第三阶段）',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色：0-普通用户，1-超级管理员（第三阶段）',
    `last_login_at` DATETIME NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) NULL COMMENT '最后登录IP',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME NULL COMMENT '删除时间（软删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_username` (`username`),
    INDEX `idx_users_status` (`status`),
    INDEX `idx_users_created_at` (`created_at`),
    INDEX `idx_users_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 1.2 密保问题表
CREATE TABLE IF NOT EXISTS `security_questions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `question` VARCHAR(200) NOT NULL COMMENT '问题内容',
    `answer` VARCHAR(255) NOT NULL COMMENT '答案（加密存储）',
    `sort_order` TINYINT NOT NULL DEFAULT 0 COMMENT '排序顺序（1-3）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_security_questions_user_id` (`user_id`),
    CONSTRAINT `fk_security_questions_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='密保问题表';

-- 1.3 用户隐私设置表
CREATE TABLE IF NOT EXISTS `user_privacy_settings` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID（唯一）',
    `homepage_visible` TINYINT NOT NULL DEFAULT 1 COMMENT '主页可见性：0-仅关注者，1-公开',
    `content_visible` TINYINT NOT NULL DEFAULT 1 COMMENT '内容可见性：0-仅关注者，1-公开',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_privacy_settings_user_id` (`user_id`),
    CONSTRAINT `fk_user_privacy_settings_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户隐私设置表';

-- =====================================================
-- 2. 内容模块
-- =====================================================

-- 2.1 分类表
CREATE TABLE IF NOT EXISTS `categories` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称（唯一）',
    `description` VARCHAR(200) NULL COMMENT '分类描述',
    `icon` VARCHAR(200) NULL COMMENT '分类图标（预留）',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `parent_id` BIGINT NULL COMMENT '父分类ID（预留，支持多级分类）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME NULL COMMENT '删除时间（软删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_categories_name` (`name`),
    INDEX `idx_categories_parent_id` (`parent_id`),
    INDEX `idx_categories_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- 2.2 标签表
CREATE TABLE IF NOT EXISTS `tags` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称（唯一）',
    `description` VARCHAR(200) NULL COMMENT '标签描述',
    `use_count` INT NOT NULL DEFAULT 0 COMMENT '使用次数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME NULL COMMENT '删除时间（软删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tags_name` (`name`),
    INDEX `idx_tags_use_count` (`use_count`),
    INDEX `idx_tags_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 2.3 文章表
CREATE TABLE IF NOT EXISTS `articles` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '作者ID',
    `title` VARCHAR(100) NOT NULL COMMENT '文章标题',
    `content` LONGTEXT NOT NULL COMMENT '文章内容（Markdown）',
    `summary` VARCHAR(200) NULL COMMENT '文章摘要',
    `cover_image` VARCHAR(500) NULL COMMENT '封面图URL',
    `slug` VARCHAR(100) NULL COMMENT '自定义URL（SEO友好）',
    `category_id` BIGINT NULL COMMENT '分类ID',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-已发布，2-私密',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '阅读量',
    `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
    `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
    `share_count` INT NOT NULL DEFAULT 0 COMMENT '分享数',
    `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶（预留）',
    `audit_status` TINYINT NOT NULL DEFAULT 1 COMMENT '审核状态：0-待审核，1-通过，2-拒绝（第三阶段）',
    `audit_remark` VARCHAR(500) NULL COMMENT '审核备注（第三阶段）',
    `published_at` DATETIME NULL COMMENT '发布时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME NULL COMMENT '删除时间（软删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_articles_user_id` (`user_id`),
    INDEX `idx_articles_category_id` (`category_id`),
    INDEX `idx_articles_status` (`status`),
    INDEX `idx_articles_published_at` (`published_at`),
    INDEX `idx_articles_view_count` (`view_count`),
    INDEX `idx_articles_created_at` (`created_at`),
    INDEX `idx_articles_deleted_at` (`deleted_at`),
    INDEX `idx_articles_user_status` (`user_id`, `status`),
    INDEX `idx_articles_category_status` (`category_id`, `status`),
    -- slug唯一索引：NULL值不参与唯一性约束，允许多个NULL值
    UNIQUE KEY `uk_articles_slug` (`slug`),
    CONSTRAINT `fk_articles_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_articles_category_id` FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- 2.4 文章标签关联表
CREATE TABLE IF NOT EXISTS `article_tags` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_tags_article_tag` (`article_id`, `tag_id`),
    INDEX `idx_article_tags_tag_id` (`tag_id`),
    CONSTRAINT `fk_article_tags_article_id` FOREIGN KEY (`article_id`) REFERENCES `articles`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_article_tags_tag_id` FOREIGN KEY (`tag_id`) REFERENCES `tags`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';

-- 2.5 问题表
CREATE TABLE IF NOT EXISTS `questions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '提问者ID',
    `title` VARCHAR(100) NOT NULL COMMENT '问题标题',
    `description` TEXT NOT NULL COMMENT '问题描述（Markdown）',
    `best_answer_id` BIGINT NULL COMMENT '最佳回答ID',
    `related_article_id` BIGINT NULL COMMENT '关联文章ID',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '查看数',
    `answer_count` INT NOT NULL DEFAULT 0 COMMENT '回答数',
    `follow_count` INT NOT NULL DEFAULT 0 COMMENT '关注数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
    `audit_status` TINYINT NOT NULL DEFAULT 1 COMMENT '审核状态：0-待审核，1-通过，2-拒绝（第三阶段）',
    `audit_remark` VARCHAR(500) NULL COMMENT '审核备注（第三阶段）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME NULL COMMENT '删除时间（软删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_questions_user_id` (`user_id`),
    INDEX `idx_questions_best_answer_id` (`best_answer_id`),
    INDEX `idx_questions_related_article_id` (`related_article_id`),
    INDEX `idx_questions_answer_count` (`answer_count`),
    INDEX `idx_questions_created_at` (`created_at`),
    INDEX `idx_questions_deleted_at` (`deleted_at`),
    CONSTRAINT `fk_questions_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_questions_related_article_id` FOREIGN KEY (`related_article_id`) REFERENCES `articles`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题表';

-- 2.6 问题标签关联表
CREATE TABLE IF NOT EXISTS `question_tags` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `question_id` BIGINT NOT NULL COMMENT '问题ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_question_tags_question_tag` (`question_id`, `tag_id`),
    INDEX `idx_question_tags_tag_id` (`tag_id`),
    CONSTRAINT `fk_question_tags_question_id` FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_question_tags_tag_id` FOREIGN KEY (`tag_id`) REFERENCES `tags`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题标签关联表';

-- 2.7 问题关注表
CREATE TABLE IF NOT EXISTS `question_follows` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `question_id` BIGINT NOT NULL COMMENT '问题ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_question_follow_user` (`question_id`, `user_id`),
    INDEX `idx_question_follows_question_id` (`question_id`),
    INDEX `idx_question_follows_user_id` (`user_id`),
    CONSTRAINT `fk_question_follows_question_id` FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_question_follows_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题关注表';

-- 2.8 回答表
CREATE TABLE IF NOT EXISTS `answers` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `question_id` BIGINT NOT NULL COMMENT '问题ID',
    `user_id` BIGINT NOT NULL COMMENT '回答者ID',
    `content` TEXT NOT NULL COMMENT '回答内容（Markdown）',
    `upvote_count` INT NOT NULL DEFAULT 0 COMMENT '赞同数',
    `downvote_count` INT NOT NULL DEFAULT 0 COMMENT '反对数',
    `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
    `is_best` TINYINT NOT NULL DEFAULT 0 COMMENT '是否最佳回答',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
    `audit_status` TINYINT NOT NULL DEFAULT 1 COMMENT '审核状态：0-待审核，1-通过，2-拒绝（第三阶段）',
    `audit_remark` VARCHAR(500) NULL COMMENT '审核备注（第三阶段）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME NULL COMMENT '删除时间（软删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_answers_question_id` (`question_id`),
    INDEX `idx_answers_user_id` (`user_id`),
    INDEX `idx_answers_is_best` (`is_best`),
    INDEX `idx_answers_upvote_count` (`upvote_count`),
    INDEX `idx_answers_created_at` (`created_at`),
    INDEX `idx_answers_deleted_at` (`deleted_at`),
    INDEX `idx_answers_question_is_best` (`question_id`, `is_best`),
    CONSTRAINT `fk_answers_question_id` FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_answers_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回答表';

-- 2.9 回答投票表
CREATE TABLE IF NOT EXISTS `answer_votes` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `answer_id` BIGINT NOT NULL COMMENT '回答ID',
    `user_id` BIGINT NOT NULL COMMENT '投票用户ID',
    `vote_type` TINYINT NOT NULL COMMENT '投票类型：1-赞同，2-反对',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_answer_votes_answer_user` (`answer_id`, `user_id`),
    INDEX `idx_answer_votes_user_id` (`user_id`),
    CONSTRAINT `fk_answer_votes_answer_id` FOREIGN KEY (`answer_id`) REFERENCES `answers`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_answer_votes_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回答投票表';

-- 添加questions表对answers表的外键约束（在answers表创建后）
ALTER TABLE `questions` ADD CONSTRAINT `fk_questions_best_answer_id` FOREIGN KEY (`best_answer_id`) REFERENCES `answers`(`id`) ON DELETE SET NULL;

-- =====================================================
-- 3. 互动模块
-- =====================================================

-- 3.1 评论表
CREATE TABLE IF NOT EXISTS `comments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `target_type` VARCHAR(20) NOT NULL COMMENT '目标类型：article/answer/video',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `user_id` BIGINT NOT NULL COMMENT '评论者ID',
    `parent_id` BIGINT NULL COMMENT '父评论ID（二级评论）',
    `content` VARCHAR(1000) NOT NULL COMMENT '评论内容（Markdown）',
    `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    `reply_count` INT NOT NULL DEFAULT 0 COMMENT '回复数',
    `is_pinned` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME NULL COMMENT '删除时间（软删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_comments_target` (`target_type`, `target_id`),
    INDEX `idx_comments_user_id` (`user_id`),
    INDEX `idx_comments_parent_id` (`parent_id`),
    INDEX `idx_comments_created_at` (`created_at`),
    INDEX `idx_comments_deleted_at` (`deleted_at`),
    CONSTRAINT `fk_comments_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comments_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `comments`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 3.2 点赞表
CREATE TABLE IF NOT EXISTS `likes` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `target_type` VARCHAR(20) NOT NULL COMMENT '目标类型：article/answer/comment/video',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `user_id` BIGINT NOT NULL COMMENT '点赞用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_likes_target_user` (`target_type`, `target_id`, `user_id`),
    INDEX `idx_likes_user_id` (`user_id`),
    INDEX `idx_likes_target` (`target_type`, `target_id`),
    CONSTRAINT `fk_likes_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';

-- 3.3 收藏夹表
CREATE TABLE IF NOT EXISTS `favorite_folders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(50) NOT NULL COMMENT '收藏夹名称',
    `description` VARCHAR(200) NULL COMMENT '收藏夹描述',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME NULL COMMENT '删除时间（软删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_favorite_folders_user_id` (`user_id`),
    INDEX `idx_favorite_folders_deleted_at` (`deleted_at`),
    CONSTRAINT `fk_favorite_folders_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏夹表';

-- 3.4 收藏表
CREATE TABLE IF NOT EXISTS `favorites` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `target_type` VARCHAR(20) NOT NULL COMMENT '目标类型：article/answer',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `user_id` BIGINT NOT NULL COMMENT '收藏用户ID',
    `folder_id` BIGINT NULL COMMENT '收藏夹ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_favorites_target_user` (`target_type`, `target_id`, `user_id`),
    INDEX `idx_favorites_user_id` (`user_id`),
    INDEX `idx_favorites_folder_id` (`folder_id`),
    INDEX `idx_favorites_target` (`target_type`, `target_id`),
    CONSTRAINT `fk_favorites_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_favorites_folder_id` FOREIGN KEY (`folder_id`) REFERENCES `favorite_folders`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- 3.5 分享表
CREATE TABLE IF NOT EXISTS `shares` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `target_type` VARCHAR(20) NOT NULL COMMENT '目标类型：article/answer/video',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `user_id` BIGINT NULL COMMENT '分享用户ID（可为NULL，匿名分享）',
    `share_platform` VARCHAR(20) NULL COMMENT '分享平台：wechat/weibo/qq/link',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_shares_target` (`target_type`, `target_id`),
    INDEX `idx_shares_user_id` (`user_id`),
    INDEX `idx_shares_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分享表';

-- =====================================================
-- 4. 社交模块
-- =====================================================

-- 4.1 关注表
CREATE TABLE IF NOT EXISTS `follows` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `follower_id` BIGINT NOT NULL COMMENT '关注者ID',
    `following_id` BIGINT NOT NULL COMMENT '被关注者ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follows_follower_following` (`follower_id`, `following_id`),
    INDEX `idx_follows_follower_id` (`follower_id`),
    INDEX `idx_follows_following_id` (`following_id`),
    CONSTRAINT `fk_follows_follower_id` FOREIGN KEY (`follower_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_follows_following_id` FOREIGN KEY (`following_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注表';

-- 4.2 通知表
CREATE TABLE IF NOT EXISTS `notifications` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
    `type` VARCHAR(50) NOT NULL COMMENT '通知类型：comment/reply/like/follow/answer/adopt/invite',
    `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
    `content` VARCHAR(500) NULL COMMENT '通知内容',
    `target_type` VARCHAR(20) NULL COMMENT '目标类型：article/answer/comment/question',
    `target_id` BIGINT NULL COMMENT '目标ID',
    `sender_id` BIGINT NULL COMMENT '发送者ID',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    `read_at` DATETIME NULL COMMENT '阅读时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_notifications_user_id` (`user_id`),
    INDEX `idx_notifications_is_read` (`is_read`),
    INDEX `idx_notifications_created_at` (`created_at`),
    INDEX `idx_notifications_user_read` (`user_id`, `is_read`),
    CONSTRAINT `fk_notifications_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_notifications_sender_id` FOREIGN KEY (`sender_id`) REFERENCES `users`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- =====================================================
-- 5. 视频模块（第二阶段：待开发）
-- =====================================================

-- 5.1 视频表
CREATE TABLE IF NOT EXISTS `videos` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '作者ID',
    `title` VARCHAR(100) NOT NULL COMMENT '视频标题',
    `description` TEXT NULL COMMENT '视频简介（Markdown）',
    `cover_image` VARCHAR(500) NULL COMMENT '封面图URL',
    `category_id` BIGINT NULL COMMENT '分类ID',
    `related_article_id` BIGINT NULL COMMENT '关联文章ID',
    `file_path` VARCHAR(500) NULL COMMENT '原始文件路径',
    `file_size` BIGINT NULL COMMENT '文件大小（字节）',
    `duration` INT NULL COMMENT '视频时长（秒）',
    `hls_url_1080p` VARCHAR(500) NULL COMMENT '1080p HLS URL',
    `hls_url_720p` VARCHAR(500) NULL COMMENT '720p HLS URL',
    `hls_url_480p` VARCHAR(500) NULL COMMENT '480p HLS URL',
    `transcode_status` TINYINT NOT NULL DEFAULT 0 COMMENT '转码状态：0-等待中，1-转码中，2-完成，3-失败',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-已发布，2-私密',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '播放量',
    `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
    `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
    `share_count` INT NOT NULL DEFAULT 0 COMMENT '分享数',
    `audit_status` TINYINT NOT NULL DEFAULT 1 COMMENT '审核状态：0-待审核，1-通过，2-拒绝（第三阶段）',
    `audit_remark` VARCHAR(500) NULL COMMENT '审核备注（第三阶段）',
    `published_at` DATETIME NULL COMMENT '发布时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME NULL COMMENT '删除时间（软删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_videos_user_id` (`user_id`),
    INDEX `idx_videos_category_id` (`category_id`),
    INDEX `idx_videos_status` (`status`),
    INDEX `idx_videos_transcode_status` (`transcode_status`),
    INDEX `idx_videos_published_at` (`published_at`),
    INDEX `idx_videos_view_count` (`view_count`),
    INDEX `idx_videos_created_at` (`created_at`),
    INDEX `idx_videos_deleted_at` (`deleted_at`),
    INDEX `idx_videos_user_status` (`user_id`, `status`),
    CONSTRAINT `fk_videos_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_videos_category_id` FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_videos_related_article_id` FOREIGN KEY (`related_article_id`) REFERENCES `articles`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频表';

-- 5.2 视频标签关联表
CREATE TABLE IF NOT EXISTS `video_tags` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `video_id` BIGINT NOT NULL COMMENT '视频ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_video_tags_video_tag` (`video_id`, `tag_id`),
    INDEX `idx_video_tags_tag_id` (`tag_id`),
    CONSTRAINT `fk_video_tags_video_id` FOREIGN KEY (`video_id`) REFERENCES `videos`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_video_tags_tag_id` FOREIGN KEY (`tag_id`) REFERENCES `tags`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频标签关联表';

-- 5.3 弹幕表
CREATE TABLE IF NOT EXISTS `danmaku` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `video_id` BIGINT NOT NULL COMMENT '视频ID',
    `user_id` BIGINT NULL COMMENT '用户ID（可为NULL，匿名弹幕）',
    `content` VARCHAR(50) NOT NULL COMMENT '弹幕内容',
    `time` DECIMAL(10,2) NOT NULL COMMENT '弹幕时间（秒）',
    `color` VARCHAR(20) NOT NULL DEFAULT '#FFFFFF' COMMENT '弹幕颜色',
    `position` TINYINT NOT NULL DEFAULT 0 COMMENT '位置：0-滚动，1-顶部，2-底部',
    `font_size` TINYINT NOT NULL DEFAULT 25 COMMENT '字体大小（预留）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted_at` DATETIME NULL COMMENT '删除时间（软删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_danmaku_video_id` (`video_id`),
    INDEX `idx_danmaku_video_time` (`video_id`, `time`),
    INDEX `idx_danmaku_user_id` (`user_id`),
    INDEX `idx_danmaku_deleted_at` (`deleted_at`),
    CONSTRAINT `fk_danmaku_video_id` FOREIGN KEY (`video_id`) REFERENCES `videos`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_danmaku_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='弹幕表';

-- 5.4 视频播放记录表
-- 注意：由于user_id可能为NULL（访客播放），无法使用唯一索引约束
-- 需要在应用层确保：当user_id不为NULL时，每个用户每个视频只有一条记录
CREATE TABLE IF NOT EXISTS `video_play_records` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `video_id` BIGINT NOT NULL COMMENT '视频ID',
    `user_id` BIGINT NULL COMMENT '用户ID（可为NULL，访客播放）',
    `play_progress` INT NOT NULL DEFAULT 0 COMMENT '播放进度（秒）',
    `play_duration` INT NOT NULL DEFAULT 0 COMMENT '播放时长（秒）',
    `is_completed` TINYINT NOT NULL DEFAULT 0 COMMENT '是否播放完成',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_video_play_records_video_id` (`video_id`),
    INDEX `idx_video_play_records_user_id` (`user_id`),
    INDEX `idx_video_play_records_video_user` (`video_id`, `user_id`),
    CONSTRAINT `fk_video_play_records_video_id` FOREIGN KEY (`video_id`) REFERENCES `videos`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_video_play_records_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频播放记录表';

-- =====================================================
-- 6. 系统模块
-- =====================================================

-- 6.1 搜索日志表（可选）
CREATE TABLE IF NOT EXISTS `search_logs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NULL COMMENT '用户ID（可为NULL，访客搜索）',
    `keyword` VARCHAR(100) NOT NULL COMMENT '搜索关键词',
    `result_count` INT NOT NULL DEFAULT 0 COMMENT '结果数量',
    `search_type` VARCHAR(20) NULL COMMENT '搜索类型：all/article/question/video/user',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_search_logs_keyword` (`keyword`),
    INDEX `idx_search_logs_user_id` (`user_id`),
    INDEX `idx_search_logs_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索日志表';

-- 6.2 系统配置表（第三阶段）
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键（唯一）',
    `config_value` TEXT NULL COMMENT '配置值',
    `config_type` VARCHAR(20) NOT NULL DEFAULT 'string' COMMENT '配置类型：string/number/boolean/json',
    `description` VARCHAR(200) NULL COMMENT '配置描述',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_system_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 6.3 管理员操作日志表（第三阶段）
CREATE TABLE IF NOT EXISTS `admin_operations` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `admin_id` BIGINT NOT NULL COMMENT '管理员ID',
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型：audit/delete/ban/unban等',
    `target_type` VARCHAR(20) NULL COMMENT '目标类型：user/article/question/answer/video',
    `target_id` BIGINT NULL COMMENT '目标ID',
    `operation_desc` VARCHAR(500) NULL COMMENT '操作描述',
    `ip_address` VARCHAR(50) NULL COMMENT 'IP地址',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_admin_operations_admin_id` (`admin_id`),
    INDEX `idx_admin_operations_target` (`target_type`, `target_id`),
    INDEX `idx_admin_operations_created_at` (`created_at`),
    CONSTRAINT `fk_admin_operations_admin_id` FOREIGN KEY (`admin_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志表';

-- =====================================================
-- 初始化完成
-- =====================================================

