# API 接口文档

**项目名称：** 次世代多用户内容社区平台 / Corasta  
**API 版本：** V1.0  
**基础路径：** `/api/v1`  
**最后更新：** 2024

---

## 目录

1. [API 设计规范](#1-api-设计规范)
2. [通用说明](#2-通用说明)
3. [第一阶段：核心功能 API](#3-第一阶段核心功能-api)
4. [第二阶段：视频功能 API](#4-第二阶段视频功能-api)
5. [第三阶段：管理员功能 API](#5-第三阶段管理员功能-api)

---

## 1. API 设计规范

### 1.1 RESTful 设计原则

- **资源导向：** 使用名词表示资源，动词通过 HTTP 方法表示
- **HTTP 方法：**
  - `GET` - 查询资源
  - `POST` - 创建资源
  - `PUT` - 完整更新资源
  - `PATCH` - 部分更新资源
  - `DELETE` - 删除资源
- **URL 规范：**
  - 使用小写字母和连字符（kebab-case）
  - 使用复数形式表示资源集合（如 `/articles`）
  - 使用嵌套资源表示关联关系（如 `/articles/{id}/comments`）

### 1.2 统一响应格式

#### 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 响应数据
  },
  "timestamp": "2024-01-01T00:00:00"
}
```

#### 分页响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "items": [],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 100,
      "totalPages": 5
    }
  },
  "timestamp": "2024-01-01T00:00:00"
}
```

#### 错误响应

```json
{
  "code": 400,
  "message": "错误描述",
  "error": "详细错误信息（可选）",
  "timestamp": "2024-01-01T00:00:00"
}
```

### 1.3 HTTP 状态码

- `200` - 请求成功
- `201` - 资源创建成功
- `204` - 请求成功，无返回内容
- `400` - 请求参数错误
- `401` - 未授权（未登录或 Token 无效）
- `403` - 禁止访问（无权限）
- `404` - 资源不存在
- `409` - 资源冲突（如用户名已存在）
- `422` - 请求参数验证失败
- `500` - 服务器内部错误

### 1.4 认证方式

使用 JWT Token 进行身份认证：

```
Authorization: Bearer {token}
```

Token 通过登录接口获取，包含用户 ID、用户名等信息。

---

## 2. 通用说明

### 2.1 请求头

```
Content-Type: application/json
Authorization: Bearer {token}  // 需要认证的接口
```

### 2.2 分页参数

所有列表接口支持以下分页参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 20 | 每页数量（最大 100） |

### 2.3 时间格式

所有时间字段使用 ISO 8601 格式：`YYYY-MM-DDTHH:mm:ss`

### 2.4 文件上传

文件上传接口使用 `multipart/form-data` 格式，支持的文件类型和大小限制见具体接口说明。

---

## 3. 第一阶段：核心功能 API

### 3.1 用户中心模块

#### 3.1.1 用户注册

**接口：** `POST /auth/register`

**说明：** 用户注册接口

**请求体：**

```json
{
  "username": "string",        // 必填，4-20字符，字母数字下划线
  "password": "string",        // 必填，8-32字符，包含字母和数字
  "confirmPassword": "string", // 必填，需与password一致
  "captcha": "string",         // 必填，图片验证码
  "captchaKey": "string",      // 必填，验证码key
  "securityQuestions": [       // 可选，最多3个
    {
      "question": "string",
      "answer": "string"
    }
  ]
}
```

**响应：**

```json
{
  "code": 201,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "username": "testuser"
  }
}
```

**错误码：**
- `400` - 参数错误
- `409` - 用户名已存在
- `422` - 验证码错误或密码格式不正确

---

#### 3.1.2 获取验证码

**接口：** `GET /auth/captcha`

**说明：** 获取图片验证码

**响应：**

```json
{
  "code": 200,
  "data": {
    "key": "captcha_key_123",
    "image": "data:image/png;base64,..."
  }
}
```

---

#### 3.1.3 用户登录

**接口：** `POST /auth/login`

**说明：** 用户登录接口

**请求体：**

```json
{
  "username": "string",
  "password": "string",
  "captcha": "string",
  "captchaKey": "string",
  "rememberMe": false  // 可选，7天免登录
}
```

**响应：**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "jwt_token_string",
    "user": {
      "id": 1,
      "username": "testuser",
      "nickname": "测试用户",
      "avatar": "https://example.com/avatar.jpg"
    }
  }
}
```

---

#### 3.1.4 用户登出

**接口：** `POST /auth/logout`

**说明：** 用户登出接口

**请求头：** 需要认证

**响应：**

```json
{
  "code": 200,
  "message": "登出成功"
}
```

---

#### 3.1.5 修改密码

**接口：** `PUT /auth/password`

**说明：** 修改当前用户密码

**请求头：** 需要认证

**请求体：**

```json
{
  "oldPassword": "string",
  "newPassword": "string",
  "confirmPassword": "string"
}
```

**响应：**

```json
{
  "code": 200,
  "message": "密码修改成功"
}
```

---

#### 3.1.6 找回密码

**接口：** `POST /auth/password/reset`

**说明：** 通过密保问题找回密码

**请求体：**

```json
{
  "username": "string",
  "answers": [  // 密保问题答案，需全部答对
    {
      "questionId": 1,
      "answer": "string"
    }
  ],
  "newPassword": "string",
  "confirmPassword": "string"
}
```

**响应：**

```json
{
  "code": 200,
  "message": "密码重置成功"
}
```

---

#### 3.1.7 获取密保问题

**接口：** `GET /auth/security-questions/{username}`

**说明：** 获取指定用户的密保问题（用于找回密码）

**路径参数：**
- `username` - 用户名

**响应：**

```json
{
  "code": 200,
  "data": {
    "questions": [
      {
        "id": 1,
        "question": "你的小学名称是什么？",
        "sortOrder": 1
      }
    ]
  }
}
```

---

#### 3.1.8 获取当前用户信息

**接口：** `GET /users/me`

**说明：** 获取当前登录用户信息

**请求头：** 需要认证

**响应：**

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "avatar": "https://example.com/avatar.jpg",
    "bio": "个人简介",
    "followerCount": 10,
    "followingCount": 5,
    "likeCount": 100,
    "articleCount": 5,
    "questionCount": 3,
    "answerCount": 8
  }
}
```

---

#### 3.1.9 更新个人资料

**接口：** `PUT /users/me`

**说明：** 更新当前用户个人资料

**请求头：** 需要认证

**请求体：**

```json
{
  "nickname": "string",  // 1-20字符
  "bio": "string",       // 0-200字符，支持Markdown
  "avatar": "string"     // 头像URL（通过文件上传接口获取）
}
```

**响应：**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "nickname": "新昵称",
    "bio": "新简介",
    "avatar": "https://example.com/new-avatar.jpg"
  }
}
```

---

#### 3.1.10 上传头像

**接口：** `POST /users/me/avatar`

**说明：** 上传用户头像

**请求头：** 需要认证，`Content-Type: multipart/form-data`

**请求体：**
- `file` - 图片文件（JPG、PNG，最大 2MB）

**响应：**

```json
{
  "code": 200,
  "data": {
    "url": "https://example.com/avatars/user_1.jpg"
  }
}
```

---

#### 3.1.11 获取用户主页信息

**接口：** `GET /users/{userId}`

**说明：** 获取指定用户的主页信息

**路径参数：**
- `userId` - 用户ID

**查询参数：**
- `tab` - 内容类型筛选（可选）：`articles`、`questions`、`answers`、`all`

**响应：**

```json
{
  "code": 200,
  "data": {
    "user": {
      "id": 1,
      "username": "testuser",
      "nickname": "测试用户",
      "avatar": "https://example.com/avatar.jpg",
      "bio": "个人简介",
      "followerCount": 10,
      "followingCount": 5,
      "likeCount": 100
    },
    "isFollowing": false,  // 当前用户是否关注了该用户
    "content": {
      "articles": [],
      "questions": [],
      "answers": []
    }
  }
}
```

---

#### 3.1.12 更新隐私设置

**接口：** `PUT /users/me/privacy`

**说明：** 更新用户隐私设置

**请求头：** 需要认证

**请求体：**

```json
{
  "homepageVisible": 1,  // 0-仅关注者，1-公开
  "contentVisible": 1    // 0-仅关注者，1-公开
}
```

**响应：**

```json
{
  "code": 200,
  "message": "更新成功"
}
```

---

### 3.2 文章发布与管理模块

#### 3.2.1 创建文章

**接口：** `POST /articles`

**说明：** 创建新文章（草稿或直接发布）

**请求头：** 需要认证

**请求体：**

```json
{
  "title": "string",           // 必填，1-100字符
  "content": "string",          // 必填，Markdown格式
  "summary": "string",          // 可选，0-200字符
  "coverImage": "string",       // 可选，封面图URL
  "slug": "string",             // 可选，自定义URL（SEO友好）
  "categoryId": 1,              // 可选，分类ID
  "tags": ["tag1", "tag2"],     // 可选，标签数组（最多10个）
  "status": 0                   // 0-草稿，1-已发布，2-私密
}
```

**响应：**

```json
{
  "code": 201,
  "message": "创建成功",
  "data": {
    "id": 1,
    "title": "文章标题",
    "slug": "article-slug",
    "status": 1,
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

---

#### 3.2.2 更新文章

**接口：** `PUT /articles/{id}`

**说明：** 更新文章（只能更新自己的文章）

**请求头：** 需要认证

**路径参数：**
- `id` - 文章ID

**请求体：** 同创建文章

**响应：**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "title": "更新后的标题"
  }
}
```

---

#### 3.2.3 删除文章

**接口：** `DELETE /articles/{id}`

**说明：** 删除文章（只能删除自己的文章）

**请求头：** 需要认证

**路径参数：**
- `id` - 文章ID

**响应：**

```json
{
  "code": 200,
  "message": "删除成功"
}
```

---

#### 3.2.4 批量删除文章

**接口：** `DELETE /articles/batch`

**说明：** 批量删除文章（只能删除自己的文章）

**请求头：** 需要认证

**请求体：**

```json
{
  "ids": [1, 2, 3]
}
```

**响应：**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": {
    "deletedCount": 3
  }
}
```

---

#### 3.2.5 获取文章详情

**接口：** `GET /articles/{id}`

**说明：** 获取文章详情（增加阅读量）

**路径参数：**
- `id` - 文章ID或slug

**响应：**

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "title": "文章标题",
    "content": "文章内容（Markdown）",
    "summary": "文章摘要",
    "coverImage": "https://example.com/cover.jpg",
    "slug": "article-slug",
    "author": {
      "id": 1,
      "username": "testuser",
      "nickname": "测试用户",
      "avatar": "https://example.com/avatar.jpg"
    },
    "category": {
      "id": 1,
      "name": "技术"
    },
    "tags": [
      {"id": 1, "name": "Java"},
      {"id": 2, "name": "Spring Boot"}
    ],
    "status": 1,
    "viewCount": 100,
    "likeCount": 10,
    "commentCount": 5,
    "favoriteCount": 3,
    "isLiked": false,      // 当前用户是否点赞
    "isFavorited": false,  // 当前用户是否收藏
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
}
```

---

#### 3.2.6 获取文章列表

**接口：** `GET /articles`

**说明：** 获取文章列表

**查询参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| page | Integer | 页码 |
| pageSize | Integer | 每页数量 |
| status | Integer | 状态筛选（0-草稿，1-已发布，2-私密） |
| categoryId | Long | 分类ID筛选 |
| tagId | Long | 标签ID筛选 |
| userId | Long | 作者ID筛选 |
| keyword | String | 关键词搜索（标题、内容） |
| sort | String | 排序方式：`latest`（最新）、`popular`（热门）、`views`（阅读量） |

**响应：**

```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "title": "文章标题",
        "summary": "文章摘要",
        "coverImage": "https://example.com/cover.jpg",
        "author": {
          "id": 1,
          "nickname": "测试用户",
          "avatar": "https://example.com/avatar.jpg"
        },
        "category": {"id": 1, "name": "技术"},
        "tags": [{"id": 1, "name": "Java"}],
        "viewCount": 100,
        "likeCount": 10,
        "commentCount": 5,
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 100,
      "totalPages": 5
    }
  }
}
```

---

#### 3.2.7 获取我的文章列表

**接口：** `GET /articles/me`

**说明：** 获取当前用户自己的文章列表（用于管理后台）

**请求头：** 需要认证

**查询参数：** 同获取文章列表

**响应：** 同获取文章列表

---

#### 3.2.8 上传文章图片

**接口：** `POST /articles/images`

**说明：** 上传文章中的图片

**请求头：** 需要认证，`Content-Type: multipart/form-data`

**请求体：**
- `file` - 图片文件（JPG、PNG、GIF，最大 5MB）

**响应：**

```json
{
  "code": 200,
  "data": {
    "url": "https://example.com/images/article_123.jpg"
  }
}
```

---

### 3.3 问答系统模块

#### 3.3.1 创建问题

**接口：** `POST /questions`

**说明：** 创建新问题

**请求头：** 需要认证

**请求体：**

```json
{
  "title": "string",           // 必填，1-100字符
  "description": "string",      // 必填，支持Markdown，0-5000字符
  "tags": ["tag1", "tag2"],     // 可选，标签数组（最多5个）
  "relatedArticleId": 1        // 可选，关联文章ID
}
```

**响应：**

```json
{
  "code": 201,
  "message": "创建成功",
  "data": {
    "id": 1,
    "title": "问题标题",
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

---

#### 3.3.2 更新问题

**接口：** `PUT /questions/{id}`

**说明：** 更新问题（只能更新自己的问题）

**请求头：** 需要认证

**路径参数：**
- `id` - 问题ID

**请求体：** 同创建问题

**响应：**

```json
{
  "code": 200,
  "message": "更新成功"
}
```

---

#### 3.3.3 删除问题

**接口：** `DELETE /questions/{id}`

**说明：** 删除问题（只能删除自己的问题）

**请求头：** 需要认证

**路径参数：**
- `id` - 问题ID

**响应：**

```json
{
  "code": 200,
  "message": "删除成功"
}
```

---

#### 3.3.4 获取问题详情

**接口：** `GET /questions/{id}`

**说明：** 获取问题详情

**路径参数：**
- `id` - 问题ID

**响应：**

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "title": "问题标题",
    "description": "问题描述（Markdown）",
    "author": {
      "id": 1,
      "nickname": "提问者",
      "avatar": "https://example.com/avatar.jpg"
    },
    "tags": [{"id": 1, "name": "Java"}],
    "relatedArticle": {
      "id": 1,
      "title": "相关文章标题"
    },
    "answerCount": 5,
    "followerCount": 10,
    "isFollowing": false,  // 当前用户是否关注了该问题
    "bestAnswerId": 3,      // 最佳回答ID
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

---

#### 3.3.5 获取问题列表

**接口：** `GET /questions`

**说明：** 获取问题列表

**查询参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| page | Integer | 页码 |
| pageSize | Integer | 每页数量 |
| tagId | Long | 标签ID筛选 |
| userId | Long | 提问者ID筛选 |
| keyword | String | 关键词搜索 |
| sort | String | 排序方式：`latest`（最新）、`popular`（热门）、`answers`（回答数） |

**响应：**

```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "title": "问题标题",
        "description": "问题描述摘要...",
        "author": {
          "id": 1,
          "nickname": "提问者"
        },
        "tags": [{"id": 1, "name": "Java"}],
        "answerCount": 5,
        "followerCount": 10,
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 100,
      "totalPages": 5
    }
  }
}
```

---

#### 3.3.6 创建回答

**接口：** `POST /questions/{questionId}/answers`

**说明：** 对问题创建回答

**请求头：** 需要认证

**路径参数：**
- `questionId` - 问题ID

**请求体：**

```json
{
  "content": "string"  // 必填，支持Markdown，0-10000字符
}
```

**响应：**

```json
{
  "code": 201,
  "message": "回答成功",
  "data": {
    "id": 1,
    "content": "回答内容",
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

---

#### 3.3.7 更新回答

**接口：** `PUT /answers/{id}`

**说明：** 更新回答（只能更新自己的回答）

**请求头：** 需要认证

**路径参数：**
- `id` - 回答ID

**请求体：**

```json
{
  "content": "string"
}
```

**响应：**

```json
{
  "code": 200,
  "message": "更新成功"
}
```

---

#### 3.3.8 删除回答

**接口：** `DELETE /answers/{id}`

**说明：** 删除回答（只能删除自己的回答）

**请求头：** 需要认证

**路径参数：**
- `id` - 回答ID

**响应：**

```json
{
  "code": 200,
  "message": "删除成功"
}
```

---

#### 3.3.9 获取回答列表

**接口：** `GET /questions/{questionId}/answers`

**说明：** 获取问题的回答列表

**路径参数：**
- `questionId` - 问题ID

**查询参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| page | Integer | 页码 |
| pageSize | Integer | 每页数量 |
| sort | String | 排序方式：`votes`（赞同数）、`latest`（最新）、`oldest`（最早）、`hot`（热度） |

**响应：**

```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "content": "回答内容（Markdown）",
        "author": {
          "id": 1,
          "nickname": "回答者",
          "avatar": "https://example.com/avatar.jpg"
        },
        "upvoteCount": 10,
        "downvoteCount": 1,
        "commentCount": 3,
        "isBest": false,        // 是否为最佳回答
        "isUpvoted": false,     // 当前用户是否赞同
        "isDownvoted": false,   // 当前用户是否反对
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 50,
      "totalPages": 3
    }
  }
}
```

---

#### 3.3.10 投票回答

**接口：** `POST /answers/{id}/vote`

**说明：** 对回答进行投票（赞同或反对）

**请求头：** 需要认证

**路径参数：**
- `id` - 回答ID

**请求体：**

```json
{
  "type": "upvote"  // "upvote" 或 "downvote"
}
```

**响应：**

```json
{
  "code": 200,
  "message": "投票成功",
  "data": {
    "upvoteCount": 11,
    "downvoteCount": 1,
    "isUpvoted": true,
    "isDownvoted": false
  }
}
```

---

#### 3.3.11 取消投票

**接口：** `DELETE /answers/{id}/vote`

**说明：** 取消对回答的投票

**请求头：** 需要认证

**路径参数：**
- `id` - 回答ID

**响应：**

```json
{
  "code": 200,
  "message": "取消投票成功"
}
```

---

#### 3.3.12 标记最佳回答

**接口：** `PUT /questions/{questionId}/best-answer/{answerId}`

**说明：** 标记问题的最佳回答（只能由提问者或关联文章作者操作）

**请求头：** 需要认证

**路径参数：**
- `questionId` - 问题ID
- `answerId` - 回答ID

**响应：**

```json
{
  "code": 200,
  "message": "标记成功"
}
```

---

#### 3.3.13 关注问题

**接口：** `POST /questions/{id}/follow`

**说明：** 关注问题

**请求头：** 需要认证

**路径参数：**
- `id` - 问题ID

**响应：**

```json
{
  "code": 200,
  "message": "关注成功"
}
```

---

#### 3.3.14 取消关注问题

**接口：** `DELETE /questions/{id}/follow`

**说明：** 取消关注问题

**请求头：** 需要认证

**路径参数：**
- `id` - 问题ID

**响应：**

```json
{
  "code": 200,
  "message": "取消关注成功"
}
```

---

### 3.4 内容互动模块

#### 3.4.1 创建评论

**接口：** `POST /comments`

**说明：** 创建评论（可评论文章、回答）

**请求头：** 需要认证

**请求体：**

```json
{
  "targetType": "article",  // "article" 或 "answer"
  "targetId": 1,            // 目标ID
  "content": "string",      // 必填，支持Markdown，0-1000字符
  "parentId": null          // 可选，父评论ID（二级评论）
}
```

**响应：**

```json
{
  "code": 201,
  "message": "评论成功",
  "data": {
    "id": 1,
    "content": "评论内容",
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

---

#### 3.4.2 获取评论列表

**接口：** `GET /comments`

**说明：** 获取评论列表

**查询参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| targetType | String | 目标类型：`article`、`answer` |
| targetId | Long | 目标ID |
| page | Integer | 页码 |
| pageSize | Integer | 每页数量 |
| sort | String | 排序方式：`latest`（最新）、`oldest`（最早）、`hot`（热度） |

**响应：**

```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "content": "评论内容（Markdown）",
        "author": {
          "id": 1,
          "nickname": "评论者",
          "avatar": "https://example.com/avatar.jpg"
        },
        "likeCount": 5,
        "replyCount": 2,
        "isLiked": false,
        "isPinned": false,      // 是否置顶
        "parentId": null,       // 父评论ID（二级评论）
        "replies": [],          // 子评论列表
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 50,
      "totalPages": 3
    }
  }
}
```

---

#### 3.4.3 更新评论

**接口：** `PUT /comments/{id}`

**说明：** 更新评论（只能更新自己的评论）

**请求头：** 需要认证

**路径参数：**
- `id` - 评论ID

**请求体：**

```json
{
  "content": "string"
}
```

**响应：**

```json
{
  "code": 200,
  "message": "更新成功"
}
```

---

#### 3.4.4 删除评论

**接口：** `DELETE /comments/{id}`

**说明：** 删除评论（只能删除自己的评论，或内容作者可删除自己内容下的评论）

**请求头：** 需要认证

**路径参数：**
- `id` - 评论ID

**响应：**

```json
{
  "code": 200,
  "message": "删除成功"
}
```

---

#### 3.4.5 点赞评论

**接口：** `POST /comments/{id}/like`

**说明：** 点赞评论

**请求头：** 需要认证

**路径参数：**
- `id` - 评论ID

**响应：**

```json
{
  "code": 200,
  "message": "点赞成功",
  "data": {
    "likeCount": 6,
    "isLiked": true
  }
}
```

---

#### 3.4.6 取消点赞评论

**接口：** `DELETE /comments/{id}/like`

**说明：** 取消点赞评论

**请求头：** 需要认证

**路径参数：**
- `id` - 评论ID

**响应：**

```json
{
  "code": 200,
  "message": "取消点赞成功"
}
```

---

#### 3.4.7 置顶评论

**接口：** `PUT /comments/{id}/pin`

**说明：** 置顶评论（只能由内容作者操作，仅一条）

**请求头：** 需要认证

**路径参数：**
- `id` - 评论ID

**响应：**

```json
{
  "code": 200,
  "message": "置顶成功"
}
```

---

#### 3.4.8 点赞内容

**接口：** `POST /likes`

**说明：** 点赞内容（文章、回答、视频）

**请求头：** 需要认证

**请求体：**

```json
{
  "targetType": "article",  // "article"、"answer"、"video"
  "targetId": 1
}
```

**响应：**

```json
{
  "code": 200,
  "message": "点赞成功",
  "data": {
    "likeCount": 11,
    "isLiked": true
  }
}
```

---

#### 3.4.9 取消点赞

**接口：** `DELETE /likes`

**说明：** 取消点赞

**请求头：** 需要认证

**查询参数：**
- `targetType` - 目标类型
- `targetId` - 目标ID

**响应：**

```json
{
  "code": 200,
  "message": "取消点赞成功"
}
```

---

#### 3.4.10 创建收藏夹

**接口：** `POST /favorites/folders`

**说明：** 创建收藏夹

**请求头：** 需要认证

**请求体：**

```json
{
  "name": "string",        // 必填，1-50字符
  "description": "string" // 可选，0-200字符
}
```

**响应：**

```json
{
  "code": 201,
  "message": "创建成功",
  "data": {
    "id": 1,
    "name": "我的收藏夹",
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

---

#### 3.4.11 获取收藏夹列表

**接口：** `GET /favorites/folders`

**说明：** 获取当前用户的收藏夹列表

**请求头：** 需要认证

**响应：**

```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "name": "我的收藏夹",
        "description": "收藏夹描述",
        "itemCount": 10,
        "createdAt": "2024-01-01T00:00:00"
      }
    ]
  }
}
```

---

#### 3.4.12 收藏内容

**接口：** `POST /favorites`

**说明：** 收藏内容（文章、回答）

**请求头：** 需要认证

**请求体：**

```json
{
  "targetType": "article",  // "article" 或 "answer"
  "targetId": 1,
  "folderId": 1             // 收藏夹ID
}
```

**响应：**

```json
{
  "code": 200,
  "message": "收藏成功"
}
```

---

#### 3.4.13 取消收藏

**接口：** `DELETE /favorites`

**说明：** 取消收藏

**请求头：** 需要认证

**查询参数：**
- `targetType` - 目标类型
- `targetId` - 目标ID

**响应：**

```json
{
  "code": 200,
  "message": "取消收藏成功"
}
```

---

#### 3.4.14 获取收藏列表

**接口：** `GET /favorites`

**说明：** 获取当前用户的收藏列表

**请求头：** 需要认证

**查询参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| folderId | Long | 收藏夹ID筛选 |
| targetType | String | 内容类型筛选 |
| page | Integer | 页码 |
| pageSize | Integer | 每页数量 |

**响应：**

```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "targetType": "article",
        "target": {
          "id": 1,
          "title": "文章标题",
          "author": {"nickname": "作者"},
          "createdAt": "2024-01-01T00:00:00"
        },
        "folder": {
          "id": 1,
          "name": "我的收藏夹"
        },
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 50,
      "totalPages": 3
    }
  }
}
```

---

#### 3.4.15 分享内容

**接口：** `POST /shares`

**说明：** 分享内容（记录分享次数）

**请求体：**

```json
{
  "targetType": "article",  // "article"、"answer"、"question"、"video"
  "targetId": 1,
  "platform": "wechat"     // 可选，分享平台
}
```

**响应：**

```json
{
  "code": 200,
  "message": "分享成功",
  "data": {
    "shareUrl": "https://example.com/share/abc123",
    "shareCount": 10
  }
}
```

---

### 3.5 社区与社交模块

#### 3.5.1 关注用户

**接口：** `POST /users/{userId}/follow`

**说明：** 关注用户

**请求头：** 需要认证

**路径参数：**
- `userId` - 用户ID

**响应：**

```json
{
  "code": 200,
  "message": "关注成功"
}
```

---

#### 3.5.2 取消关注

**接口：** `DELETE /users/{userId}/follow`

**说明：** 取消关注用户

**请求头：** 需要认证

**路径参数：**
- `userId` - 用户ID

**响应：**

```json
{
  "code": 200,
  "message": "取消关注成功"
}
```

---

#### 3.5.3 获取关注列表

**接口：** `GET /users/{userId}/following`

**说明：** 获取用户的关注列表

**路径参数：**
- `userId` - 用户ID

**查询参数：**
- `page` - 页码
- `pageSize` - 每页数量

**响应：**

```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "username": "user1",
        "nickname": "用户1",
        "avatar": "https://example.com/avatar.jpg",
        "bio": "个人简介",
        "followerCount": 10,
        "isFollowing": true,
        "followedAt": "2024-01-01T00:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 50,
      "totalPages": 3
    }
  }
}
```

---

#### 3.5.4 获取粉丝列表

**接口：** `GET /users/{userId}/followers`

**说明：** 获取用户的粉丝列表

**路径参数：**
- `userId` - 用户ID

**查询参数：**
- `page` - 页码
- `pageSize` - 每页数量

**响应：** 同获取关注列表

---

#### 3.5.5 获取动态Feed流

**接口：** `GET /feed`

**说明：** 获取关注的用户最新动态

**请求头：** 需要认证

**查询参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| page | Integer | 页码 |
| pageSize | Integer | 每页数量 |
| type | String | 内容类型筛选：`all`、`articles`、`questions`、`videos` |

**响应：**

```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "type": "article",
        "content": {
          "id": 1,
          "title": "文章标题",
          "summary": "文章摘要",
          "coverImage": "https://example.com/cover.jpg",
          "viewCount": 100,
          "likeCount": 10,
          "commentCount": 5
        },
        "author": {
          "id": 1,
          "nickname": "作者",
          "avatar": "https://example.com/avatar.jpg"
        },
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 100,
      "totalPages": 5
    }
  }
}
```

---

#### 3.5.6 获取通知列表

**接口：** `GET /notifications`

**说明：** 获取当前用户的通知列表

**请求头：** 需要认证

**查询参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| page | Integer | 页码 |
| pageSize | Integer | 每页数量 |
| type | String | 通知类型筛选 |
| read | Boolean | 是否已读筛选 |

**响应：**

```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "type": "comment",  // "comment"、"like"、"follow"、"answer"、"best_answer"、"invite"
        "title": "新评论",
        "content": "用户A评论了你的文章《xxx》",
        "targetType": "article",
        "targetId": 1,
        "fromUser": {
          "id": 2,
          "nickname": "用户A",
          "avatar": "https://example.com/avatar.jpg"
        },
        "isRead": false,
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 50,
      "totalPages": 3
    },
    "unreadCount": 5
  }
}
```

---

#### 3.5.7 标记通知已读

**接口：** `PUT /notifications/{id}/read`

**说明：** 标记单个通知为已读

**请求头：** 需要认证

**路径参数：**
- `id` - 通知ID

**响应：**

```json
{
  "code": 200,
  "message": "标记成功"
}
```

---

#### 3.5.8 标记全部通知已读

**接口：** `PUT /notifications/read-all`

**说明：** 标记全部通知为已读

**请求头：** 需要认证

**响应：**

```json
{
  "code": 200,
  "message": "标记成功",
  "data": {
    "markedCount": 10
  }
}
```

---

#### 3.5.9 获取未读通知数

**接口：** `GET /notifications/unread-count`

**说明：** 获取未读通知数量（用于显示小红点）

**请求头：** 需要认证

**响应：**

```json
{
  "code": 200,
  "data": {
    "count": 5
  }
}
```

---

### 3.6 搜索模块

#### 3.6.1 全站搜索

**接口：** `GET /search`

**说明：** 全站内容搜索

**查询参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| keyword | String | 必填，搜索关键词 |
| type | String | 内容类型筛选：`all`、`articles`、`questions`、`videos`、`users` |
| categoryId | Long | 分类筛选 |
| tagId | Long | 标签筛选 |
| authorId | Long | 作者筛选 |
| sort | String | 排序方式：`relevance`（相关度）、`latest`（最新）、`popular`（热门） |
| page | Integer | 页码 |
| pageSize | Integer | 每页数量 |

**响应：**

```json
{
  "code": 200,
  "data": {
    "articles": {
      "items": [],
      "total": 10
    },
    "questions": {
      "items": [],
      "total": 5
    },
    "videos": {
      "items": [],
      "total": 0
    },
    "users": {
      "items": [],
      "total": 3
    },
    "total": 18
  }
}
```

---

#### 3.6.2 搜索建议

**接口：** `GET /search/suggestions`

**说明：** 获取搜索建议（输入时调用）

**查询参数：**
- `keyword` - 关键词（部分输入）

**响应：**

```json
{
  "code": 200,
  "data": {
    "suggestions": [
      "Java",
      "Java Spring Boot",
      "Java 并发编程"
    ]
  }
}
```

---

#### 3.6.3 热门搜索词

**接口：** `GET /search/hot`

**说明：** 获取热门搜索词

**响应：**

```json
{
  "code": 200,
  "data": {
    "keywords": [
      {"keyword": "Java", "count": 100},
      {"keyword": "Spring Boot", "count": 80},
      {"keyword": "Vue", "count": 60}
    ]
  }
}
```

---

#### 3.6.4 搜索历史

**接口：** `GET /search/history`

**说明：** 获取当前用户的搜索历史（最近10条）

**请求头：** 需要认证

**响应：**

```json
{
  "code": 200,
  "data": {
    "items": [
      "Java",
      "Spring Boot",
      "Vue 3"
    ]
  }
}
```

---

#### 3.6.5 清除搜索历史

**接口：** `DELETE /search/history`

**说明：** 清除当前用户的搜索历史

**请求头：** 需要认证

**响应：**

```json
{
  "code": 200,
  "message": "清除成功"
}
```

---

### 3.7 内容管理后台模块

#### 3.7.1 获取内容管理仪表盘

**接口：** `GET /dashboard/content`

**说明：** 获取内容管理仪表盘数据

**请求头：** 需要认证

**响应：**

```json
{
  "code": 200,
  "data": {
    "statistics": {
      "articleCount": 10,
      "questionCount": 5,
      "answerCount": 8,
      "totalViewCount": 1000,
      "totalLikeCount": 100,
      "totalCommentCount": 50
    },
    "recentArticles": [],
    "recentQuestions": [],
    "recentAnswers": []
  }
}
```

---

#### 3.7.2 获取我的互动记录

**接口：** `GET /dashboard/interactions`

**说明：** 获取当前用户的互动记录（点赞、评论、回答、收藏）

**请求头：** 需要认证

**查询参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| type | String | 类型筛选：`likes`、`comments`、`answers`、`favorites` |
| page | Integer | 页码 |
| pageSize | Integer | 每页数量 |

**响应：**

```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "type": "like",
        "targetType": "article",
        "target": {
          "id": 1,
          "title": "文章标题"
        },
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 50,
      "totalPages": 3
    }
  }
}
```

---

#### 3.7.3 获取数据分析仪表盘

**接口：** `GET /dashboard/analytics`

**说明：** 获取数据分析仪表盘数据

**请求头：** 需要认证

**查询参数：**
- `startDate` - 开始日期
- `endDate` - 结束日期

**响应：**

```json
{
  "code": 200,
  "data": {
    "overview": {
      "totalViews": 1000,
      "totalLikes": 100,
      "totalComments": 50,
      "totalFavorites": 30
    },
    "trends": {
      "views": [
        {"date": "2024-01-01", "count": 100},
        {"date": "2024-01-02", "count": 120}
      ],
      "likes": [],
      "comments": []
    },
    "topContent": {
      "articles": [],
      "questions": [],
      "answers": []
    }
  }
}
```

---

### 3.8 分类与标签模块

#### 3.8.1 获取分类列表

**接口：** `GET /categories`

**说明：** 获取所有分类列表

**响应：**

```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "name": "技术",
        "description": "技术相关文章",
        "icon": "https://example.com/icon.png",
        "articleCount": 100
      }
    ]
  }
}
```

---

#### 3.8.2 获取标签列表

**接口：** `GET /tags`

**说明：** 获取标签列表

**查询参数：**
- `keyword` - 关键词搜索
- `page` - 页码
- `pageSize` - 每页数量
- `sort` - 排序方式：`popular`（使用次数）、`latest`（最新）

**响应：**

```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "name": "Java",
        "description": "Java相关",
        "useCount": 100
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 100,
      "totalPages": 5
    }
  }
}
```

---

#### 3.8.3 获取热门标签

**接口：** `GET /tags/hot`

**说明：** 获取热门标签（按使用次数排序）

**查询参数：**
- `limit` - 返回数量（默认20）

**响应：**

```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "name": "Java",
        "useCount": 100
      }
    ]
  }
}
```

---

### 3.9 内容管理后台模块

> **说明：** 本模块为普通用户管理自己内容与数据的后台面板，不涉及第三阶段的超级管理员功能。

#### 3.9.1 仪表盘概览

**接口：** `GET /dashboard/overview`

**说明：** 返回当前登录用户的内容统计、最近内容以及待处理事项。

**响应：**

```json
{
  "code": 200,
  "data": {
    "stats": {
      "articleCount": 12,
      "draftCount": 3,
      "questionCount": 4,
      "answerCount": 18,
      "followerCount": 56,
      "totalViews": 1200,
      "totalLikes": 320,
      "totalFavorites": 48,
      "totalComments": 210
    },
    "recentContents": [
      {
        "id": 101,
        "type": "article",
        "title": "微服务架构入门",
        "status": "published",
        "viewCount": 320,
        "likeCount": 45,
        "commentCount": 12,
        "createdAt": "2024-01-02T10:00:00"
      }
    ],
    "pendingItems": [
      {
        "type": "question",
        "description": "问题《如何优化MySQL索引？》还没有回答",
        "targetId": 88,
        "createdAt": "2024-01-01T12:00:00"
      }
    ]
  }
}
```

#### 3.9.2 我的内容列表

**接口：** `GET /dashboard/contents`

**说明：** 分页查询当前用户的文章 / 问题 / 回答列表。

**查询参数：**

| 参数 | 说明 | 备注 |
|------|------|------|
| `type` | 内容类型：`articles` / `questions` / `answers` | 默认 `articles` |
| `status` | 内容状态：文章 `draft/published/private`；问题 `pending/answered`；回答 `best` | 可选 |
| `keyword` | 关键词搜索（标题 / 内容） | 可选 |
| `page` | 页码 | 默认 1 |
| `pageSize` | 每页条数 | 默认 20 |

**响应：** 返回统一的分页结构，`items` 中的 `type`、`status` 字段描述内容类型及状态。

#### 3.9.3 我的互动记录

**接口：** `GET /dashboard/interactions`

**说明：** 查看当前用户的互动行为记录。

**查询参数：**

| 参数 | 说明 |
|------|------|
| `type` | `likes`（点赞记录） / `favorites`（收藏记录） / `comments`（评论记录） / `answers`（回答记录） |
| `page` / `pageSize` | 分页参数 |

**响应：**

```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "type": "like",
        "actionTime": "2024-01-02T09:00:00",
        "targetType": "article",
        "targetId": 101,
        "targetTitle": "Spring Cloud 进阶",
        "targetExcerpt": "……",
        "targetOwnerName": "星尘",
        "likeCount": 45,
        "commentCount": 12
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 35,
      "totalPages": 2
    }
  }
}
```

#### 3.9.4 数据趋势

**接口：** `GET /dashboard/trends`

**说明：** 返回近 7 天的内容发布趋势与粉丝增长趋势（仅统计当前用户的数据）。

**响应：**

```json
{
  "code": 200,
  "data": {
    "contentTrend": [
      {"date": "2024-01-01", "value": 2},
      {"date": "2024-01-02", "value": 0}
    ],
    "followerTrend": [
      {"date": "2024-01-01", "value": 3},
      {"date": "2024-01-02", "value": 1}
    ]
  }
}
```

---

## 4. 第二阶段：视频功能 API

> **说明：** 以下接口为第二阶段开发内容，当前阶段暂不实现。

### 4.1 视频上传系统

#### 4.1.1 初始化上传

**接口：** `POST /videos/upload/init`

**说明：** 初始化视频上传，获取上传凭证

**请求头：** 需要认证

**请求体：**

```json
{
  "filename": "string",
  "fileSize": 1024000,
  "mimeType": "video/mp4"
}
```

**响应：**

```json
{
  "code": 200,
  "data": {
    "uploadId": "upload_123",
    "chunkSize": 5242880,
    "uploadUrl": "https://storage.example.com/upload"
  }
}
```

---

#### 4.1.2 分片上传

**接口：** `POST /videos/upload/chunk`

**说明：** 上传视频分片（支持断点续传）

**请求头：** 需要认证，`Content-Type: multipart/form-data`

**请求体：**
- `uploadId` - 上传ID
- `chunkIndex` - 分片索引
- `chunk` - 分片文件

**响应：**

```json
{
  "code": 200,
  "data": {
    "uploadedChunks": [0, 1, 2],
    "totalChunks": 10
  }
}
```

---

#### 4.1.3 完成上传

**接口：** `POST /videos/upload/complete`

**说明：** 完成视频上传，触发转码

**请求头：** 需要认证

**请求体：**

```json
{
  "uploadId": "upload_123",
  "chunks": [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
}
```

**响应：**

```json
{
  "code": 200,
  "message": "上传成功，转码中",
  "data": {
    "videoId": 1,
    "transcodeStatus": "processing"
  }
}
```

---

### 4.2 视频信息管理

#### 4.2.1 创建视频

**接口：** `POST /videos`

**说明：** 创建视频（上传完成后填写视频信息）

**请求头：** 需要认证

**请求体：**

```json
{
  "title": "string",
  "description": "string",
  "categoryId": 1,
  "tags": ["tag1", "tag2"],
  "coverImage": "string",
  "relatedArticleId": 1,
  "status": 1
}
```

**响应：**

```json
{
  "code": 201,
  "message": "创建成功",
  "data": {
    "id": 1,
    "title": "视频标题"
  }
}
```

---

#### 4.2.2 获取视频详情

**接口：** `GET /videos/{id}`

**说明：** 获取视频详情（增加播放量）

**响应：**

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "title": "视频标题",
    "description": "视频描述",
    "playUrl": "https://example.com/video.m3u8",
    "qualities": [
      {"quality": "1080p", "url": "https://example.com/video_1080p.m3u8"},
      {"quality": "720p", "url": "https://example.com/video_720p.m3u8"}
    ],
    "duration": 600,
    "viewCount": 1000,
    "likeCount": 100,
    "commentCount": 50,
    "author": {},
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

---

#### 4.2.3 获取视频列表

**接口：** `GET /videos`

**说明：** 获取视频列表

**查询参数：** 同文章列表

**响应：** 类似文章列表响应

---

### 4.3 视频播放与弹幕

#### 4.3.1 记录播放进度

**接口：** `POST /videos/{id}/play-record`

**说明：** 记录视频播放进度

**请求头：** 需要认证

**请求体：**

```json
{
  "currentTime": 120,
  "duration": 600,
  "quality": "1080p"
}
```

**响应：**

```json
{
  "code": 200,
  "message": "记录成功"
}
```

---

#### 4.3.2 发送弹幕

**接口：** `POST /videos/{id}/danmaku`

**说明：** 发送弹幕（WebSocket实时推送）

**请求头：** 需要认证

**请求体：**

```json
{
  "content": "string",
  "time": 120,
  "color": "#FFFFFF",
  "type": "scroll"
}
```

**响应：**

```json
{
  "code": 200,
  "message": "发送成功",
  "data": {
    "id": 1,
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

---

#### 4.3.3 获取弹幕列表

**接口：** `GET /videos/{id}/danmaku`

**说明：** 获取视频弹幕列表

**查询参数：**
- `time` - 时间点（秒）

**响应：**

```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "content": "弹幕内容",
        "time": 120,
        "color": "#FFFFFF",
        "type": "scroll",
        "author": {
          "id": 1,
          "nickname": "用户"
        },
        "createdAt": "2024-01-01T00:00:00"
      }
    ]
  }
}
```

---

## 5. 第三阶段：管理员功能 API

> **说明：** 以下接口为第三阶段开发内容，当前阶段暂不实现。所有接口需要超级管理员权限。

### 5.1 全站内容管理

#### 5.1.1 获取待审核内容列表

**接口：** `GET /admin/content/pending`

**说明：** 获取待审核的内容列表

**请求头：** 需要超级管理员权限

**查询参数：**
- `type` - 内容类型：`articles`、`questions`、`answers`、`videos`
- `page` - 页码
- `pageSize` - 每页数量

**响应：**

```json
{
  "code": 200,
  "data": {
    "items": [],
    "pagination": {}
  }
}
```

---

#### 5.1.2 审核内容

**接口：** `PUT /admin/content/{id}/review`

**说明：** 审核内容（通过/拒绝）

**请求头：** 需要超级管理员权限

**路径参数：**
- `id` - 内容ID

**请求体：**

```json
{
  "action": "approve",  // "approve" 或 "reject"
  "remark": "string"    // 审核备注
}
```

**响应：**

```json
{
  "code": 200,
  "message": "审核成功"
}
```

---

#### 5.1.3 删除内容

**接口：** `DELETE /admin/content/{id}`

**说明：** 删除全站内容（强制删除）

**请求头：** 需要超级管理员权限

**响应：**

```json
{
  "code": 200,
  "message": "删除成功"
}
```

---

### 5.2 用户管理

#### 5.2.1 获取用户列表

**接口：** `GET /admin/users`

**说明：** 获取全站用户列表

**请求头：** 需要超级管理员权限

**查询参数：**
- `keyword` - 关键词搜索
- `status` - 状态筛选
- `page` - 页码
- `pageSize` - 每页数量

**响应：**

```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "username": "user1",
        "nickname": "用户1",
        "status": 1,
        "articleCount": 10,
        "followerCount": 100,
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "pagination": {}
  }
}
```

---

#### 5.2.2 封禁用户

**接口：** `PUT /admin/users/{id}/ban`

**说明：** 封禁用户

**请求头：** 需要超级管理员权限

**路径参数：**
- `id` - 用户ID

**请求体：**

```json
{
  "reason": "string"  // 封禁原因
}
```

**响应：**

```json
{
  "code": 200,
  "message": "封禁成功"
}
```

---

#### 5.2.3 解封用户

**接口：** `PUT /admin/users/{id}/unban`

**说明：** 解封用户

**请求头：** 需要超级管理员权限

**响应：**

```json
{
  "code": 200,
  "message": "解封成功"
}
```

---

### 5.3 系统设置与配置

#### 5.3.1 获取系统配置

**接口：** `GET /admin/config`

**说明：** 获取系统配置

**请求头：** 需要超级管理员权限

**响应：**

```json
{
  "code": 200,
  "data": {
    "siteName": "Corasta",
    "siteDescription": "次世代多用户内容社区平台",
    "reviewEnabled": true
  }
}
```

---

#### 5.3.2 更新系统配置

**接口：** `PUT /admin/config`

**说明：** 更新系统配置

**请求头：** 需要超级管理员权限

**请求体：**

```json
{
  "siteName": "string",
  "siteDescription": "string",
  "reviewEnabled": true
}
```

**响应：**

```json
{
  "code": 200,
  "message": "更新成功"
}
```

---

#### 5.3.2 管理分类

**接口：** `POST /admin/categories`

**说明：** 创建分类

**请求头：** 需要超级管理员权限

**请求体：**

```json
{
  "name": "string",
  "description": "string",
  "icon": "string",
  "sortOrder": 0
}
```

**响应：**

```json
{
  "code": 201,
  "message": "创建成功"
}
```

---

### 5.4 数据统计与分析

#### 5.4.1 获取全站数据概览

**接口：** `GET /admin/statistics/overview`

**说明：** 获取全站数据概览

**请求头：** 需要超级管理员权限

**响应：**

```json
{
  "code": 200,
  "data": {
    "totalUsers": 1000,
    "totalArticles": 5000,
    "totalQuestions": 2000,
    "totalAnswers": 8000,
    "totalVideos": 0,
    "totalViews": 100000,
    "totalLikes": 10000,
    "totalComments": 5000
  }
}
```

---

#### 5.4.2 获取数据趋势

**接口：** `GET /admin/statistics/trends`

**说明：** 获取数据趋势（按时间）

**请求头：** 需要超级管理员权限

**查询参数：**
- `startDate` - 开始日期
- `endDate` - 结束日期
- `type` - 数据类型：`users`、`content`、`interactions`

**响应：**

```json
{
  "code": 200,
  "data": {
    "trends": [
      {
        "date": "2024-01-01",
        "users": 10,
        "articles": 50,
        "questions": 20
      }
    ]
  }
}
```

---

---

## 附录

### A. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 201 | 资源创建成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（未登录或Token无效） |
| 403 | 禁止访问（无权限） |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 422 | 请求参数验证失败 |
| 500 | 服务器内部错误 |

### B. 数据字典

#### 内容状态（status）

- `0` - 草稿
- `1` - 已发布
- `2` - 私密

#### 用户状态（status）

- `0` - 封禁
- `1` - 正常

#### 用户角色（role）

- `0` - 普通用户
- `1` - 超级管理员

#### 通知类型（type）

- `comment` - 新评论/回复
- `like` - 新点赞
- `follow` - 新粉丝
- `answer` - 新回答
- `best_answer` - 回答被采纳
- `invite` - 被邀请回答

#### 内容类型（targetType）

- `article` - 文章
- `question` - 问题
- `answer` - 回答
- `video` - 视频
- `comment` - 评论

---

**文档版本：** V1.0  
**最后更新：** 2024

