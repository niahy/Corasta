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

-- =====================================================
-- 内置测试数据
-- =====================================================

-- 1. 用户数据
INSERT INTO `users` (`id`, `username`, `password`, `nickname`, `avatar`, `bio`, `status`, `role`, `created_at`) VALUES
(1, 'admin', 'admin123', '管理员', NULL, '系统管理员', 1, 1, NOW()),
(2, 'alice', 'alice123', '星屿', 'https://via.placeholder.com/150', '热爱写作的文艺青年，喜欢记录生活中的美好瞬间', 1, 0, NOW()),
(3, 'bob', 'bob123', 'Limer', 'https://via.placeholder.com/150', '技术爱好者，专注于前端开发', 1, 0, NOW()),
(4, 'charlie', 'charlie123', '墨染', 'https://via.placeholder.com/150', '摄影爱好者，用镜头捕捉世界', 1, 0, NOW()),
(5, 'diana', 'diana123', '清风', 'https://via.placeholder.com/150', '喜欢阅读和分享，追求内心的平静', 1, 0, NOW());

-- 2. 用户隐私设置
INSERT INTO `user_privacy_settings` (`user_id`, `homepage_visible`, `content_visible`) VALUES
(1, 1, 1),
(2, 1, 1),
(3, 1, 1),
(4, 1, 1),
(5, 1, 1);

-- 3. 分类数据
INSERT INTO `categories` (`id`, `name`, `description`, `sort_order`) VALUES
(1, '文学', '文学创作与阅读分享', 1),
(2, '技术', '编程技术与开发经验', 2),
(3, '生活', '日常生活与感悟', 3),
(4, '摄影', '摄影作品与技巧分享', 4),
(5, '旅行', '旅行见闻与攻略', 5);

-- 4. 标签数据
INSERT INTO `tags` (`id`, `name`, `description`, `use_count`) VALUES
(1, 'Vue', 'Vue.js 前端框架', 5),
(2, 'Spring Boot', 'Spring Boot 后端框架', 3),
(3, '散文', '散文创作', 8),
(4, '摄影技巧', '摄影技巧分享', 4),
(5, '旅行攻略', '旅行攻略分享', 6),
(6, '生活感悟', '生活感悟与思考', 10),
(7, '前端开发', '前端开发技术', 7),
(8, '后端开发', '后端开发技术', 5);

-- 5. 文章数据
INSERT INTO `articles` (`id`, `user_id`, `title`, `content`, `summary`, `cover_image`, `category_id`, `status`, `view_count`, `like_count`, `comment_count`, `favorite_count`, `published_at`, `created_at`) VALUES
(1, 2, '如何写出带薄荷味的夏夜？', 
'## 夏夜的记忆

那个夏天，我坐在阳台上，看着夜空中的星星。微风轻拂，带来一丝凉意。

### 薄荷的味道

薄荷的清香，总是让我想起那个夏夜。那种清凉的感觉，仿佛能穿透时光，回到那个美好的夜晚。

**写作的灵感**

写作，就是要把这种感觉记录下来。用文字，把那些美好的瞬间，永远地保存下来。', 
'用文字记录夏夜的美好，感受薄荷般的清凉', 
'https://via.placeholder.com/800x400', 1, 1, 2300, 156, 23, 89, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),

(2, 3, '粉色玻璃：青春故事里的五种光', 
'## 青春的色彩

青春，就像粉色玻璃一样，透明而美丽。透过它，我们看到了五种不同的光。

### 第一种光：希望

希望之光，总是那么明亮。它指引着我们前进的方向。

### 第二种光：梦想

梦想之光，虽然遥远，但总是那么吸引人。

### 第三种光：友情

友情之光，温暖而持久。

### 第四种光：爱情

爱情之光，绚烂而短暂。

### 第五种光：成长

成长之光，虽然痛苦，但最终会让我们变得更强大。', 
'透过粉色玻璃，看青春的五种光', 
'https://via.placeholder.com/800x400', 1, 1, 4600, 289, 45, 156, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),

(3, 3, 'Vue 3 Composition API 实战指南', 
'## 什么是 Composition API？

Composition API 是 Vue 3 引入的新特性，它让我们能够更好地组织组件逻辑。

### 基本用法

```javascript
import { ref, computed } from ''vue''

export default {
  setup() {
    const count = ref(0)
    const doubleCount = computed(() => count.value * 2)
    
    return {
      count,
      doubleCount
    }
  }
}
```

### 优势

1. 更好的逻辑复用
2. 更清晰的代码组织
3. 更好的 TypeScript 支持', 
'深入理解 Vue 3 Composition API 的使用方法和优势', 
'https://via.placeholder.com/800x400', 2, 1, 3200, 198, 34, 112, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),

(4, 4, '城市夜景摄影技巧分享', 
'## 夜景摄影的魅力

城市夜景，总是那么迷人。如何在夜晚捕捉到最美的瞬间？

### 设备准备

1. 三脚架：必不可少
2. 快门线：减少震动
3. 广角镜头：捕捉更多画面

### 拍摄技巧

1. 使用小光圈（f/8-f/16）
2. 低 ISO（100-400）
3. 长曝光（5-30秒）

### 后期处理

适当的后期处理，能让照片更加出彩。', 
'分享城市夜景摄影的技巧和经验', 
'https://via.placeholder.com/800x400', 4, 1, 1800, 124, 18, 67, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),

(5, 5, '生活中的小确幸', 
'## 什么是小确幸？

小确幸，就是生活中那些微小而确实的幸福。

### 早晨的阳光

每天早晨，当阳光透过窗户洒进来，那种温暖的感觉，就是小确幸。

### 一杯热茶

在寒冷的冬天，一杯热茶，能带来满满的幸福感。

### 朋友的问候

朋友的问候，虽然简单，但总是那么温暖。

**珍惜当下**

让我们珍惜生活中的每一个小确幸。', 
'记录生活中的小确幸，感受生活的美好', 
'https://via.placeholder.com/800x400', 3, 1, 1500, 98, 15, 45, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY);

-- 6. 文章标签关联
INSERT INTO `article_tags` (`article_id`, `tag_id`) VALUES
(1, 3), (1, 6),
(2, 3), (2, 6),
(3, 1), (3, 7),
(4, 4),
(5, 6);

-- 7. 问题数据
INSERT INTO `questions` (`id`, `user_id`, `title`, `description`, `view_count`, `answer_count`, `follow_count`, `status`, `created_at`) VALUES
(1, 3, 'Vue 3 和 Vue 2 的主要区别是什么？', 
'最近在学习 Vue 3，想了解一下 Vue 3 和 Vue 2 的主要区别。特别是 Composition API 的使用场景。', 
1200, 3, 15, 1, NOW() - INTERVAL 4 DAY),

(2, 4, '如何拍摄出有质感的夜景照片？', 
'我刚开始学习摄影，想拍出有质感的夜景照片。请问有什么技巧和注意事项？', 
800, 2, 8, 1, NOW() - INTERVAL 2 DAY),

(3, 5, '如何保持生活的热情？', 
'最近感觉生活有些乏味，想知道如何保持对生活的热情和好奇心。', 
600, 1, 5, 1, NOW() - INTERVAL 1 DAY);

-- 8. 问题标签关联
INSERT INTO `question_tags` (`question_id`, `tag_id`) VALUES
(1, 1), (1, 7),
(2, 4),
(3, 6);

-- 9. 回答数据
INSERT INTO `answers` (`id`, `question_id`, `user_id`, `content`, `upvote_count`, `downvote_count`, `comment_count`, `is_best`, `status`, `created_at`) VALUES
(1, 1, 2, 
'Vue 3 相比 Vue 2 的主要改进包括：

1. **性能提升**：更快的渲染速度和更小的包体积
2. **Composition API**：更好的逻辑复用和代码组织
3. **TypeScript 支持**：更好的类型推断
4. **新的响应式系统**：基于 Proxy 的实现

Composition API 特别适合：
- 大型组件
- 需要逻辑复用的场景
- TypeScript 项目', 
45, 2, 5, 1, 1, NOW() - INTERVAL 3 DAY),

(2, 1, 3, 
'补充一点：Vue 3 还引入了 Teleport、Suspense 等新特性，这些在 Vue 2 中是没有的。', 
12, 0, 1, 0, 1, NOW() - INTERVAL 2 DAY),

(3, 2, 4, 
'拍摄夜景的关键点：

1. **稳定设备**：使用三脚架
2. **正确曝光**：使用手动模式，控制光圈和快门
3. **低 ISO**：保持画面清晰
4. **构图**：注意前景和背景的搭配', 
28, 1, 3, 1, 1, NOW() - INTERVAL 1 DAY),

(4, 3, 5, 
'保持生活热情的方法：

1. 培养新的兴趣爱好
2. 定期给自己设定小目标
3. 多和朋友交流
4. 记录生活中的美好瞬间', 
15, 0, 2, 0, 1, NOW() - INTERVAL 1 DAY);

-- 10. 更新问题的最佳回答
UPDATE `questions` SET `best_answer_id` = 1 WHERE `id` = 1;
UPDATE `questions` SET `best_answer_id` = 3 WHERE `id` = 2;

-- 11. 评论数据
INSERT INTO `comments` (`id`, `target_type`, `target_id`, `user_id`, `parent_id`, `content`, `like_count`, `reply_count`, `status`, `created_at`) VALUES
(1, 'article', 1, 3, NULL, '写得真好，让我想起了自己的夏夜记忆', 12, 2, 1, NOW() - INTERVAL 4 DAY),
(2, 'article', 1, 4, 1, '同感！薄荷的味道确实很特别', 5, 0, 1, NOW() - INTERVAL 3 DAY),
(3, 'article', 2, 2, NULL, '青春的五种光，写得太美了', 18, 1, 1, NOW() - INTERVAL 2 DAY),
(4, 'article', 3, 2, NULL, 'Composition API 确实很好用，感谢分享', 8, 0, 1, NOW() - INTERVAL 1 DAY),
(5, 'answer', 1, 4, NULL, '总结得很全面，学到了', 6, 0, 1, NOW() - INTERVAL 2 DAY);

-- 12. 点赞数据
INSERT INTO `likes` (`target_type`, `target_id`, `user_id`, `created_at`) VALUES
('article', 1, 3, NOW() - INTERVAL 4 DAY),
('article', 1, 4, NOW() - INTERVAL 3 DAY),
('article', 2, 2, NOW() - INTERVAL 2 DAY),
('article', 2, 3, NOW() - INTERVAL 2 DAY),
('article', 3, 2, NOW() - INTERVAL 1 DAY),
('article', 3, 4, NOW() - INTERVAL 1 DAY),
('answer', 1, 3, NOW() - INTERVAL 2 DAY),
('answer', 1, 4, NOW() - INTERVAL 2 DAY),
('comment', 1, 2, NOW() - INTERVAL 3 DAY),
('comment', 3, 3, NOW() - INTERVAL 1 DAY);

-- 13. 收藏夹数据
INSERT INTO `favorite_folders` (`id`, `user_id`, `name`, `description`, `sort_order`) VALUES
(1, 2, '技术文章', '收藏的技术相关文章', 1),
(2, 2, '文学创作', '喜欢的文学作品', 2),
(3, 3, '前端开发', '前端开发相关资源', 1),
(4, 4, '摄影技巧', '摄影技巧和教程', 1);

-- 14. 收藏数据
INSERT INTO `favorites` (`target_type`, `target_id`, `user_id`, `folder_id`, `created_at`) VALUES
('article', 1, 2, 2, NOW() - INTERVAL 4 DAY),
('article', 2, 2, 2, NOW() - INTERVAL 2 DAY),
('article', 3, 3, 3, NOW() - INTERVAL 1 DAY),
('article', 4, 4, 4, NOW() - INTERVAL 1 DAY);

-- 15. 关注数据
INSERT INTO `follows` (`follower_id`, `following_id`, `created_at`) VALUES
(2, 3, NOW() - INTERVAL 10 DAY),
(3, 2, NOW() - INTERVAL 8 DAY),
(4, 2, NOW() - INTERVAL 5 DAY),
(5, 2, NOW() - INTERVAL 3 DAY),
(2, 4, NOW() - INTERVAL 2 DAY);

-- 16. 问题关注数据
INSERT INTO `question_follows` (`question_id`, `user_id`, `created_at`) VALUES
(1, 2, NOW() - INTERVAL 3 DAY),
(1, 4, NOW() - INTERVAL 2 DAY),
(2, 3, NOW() - INTERVAL 1 DAY);

-- 17. 回答投票数据
INSERT INTO `answer_votes` (`answer_id`, `user_id`, `vote_type`, `created_at`) VALUES
(1, 3, 1, NOW() - INTERVAL 2 DAY),
(1, 4, 1, NOW() - INTERVAL 2 DAY),
(1, 5, 1, NOW() - INTERVAL 1 DAY),
(2, 2, 1, NOW() - INTERVAL 1 DAY),
(3, 2, 1, NOW() - INTERVAL 1 DAY),
(3, 3, 1, NOW() - INTERVAL 1 DAY);

-- 18. 通知数据
INSERT INTO `notifications` (`user_id`, `type`, `title`, `content`, `target_type`, `target_id`, `sender_id`, `is_read`, `created_at`) VALUES
(2, 'comment', '新评论', 'alice 评论了你的文章《如何写出带薄荷味的夏夜？》', 'article', 1, 3, 0, NOW() - INTERVAL 4 DAY),
(2, 'like', '新点赞', 'bob 点赞了你的文章《如何写出带薄荷味的夏夜？》', 'article', 1, 3, 0, NOW() - INTERVAL 3 DAY),
(3, 'comment', '新评论', 'alice 评论了你的文章《粉色玻璃：青春故事里的五种光》', 'article', 2, 2, 1, NOW() - INTERVAL 2 DAY),
(3, 'follow', '新关注', 'charlie 关注了你', 'user', 3, 4, 0, NOW() - INTERVAL 2 DAY),
(1, 'answer', '新回答', 'alice 回答了你的问题《Vue 3 和 Vue 2 的主要区别是什么？》', 'question', 1, 2, 0, NOW() - INTERVAL 3 DAY);

-- 19. 搜索日志数据（用于测试搜索建议功能）
INSERT INTO `search_logs` (`user_id`, `keyword`, `result_count`, `search_type`, `created_at`) VALUES
(2, 'Vue', 5, 'all', NOW() - INTERVAL 10 DAY),
(3, 'Vue 3', 3, 'article', NOW() - INTERVAL 8 DAY),
(2, '摄影', 4, 'all', NOW() - INTERVAL 7 DAY),
(4, '摄影技巧', 2, 'article', NOW() - INTERVAL 6 DAY),
(3, '前端开发', 7, 'all', NOW() - INTERVAL 5 DAY),
(2, '生活', 6, 'article', NOW() - INTERVAL 4 DAY),
(5, '生活感悟', 3, 'article', NOW() - INTERVAL 3 DAY),
(2, 'Vue', 5, 'all', NOW() - INTERVAL 2 DAY),
(3, 'Vue 3', 3, 'article', NOW() - INTERVAL 1 DAY),
(4, '摄影', 4, 'all', NOW() - INTERVAL 1 DAY);

-- =====================================================
-- 测试数据初始化完成
-- =====================================================
