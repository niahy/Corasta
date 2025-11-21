# 🌟 Corasta

> **Coral（珊瑚） + Stardust（星尘）**  
> 专为年轻人打造的青春浪漫、动漫般的罗曼蒂克恋爱风格社区平台

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.x-4FC08D.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1.svg)](https://www.mysql.com/)

## ✨ 项目简介

**Corasta** 是一个次世代多用户内容社区平台，融合了文章创作、视频分享、知识问答等多种内容形态。我们致力于为年轻人打造一个充满青春浪漫氛围、具有动漫般罗曼蒂克风格的社区空间，让每一位用户都能在这里找到情感共鸣，分享创作灵感。

### 🎯 核心特性

- 📝 **多维内容创作** - 支持文章、视频、问答三种核心内容形态
- 💬 **深度互动体验** - 评论、点赞、收藏、关注、实时弹幕等丰富互动
- 🎨 **青春浪漫风格** - 专为年轻人设计的UI/UX，营造动漫般的浪漫氛围
- 👥 **社区化生态** - 关注、动态Feed、问答系统，促进用户连接
- 📊 **数据可视化** - 个人内容管理与数据分析仪表盘
- 🔍 **智能搜索** - 全文检索，支持多维度筛选

## 🛠️ 技术栈

### 前端
- **Vue 3** - 渐进式JavaScript框架
- **Vite** - 下一代前端构建工具
- **Pinia** - 状态管理
- **Element Plus** - UI组件库
- **Markdown编辑器** - 富文本内容创作

### 后端
- **Spring Boot 3.x** - Java应用框架
- **Spring Data JPA** - 持久层框架
- **MySQL 8.0+** - 关系型数据库
- **Elasticsearch** - 全文搜索引擎
- **JWT** - 身份认证

### 未来规划
- **FFmpeg** - 视频转码（第二阶段）
- **WebSocket** - 实时弹幕系统（第二阶段）
- **MinIO/S3** - 对象存储（第二阶段）

## 📊 开发进度

### 整体进度概览

```
项目准备阶段        ████████████████████ 100% ✅
第一阶段：核心功能  ░░░░░░░░░░░░░░░░░░░░   0% ⏳
第二阶段：视频功能  ░░░░░░░░░░░░░░░░░░░░   0% ⏳
第三阶段：管理员    ░░░░░░░░░░░░░░░░░░░░   0% ⏳
```

### 📋 详细开发阶段

#### 🎯 项目准备阶段（已完成）

<details>
<summary><b>✅ 已完成工作（点击展开）</b></summary>

| 工作项 | 说明 | 状态 |
|--------|------|------|
| 📋 需求文档 | 完整的功能需求规格说明书 | ✅ 完成 |
| 🗄️ 数据库设计 | 数据库表结构设计文档 | ✅ 完成 |
| 📝 SQL脚本 | 可直接执行的数据库初始化脚本 | ✅ 完成 |
| 🏗️ 项目初始化 | 前后端项目框架搭建 | ✅ 完成 |

</details>

**完成度：** `4/4` 项 ✅

---

#### 🎯 第一阶段：核心功能开发（进行中）

<details>
<summary><b>⏳ 开发中功能（点击展开）</b></summary>

| 模块 | 功能点 | 状态 |
|------|--------|------|
| 👤 用户中心模块 | 用户注册、登录、密码管理、个人资料管理 | ⏳ 待开发 |
| 📝 文章发布与管理模块 | 文章编辑器、文章管理、文章浏览 | ⏳ 待开发 |
| ❓ 问答系统模块 | 提问功能、回答功能、问答互动机制、问答浏览与管理 | ⏳ 待开发 |
| 💬 内容互动模块 | 评论系统、点赞与收藏、分享功能 | ⏳ 待开发 |
| 👥 社区与社交模块 | 关注系统、动态Feed流、通知系统 | ⏳ 待开发 |
| 🔍 搜索模块 | 全站搜索引擎 | ⏳ 待开发 |
| 📊 内容管理后台模块 | 内容管理仪表盘、我的互动记录、数据分析仪表盘 | ⏳ 待开发 |

</details>

**完成度：** `0/7` 模块 ⏳

---

#### 🎬 第二阶段：视频功能开发（规划中）

<details>
<summary><b>⏳ 待开发功能（点击展开）</b></summary>

| 模块 | 功能点 | 状态 |
|------|--------|------|
| 📤 视频上传系统 | 大文件上传、断点续传 | ⏳ 待开发 |
| 🎞️ 视频转码服务 | FFmpeg + HLS多清晰度转码 | ⏳ 待开发 |
| 📝 视频信息管理 | 视频元数据编辑和管理、视频管理后台 | ⏳ 待开发 |
| ▶️ 视频播放器 | HLS播放器、清晰度切换、播放统计 | ⏳ 待开发 |
| 💬 实时弹幕系统 | WebSocket实时弹幕、弹幕渲染、弹幕管理 | ⏳ 待开发 |

</details>

**完成度：** `0/5` 模块 ⏳

**说明：** 视频相关搜索优化将在搜索模块（4.6）中实现，不单独列出。

---

#### 👑 第三阶段：超级管理员功能（规划中）

<details>
<summary><b>⏳ 待开发功能（点击展开）</b></summary>

| 模块 | 功能点 | 状态 |
|------|--------|------|
| ✅ 全站内容管理 | 内容审核、内容管理、内容统计 | ⏳ 待开发 |
| 👥 用户管理 | 用户列表、用户操作、用户统计 | ⏳ 待开发 |
| ⚙️ 系统设置与配置 | 分类管理、标签管理、系统参数配置 | ⏳ 待开发 |
| 📈 数据统计与分析（全站） | 全站数据概览、内容分析、用户分析、互动分析 | ⏳ 待开发 |

</details>

**完成度：** `0/4` 模块 ⏳

---

### 📈 项目统计

```
总模块数：16
已完成：  4  (25.0%) - 项目准备阶段
进行中：  0  (0.0%)
待开发： 12  (75.0%) - 功能开发阶段
  ├─ 第一阶段：7 个模块
  ├─ 第二阶段：5 个模块
  └─ 第三阶段：4 个模块
```

## 📁 项目结构

```
Corasta/
├── 📂 back/                    # 后端项目 (Spring Boot)
│   ├── src/
│   │   └── main/
│   │       ├── java/           # Java 源码
│   │       └── resources/
│   │           └── db/         # 数据库脚本 (crsa.sql)
│   └── pom.xml                 # Maven 依赖配置
│
├── 📂 front/                    # 前端项目 (Vue 3)
│   ├── src/                     # 前端源码
│   └── package.json             # NPM 依赖配置
│
├── 📄 用户需求xx版.md          # 📋 需求规格说明书
├── 📄 数据库设计文档.md        # 🗄️ 数据库设计文档
└── 📄 README.md                 # 📖 项目说明文档
```

### 📚 文档说明

- **用户需求xx版.md** - 详细的功能需求规格说明，包含所有模块的设计和功能点
- **数据库设计文档.md** - 完整的数据库表结构设计，包含索引、外键、预留字段等
- **crsa.sql** - 可直接执行的数据库初始化脚本

## 🐛 问题反馈

如果你在使用过程中遇到任何问题或发现 Bug，请通过以下方式反馈：

### 📝 创建 Issue

1. 访问 [Issues 页面](https://github.com/naihy/Corasta/issues)
2. 点击 "New Issue" 创建新问题
3. 选择合适的问题类型（Bug、功能建议、文档改进等）
4. 详细描述问题，包括：
   - 问题描述
   - 复现步骤
   - 预期行为
   - 实际行为
   - 环境信息（浏览器、操作系统等）
   - 截图或日志（如有）

### 💡 功能建议

我们非常欢迎你的功能建议！如果你有好的想法，请：
- 创建 Feature Request Issue
- 详细描述功能需求和使用场景
- 说明该功能的价值和意义

---

## 🤝 贡献指南

我们欢迎所有形式的贡献！无论是代码、文档、设计还是建议，都是对我们项目的巨大帮助。

### 如何贡献

1. **Fork 本项目** - 点击右上角的 Fork 按钮
2. **创建特性分支** - `git checkout -b feature/AmazingFeature`
3. **提交你的更改** - `git commit -m 'Add some AmazingFeature'`
4. **推送到分支** - `git push origin feature/AmazingFeature`
5. **开启 Pull Request** - 在 GitHub 上创建 Pull Request

### 贡献类型

- 💻 **代码贡献** - 修复 Bug、添加新功能、优化性能
- 📖 **文档贡献** - 完善文档、添加示例、改进说明
- 🎨 **设计贡献** - UI/UX 改进、视觉设计、交互优化
- 🐛 **Bug 报告** - 发现问题、提供复现步骤、协助修复
- 💡 **功能建议** - 提出新想法、讨论改进方向

### 开发规范

- 遵循项目代码风格
- 添加必要的注释和文档
- 确保代码通过测试
- 提交前检查代码质量

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🌟 致谢

感谢所有为这个项目做出贡献的开发者！你的每一个 Star、Issue 和 Pull Request 都是对我们最大的支持。

### ⭐ 如果这个项目对你有帮助

- ⭐ **Star** 这个项目，让更多人看到
- 🍴 **Fork** 这个项目，开始你的探索
- 🐛 **报告 Bug**，帮助我们改进
- 💡 **提出建议**，分享你的想法
- 📢 **分享项目**，推荐给更多人

---

## 📞 联系我们

- 📧 **邮箱**：Sumsurf@outlook.com
- 💬 **讨论区**：[GitHub Discussions](https://github.com/niahy/Corasta/discussions)
- 🐛 **问题反馈**：[GitHub Issues](https://github.com/niahy/Corasta/issues)

---

<div align="center">

### ✨ Made with ❤️ by Corasta Team ✨

[![GitHub stars](https://img.shields.io/github/stars/niahy/Corasta?style=social)](https://github.com/yourusername/Corasta)
[![GitHub forks](https://img.shields.io/github/forks/niahy/Corasta?style=social)](https://github.com/yourusername/Corasta)
[![GitHub watchers](https://img.shields.io/github/watchers/niahy/Corasta?style=social)](https://github.com/yourusername/Corasta)

---

[⭐ Star us on GitHub](https://github.com/niahy/Corasta) • [📖 查看文档](用户需求xx版.md) • [🐛 报告问题](https://github.com/niahy/Corasta/issues) • [💬 参与讨论](https://github.com/niahy/Corasta/discussions)

**让青春与浪漫在这里相遇** 🌸✨

</div>

