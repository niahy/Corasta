<template>
  <div class="article-detail-page">
    <div class="app-shell">
      <!-- 返回按钮 -->
      <router-link to="/articles" class="back-link">← 返回文章列表</router-link>

      <!-- 加载中 -->
      <div v-if="loading" class="loading-container">
        <Loading text="加载中..." />
      </div>

      <!-- 文章内容 -->
      <article v-else-if="article" class="article-detail">
        <!-- 文章头部 -->
        <header class="article-header">
          <div class="article-category">
            <Badge v-if="article.category" variant="secondary">
              {{ article.category.name }}
            </Badge>
          </div>
          <h1 class="article-title">{{ article.title }}</h1>
          <div class="article-author-info">
            <Avatar
              :src="article.author.avatar"
              :name="article.author.nickname"
              size="medium"
            />
            <div class="author-details">
              <div class="author-name">{{ article.author.nickname }}</div>
              <div class="article-meta">
                <span>{{ formatRelativeTime(article.createdAt) }}</span>
                <span>·</span>
                <span>{{ formatLargeNumber(article.viewCount) }} 阅读</span>
              </div>
            </div>
          </div>
        </header>

        <!-- 封面图 -->
        <div v-if="article.coverImage" class="article-cover">
          <img :src="article.coverImage" :alt="article.title" />
        </div>

        <!-- 文章正文 -->
        <div class="article-content">
          <div class="markdown-body" v-html="renderedContent"></div>
        </div>

        <!-- 文章底部信息 -->
        <footer class="article-footer">
          <div class="article-tags">
            <span
              v-for="tag in article.tags"
              :key="tag.id"
              class="article-tag"
            >
              #{{ tag.name }}
            </span>
          </div>
          <div class="article-stats">
            <span class="stat-item">
              <span class="stat-icon">👁</span>
              {{ formatLargeNumber(article.viewCount) }}
            </span>
            <span class="stat-item">
              <span class="stat-icon">❤️</span>
              {{ formatLargeNumber(article.likeCount) }}
            </span>
            <span class="stat-item">
              <span class="stat-icon">💬</span>
              {{ formatLargeNumber(article.commentCount) }}
            </span>
            <span class="stat-item">
              <span class="stat-icon">⭐</span>
              {{ formatLargeNumber(article.favoriteCount) }}
            </span>
          </div>
        </footer>

        <!-- 互动操作栏 -->
        <div class="article-actions">
          <Button
            :variant="article.isLiked ? 'primary' : 'outline'"
            @click="handleLike"
            :loading="likeLoading"
          >
            {{ article.isLiked ? '已点赞' : '点赞' }}
          </Button>
          <Button
            :variant="article.isFavorited ? 'primary' : 'outline'"
            @click="handleFavorite"
            :loading="favoriteLoading"
          >
            {{ article.isFavorited ? '已收藏' : '收藏' }}
          </Button>
          <Button variant="outline" @click="handleShare">分享</Button>
          <Button
            v-if="isAuthor"
            variant="outline"
            :to="`/articles/edit/${article.id}`"
          >
            编辑
          </Button>
        </div>

        <!-- 评论区域 -->
        <section class="article-comments">
          <h3 class="comments-title">评论 · {{ article.commentCount }}</h3>
          <CommentList
            :target-type="'article'"
            :target-id="article.id"
            :is-author="isAuthor"
            @comment-added="handleCommentAdded"
            @comment-updated="handleCommentUpdated"
          />
        </section>

        <!-- 相关推荐 -->
        <aside class="article-related">
          <Card>
            <template #header>
              <h3 class="related-title">相关阅读</h3>
            </template>
            <div class="related-list">
              <!-- TODO: 相关文章推荐 -->
              <p class="related-placeholder">相关文章推荐功能开发中...</p>
            </div>
          </Card>
        </aside>
      </article>

      <!-- 收藏夹选择对话框 -->
      <div v-if="showFolderDialog" class="folder-dialog-overlay" @click="showFolderDialog = false">
        <Card class="folder-dialog" @click.stop>
          <template #header>
            <h3 class="dialog-title">选择收藏夹</h3>
          </template>
          <div class="folder-dialog-content">
            <div class="folder-list">
              <label
                v-for="folder in folders"
                :key="folder.id"
                :class="['folder-item', { active: selectedFolderId === folder.id }]"
              >
                <input
                  type="radio"
                  :value="folder.id"
                  v-model="selectedFolderId"
                />
                <div class="folder-info">
                  <div class="folder-name">{{ folder.name }}</div>
                  <div v-if="folder.description" class="folder-description">
                    {{ folder.description }}
                  </div>
                  <div class="folder-count">{{ folder.itemCount || 0 }} 个内容</div>
                </div>
              </label>
            </div>
            <div class="create-folder-section">
              <Input
                v-model="newFolderName"
                placeholder="创建新收藏夹"
                :maxlength="50"
                @keyup.enter="handleCreateFolder"
              >
                <template #suffix>
                  <Button
                    variant="text"
                    size="small"
                    :loading="creatingFolder"
                    @click="handleCreateFolder"
                  >
                    创建
                  </Button>
                </template>
              </Input>
            </div>
          </div>
          <template #footer>
            <div class="dialog-actions">
              <Button variant="outline" @click="showFolderDialog = false">取消</Button>
              <Button
                variant="primary"
                :loading="favoriteLoading"
                :disabled="!selectedFolderId"
                @click="confirmFavorite"
              >
                确认收藏
              </Button>
            </div>
          </template>
        </Card>
      </div>

      <!-- 文章不存在 -->
      <div v-else class="not-found">
        <p>文章不存在</p>
        <Button variant="primary" to="/articles">返回文章列表</Button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getArticleDetail } from '@/api/article'
import { likeContent, unlikeContent } from '@/api/like'
import { favoriteContent, unfavoriteContent, getFolderList, createFolder } from '@/api/favorite'
import { formatRelativeTime, formatLargeNumber } from '@/utils/format'
import Card from '@/components/Card.vue'
import Button from '@/components/Button.vue'
import Badge from '@/components/Badge.vue'
import Avatar from '@/components/Avatar.vue'
import Loading from '@/components/Loading.vue'
import CommentList from '@/components/CommentList.vue'
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const article = ref(null)
const loading = ref(false)
const likeLoading = ref(false)
const favoriteLoading = ref(false)
const showFolderDialog = ref(false)
const folders = ref([])
const selectedFolderId = ref(null)
const newFolderName = ref('')
const creatingFolder = ref(false)

const isAuthor = computed(() => {
  return article.value && userStore.userInfo && article.value.author.id === userStore.userInfo.id
})

// 简单的 Markdown 渲染（基础版本，后续可集成专业库）
const renderedContent = computed(() => {
  if (!article.value || !article.value.content) return ''
  
  let content = article.value.content
  // 转义 HTML
  content = content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
  
  // 标题
  content = content.replace(/^### (.*$)/gim, '<h3>$1</h3>')
  content = content.replace(/^## (.*$)/gim, '<h2>$1</h2>')
  content = content.replace(/^# (.*$)/gim, '<h1>$1</h1>')
  
  // 粗体和斜体
  content = content.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  content = content.replace(/\*(.*?)\*/g, '<em>$1</em>')
  
  // 代码块
  content = content.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
  content = content.replace(/`([^`]+)`/g, '<code>$1</code>')
  
  // 链接
  content = content.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
  
  // 图片
  content = content.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" />')
  
  // 换行
  content = content.replace(/\n/g, '<br>')
  
  return content
})

// 获取文章详情
async function fetchArticleDetail() {
  loading.value = true
  try {
    const id = route.params.id
    const response = await getArticleDetail(id)

    if (response.data) {
      article.value = response.data
    }
  } catch (error) {
    console.error('获取文章详情失败:', error)
    alert(error.message || '获取文章详情失败，请重试')
  } finally {
    loading.value = false
  }
}

// 点赞/取消点赞
async function handleLike() {
  if (!userStore.isLoggedIn) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }

  likeLoading.value = true
  try {
    if (article.value.isLiked) {
      await unlikeContent('article', article.value.id)
      article.value.isLiked = false
      article.value.likeCount -= 1
    } else {
      await likeContent({ targetType: 'article', targetId: article.value.id })
      article.value.isLiked = true
      article.value.likeCount += 1
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    alert(error.message || '操作失败，请重试')
  } finally {
    likeLoading.value = false
  }
}

// 获取收藏夹列表
async function fetchFolders() {
  try {
    const response = await getFolderList()
    if (response.data && response.data.items) {
      folders.value = response.data.items
    }
  } catch (error) {
    console.error('获取收藏夹列表失败:', error)
  }
}

// 创建收藏夹
async function handleCreateFolder() {
  if (!newFolderName.value.trim()) {
    alert('请输入收藏夹名称')
    return
  }

  creatingFolder.value = true
  try {
    const response = await createFolder({
      name: newFolderName.value.trim(),
    })
    if (response.data) {
      folders.value.push(response.data)
      selectedFolderId.value = response.data.id
      newFolderName.value = ''
    }
  } catch (error) {
    console.error('创建收藏夹失败:', error)
    alert(error.message || '创建失败，请重试')
  } finally {
    creatingFolder.value = false
  }
}

// 收藏/取消收藏
async function handleFavorite() {
  if (!userStore.isLoggedIn) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }

  // 如果已收藏，直接取消收藏
  if (article.value.isFavorited) {
    favoriteLoading.value = true
    try {
      await unfavoriteContent('article', article.value.id)
      article.value.isFavorited = false
      article.value.favoriteCount -= 1
    } catch (error) {
      console.error('取消收藏失败:', error)
      alert(error.message || '操作失败，请重试')
    } finally {
      favoriteLoading.value = false
    }
    return
  }

  // 如果未收藏，显示收藏夹选择对话框
  await fetchFolders()
  selectedFolderId.value = folders.value.length > 0 ? folders.value[0].id : null
  showFolderDialog.value = true
}

// 确认收藏
async function confirmFavorite() {
  if (!selectedFolderId.value) {
    alert('请选择收藏夹')
    return
  }

  favoriteLoading.value = true
  try {
    await favoriteContent({
      targetType: 'article',
      targetId: article.value.id,
      folderId: selectedFolderId.value,
    })
    article.value.isFavorited = true
    article.value.favoriteCount += 1
    showFolderDialog.value = false
    selectedFolderId.value = null
  } catch (error) {
    console.error('收藏失败:', error)
    alert(error.message || '收藏失败，请重试')
  } finally {
    favoriteLoading.value = false
  }
}

// 分享
function handleShare() {
  // TODO: 实现分享功能
  const url = window.location.href
  navigator.clipboard.writeText(url).then(() => {
    alert('链接已复制到剪贴板')
  }).catch(() => {
    alert('复制失败，请手动复制链接')
  })
}

// 评论添加后更新计数
function handleCommentAdded() {
  if (article.value) {
    article.value.commentCount += 1
  }
}

// 评论更新后刷新详情
function handleCommentUpdated() {
  fetchArticleDetail()
}

// 组件挂载时获取数据
onMounted(() => {
  fetchArticleDetail()
})
</script>

<style scoped>
.article-detail-page {
  min-height: 100vh;
  padding: 40px 0;
}

.back-link {
  display: inline-block;
  margin-bottom: 24px;
  color: var(--midnight-purple);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.back-link:hover {
  color: var(--coral-pink);
}

.loading-container {
  padding: 80px 20px;
  text-align: center;
}

.article-detail {
  max-width: 900px;
  margin: 0 auto;
}

.article-header {
  margin-bottom: 32px;
}

.article-category {
  margin-bottom: 12px;
}

.article-title {
  font-size: 36px;
  font-weight: 700;
  color: var(--midnight-purple);
  margin: 0 0 24px 0;
  line-height: 1.3;
}

.article-author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-details {
  flex: 1;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin-bottom: 4px;
}

.article-meta {
  font-size: 14px;
  color: var(--text-dark);
  opacity: 0.7;
}

.article-cover {
  width: 100%;
  max-height: 500px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-bottom: 32px;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.article-content {
  margin-bottom: 32px;
  line-height: 1.8;
  color: var(--text-dark);
}

.markdown-body {
  font-size: 16px;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  color: var(--midnight-purple);
  margin-top: 32px;
  margin-bottom: 16px;
}

.markdown-body :deep(p) {
  margin-bottom: 16px;
}

.markdown-body :deep(code) {
  background: rgba(166, 140, 224, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
}

.markdown-body :deep(pre) {
  background: #f5f5f5;
  padding: 16px;
  border-radius: var(--radius-sm);
  overflow-x: auto;
  margin-bottom: 16px;
}

.markdown-body :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-sm);
  margin: 16px 0;
}

.article-footer {
  padding: 24px 0;
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
  margin-bottom: 32px;
}

.article-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.article-tag {
  font-size: 14px;
  color: var(--stardust-purple);
  padding: 6px 12px;
  background: rgba(166, 140, 224, 0.1);
  border-radius: var(--radius-sm);
}

.article-stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  color: var(--text-dark);
  opacity: 0.8;
}

.stat-icon {
  font-size: 18px;
}

.article-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 40px;
  padding-bottom: 32px;
  border-bottom: 1px solid var(--border);
}

.article-comments {
  margin-bottom: 40px;
}

.comments-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin: 0 0 24px 0;
}

.comments-placeholder {
  padding: 40px;
  text-align: center;
  color: var(--text-dark);
  opacity: 0.6;
}

.article-related {
  margin-top: 40px;
}

.related-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin: 0;
}

.related-list {
  padding: 16px 0;
}

.related-placeholder {
  padding: 20px;
  text-align: center;
  color: var(--text-dark);
  opacity: 0.6;
}

.not-found {
  padding: 80px 20px;
  text-align: center;
}

.not-found p {
  font-size: 20px;
  color: var(--text-dark);
  margin-bottom: 24px;
}

.folder-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.folder-dialog {
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.dialog-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin: 0;
}

.folder-dialog-content {
  padding: 20px 0;
}

.folder-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.folder-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
}

.folder-item:hover {
  border-color: var(--coral-pink);
  background: rgba(255, 126, 138, 0.05);
}

.folder-item.active {
  border-color: var(--coral-pink);
  background: rgba(255, 126, 138, 0.1);
}

.folder-item input[type="radio"] {
  margin-top: 4px;
  cursor: pointer;
}

.folder-info {
  flex: 1;
}

.folder-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--midnight-purple);
  margin-bottom: 4px;
}

.folder-description {
  font-size: 13px;
  color: var(--text-dark);
  opacity: 0.7;
  margin-bottom: 4px;
}

.folder-count {
  font-size: 12px;
  color: var(--text-dark);
  opacity: 0.6;
}

.create-folder-section {
  padding-top: 16px;
  border-top: 1px solid var(--border);
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .article-title {
    font-size: 28px;
  }

  .article-actions {
    flex-wrap: wrap;
  }
}
</style>

