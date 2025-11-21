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

## 📋 开发阶段

### ✅ 第一阶段：核心功能开发（当前阶段）
- [x] 用户中心模块（注册、登录、个人资料）
- [x] 文章发布与管理模块
- [x] 问答系统
- [x] 内容互动（评论、点赞、收藏）
- [x] 关注系统
- [x] 通知系统
- [x] 搜索功能
- [x] 内容管理后台

### ⏳ 第二阶段：视频功能开发（待开发）
- [ ] 视频上传系统（大文件上传、断点续传）
- [ ] 视频转码服务（FFmpeg + HLS）
- [ ] 视频播放器集成
- [ ] 实时弹幕系统（WebSocket）
- [ ] 视频管理后台

### ⏳ 第三阶段：超级管理员功能（待开发）
- [ ] 超级管理员角色与权限系统
- [ ] 全站内容审核与管理
- [ ] 用户管理（封禁、解封等）
- [ ] 系统设置与配置管理
- [ ] 全站数据统计与分析

## 🚀 快速开始

### 环境要求
- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/yourusername/Corasta.git
cd Corasta
```

2. **数据库初始化**
```bash
# 执行数据库初始化脚本
mysql -u root -p < back/src/main/resources/db/schema.sql
```

3. **后端启动**
```bash
cd back
mvn spring-boot:run
```

4. **前端启动**
```bash
cd front
npm install
npm run dev
```

## 📁 项目结构

```
Corasta/
├── back/                 # 后端项目
│   ├── src/
│   │   └── main/
│   │       ├── java/     # Java源码
│   │       └── resources/
│   │           └── db/   # 数据库脚本
│   └── pom.xml
├── front/                # 前端项目
│   ├── src/
│   └── package.json
├── 用户需求xx版.md       # 需求文档
├── 数据库设计文档.md     # 数据库设计文档
└── README.md
```

## 🤝 贡献指南

我们欢迎所有形式的贡献！如果你有任何想法或发现问题，请：

1. Fork 本项目
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启一个 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## ⭐ 致谢

如果这个项目对你有帮助，请给我们一个 ⭐ Star！你的支持是我们前进的动力。

---

<div align="center">

**Made with ❤️ by Corasta Team**

[⭐ Star us on GitHub](https://github.com/yourusername/Corasta) | [📖 查看文档](用户需求xx版.md) | [🐛 报告问题](https://github.com/yourusername/Corasta/issues)

</div>

