<template>
  <div class="feed-page">
    <div class="app-shell">
      <div class="feed-container">
        <!-- 主要内容区域 -->
        <main class="feed-main">
          <!-- 筛选标签 -->
          <div class="feed-tabs">
            <button
              v-for="tab in tabs"
              :key="tab.value"
              :class="['tab-btn', { active: activeTab === tab.value }]"
              @click="handleTabChange(tab.value)"
            >
              {{ tab.label }}
            </button>
          </div>

          <!-- Feed流列表 -->
          <div v-if="loading && items.length === 0" class="loading-container">
            <Loading text="加载中..." />
          </div>

          <div v-else-if="items.length === 0" class="empty-container">
            <div class="empty-content">
              <p class="empty-text">暂无动态</p>
              <p class="empty-hint">关注一些创作者，开始你的探索之旅吧</p>
              <Button variant="primary" to="/search">发现创作者</Button>
            </div>
          </div>

          <div v-else class="feed-list">
            <article
              v-for="item in items"
              :key="`${item.type}-${item.content.id}`"
              class="feed-item"
            >
              <Card hover>
                <div class="feed-item-header">
                  <div class="feed-item-meta">
                    <Badge :variant="getTypeBadgeVariant(item.type)">
                      {{ getTypeLabel(item.type) }}
                    </Badge>
                    <div class="feed-item-author">
                      <Avatar
                        :src="item.author.avatar"
                        :name="item.author.nickname"
                        size="small"
                      />
                      <span class="author-name">{{ item.author.nickname }}</span>
                    </div>
                  </div>
                  <span class="feed-item-time">{{ formatRelativeTime(item.createdAt) }}</span>
                </div>

                <div class="feed-item-content">
                  <h3 class="feed-item-title">
                    <router-link
                      :to="getContentLink(item.type, item.content.id)"
                      class="feed-item-link"
                    >
                      {{ item.content.title || item.content.description?.substring(0, 50) }}
                    </router-link>
                  </h3>
                  <p v-if="item.content.summary" class="feed-item-summary">
                    {{ item.content.summary }}
                  </p>
                  <div v-if="item.content.coverImage && !coverImageErrors[`${item.type}-${item.content.id}`]" class="feed-item-cover">
                    <img :src="item.content.coverImage" :alt="item.content.title" @error="coverImageErrors[`${item.type}-${item.content.id}`] = true" />
                  </div>
                </div>

                <div class="feed-item-footer">
                  <div class="feed-item-stats">
                    <span v-if="item.content.viewCount !== undefined" class="stat-item">
                      <span class="stat-icon">👁</span>
                      {{ formatLargeNumber(item.content.viewCount) }}
                    </span>
                    <span v-if="item.content.likeCount !== undefined" class="stat-item">
                      <span class="stat-icon">❤️</span>
                      {{ formatLargeNumber(item.content.likeCount) }}
                    </span>
                    <span v-if="item.content.commentCount !== undefined" class="stat-item">
                      <span class="stat-icon">💬</span>
                      {{ formatLargeNumber(item.content.commentCount) }}
                    </span>
                    <span v-if="item.content.answerCount !== undefined" class="stat-item">
                      <span class="stat-icon">💡</span>
                      {{ formatLargeNumber(item.content.answerCount) }}
                    </span>
                  </div>
                  <div class="feed-item-actions">
                    <Button variant="text" size="small" @click="handleLike(item)">
                      {{ item.isLiked ? '已点赞' : '点赞' }}
                    </Button>
                    <Button variant="text" size="small" @click="handleComment(item)">
                      评论
                    </Button>
                  </div>
                </div>
              </Card>
            </article>
          </div>

          <!-- 分页加载 -->
          <div v-if="hasMore && !loading" class="load-more">
            <Button variant="outline" :loading="loadingMore" @click="loadMore">
              加载更多
            </Button>
          </div>

          <div v-if="loadingMore" class="loading-more">
            <Loading text="加载中..." />
          </div>
        </main>

        <!-- 侧边栏 -->
        <aside class="feed-sidebar">
          <Card class="sidebar-card">
            <template #header>
              <h3 class="sidebar-title">热门话题</h3>
            </template>
            <div class="topic-list">
              <div class="topic-item">
                <span class="topic-tag"># 夏夜</span>
                <span class="topic-count">1024 讨论</span>
              </div>
              <div class="topic-item">
                <span class="topic-tag"># 动漫配色</span>
                <span class="topic-count">845 讨论</span>
              </div>
              <div class="topic-item">
                <span class="topic-tag"># 角色设定</span>
                <span class="topic-count">640 讨论</span>
              </div>
            </div>
          </Card>

          <Card class="sidebar-card mt-20">
            <template #header>
              <h3 class="sidebar-title">本周创作者</h3>
            </template>
            <div class="creator-list">
              <div class="creator-item">
                <Avatar src="" name="星尘" size="small" />
                <div class="creator-info">
                  <div class="creator-name">星尘</div>
                  <div class="creator-stats">12 篇文章</div>
                </div>
              </div>
            </div>
          </Card>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getFeed } from '@/api/feed'
import { formatRelativeTime, formatLargeNumber } from '@/utils/format'
import Card from '@/components/Card.vue'
import Button from '@/components/Button.vue'
import Badge from '@/components/Badge.vue'
import Avatar from '@/components/Avatar.vue'
import Loading from '@/components/Loading.vue'

const router = useRouter()
const userStore = useUserStore()

// 标签配置
const tabs = [
  { label: '全部', value: 'all' },
  { label: '文章', value: 'articles' },
  { label: '问答', value: 'questions' },
]

const activeTab = ref('all')
const items = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const coverImageErrors = ref({})
const pagination = ref({
  page: 1,
  pageSize: 20,
  total: 0,
  totalPages: 0,
})

const hasMore = computed(() => {
  return pagination.value.page < pagination.value.totalPages
})

// 获取类型标签样式
function getTypeBadgeVariant(type) {
  const variantMap = {
    article: 'primary',
    question: 'secondary',
    answer: 'secondary',
    video: 'primary',
  }
  return variantMap[type] || 'primary'
}

// 获取类型标签文本
function getTypeLabel(type) {
  const labelMap = {
    article: '文章',
    question: '问答',
    answer: '回答',
    video: '视频',
  }
  return labelMap[type] || '内容'
}

// 获取内容链接
function getContentLink(type, id) {
  const linkMap = {
    article: `/articles/${id}`,
    question: `/questions/${id}`,
    answer: `/questions/${id}`, // 回答跳转到问题详情页
    video: `/videos/${id}`, // 第二阶段
  }
  return linkMap[type] || '#'
}

// 切换标签
function handleTabChange(type) {
  activeTab.value = type
  items.value = []
  pagination.value.page = 1
  fetchFeed()
}

// 获取Feed流数据
async function fetchFeed(reset = true) {
  if (reset) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const response = await getFeed({
      page: pagination.value.page,
      pageSize: pagination.value.pageSize,
      type: activeTab.value === 'all' ? undefined : activeTab.value,
    })

    if (response.data) {
      if (reset) {
        items.value = response.data.items || []
      } else {
        items.value.push(...(response.data.items || []))
      }
      pagination.value = {
        ...pagination.value,
        ...response.data.pagination,
      }
    }
  } catch (error) {
    console.error('获取Feed流失败:', error)
    alert(error.message || '获取动态失败，请重试')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 加载更多
function loadMore() {
  if (hasMore.value && !loadingMore.value) {
    pagination.value.page += 1
    fetchFeed(false)
  }
}

// 点赞
function handleLike(item) {
  // TODO: 实现点赞功能（第五批开发）
  console.log('点赞:', item)
}

// 评论
function handleComment(item) {
  // TODO: 实现评论功能（第五批开发）
  const link = getContentLink(item.type, item.content.id)
  router.push(link)
}

// 组件挂载时获取数据
onMounted(() => {
  if (userStore.isLoggedIn) {
    fetchFeed()
  }
})
</script>

<style scoped>
.feed-page {
  min-height: 100vh;
  padding: 40px 0;
}

.feed-container {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 40px;
  align-items: start;
}

.feed-main {
  min-width: 0;
}

.feed-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  border-bottom: 2px solid var(--border);
}

.tab-btn {
  padding: 12px 20px;
  background: transparent;
  border: none;
  font-size: 15px;
  font-weight: 500;
  color: var(--midnight-purple);
  opacity: 0.6;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.tab-btn:hover {
  opacity: 0.8;
}

.tab-btn.active {
  opacity: 1;
  color: var(--coral-pink);
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--coral-pink), var(--stardust-purple));
}

.loading-container,
.empty-container {
  padding: 80px 20px;
  text-align: center;
}

.empty-content {
  max-width: 400px;
  margin: 0 auto;
}

.empty-text {
  font-size: 20px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin-bottom: 12px;
}

.empty-hint {
  font-size: 14px;
  color: var(--text-dark);
  opacity: 0.7;
  margin-bottom: 24px;
}

.feed-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feed-item {
  width: 100%;
}

.feed-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.feed-item-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.feed-item-author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--midnight-purple);
}

.feed-item-time {
  font-size: 12px;
  color: var(--text-dark);
  opacity: 0.6;
}

.feed-item-content {
  margin-bottom: 16px;
}

.feed-item-title {
  margin: 0 0 12px 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--midnight-purple);
  line-height: 1.4;
}

.feed-item-link {
  color: inherit;
  text-decoration: none;
  transition: color 0.2s;
}

.feed-item-link:hover {
  color: var(--coral-pink);
}

.feed-item-summary {
  font-size: 15px;
  color: var(--text-dark);
  opacity: 0.8;
  line-height: 1.6;
  margin: 0 0 16px 0;
}

.feed-item-cover {
  width: 100%;
  max-height: 300px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  margin-top: 16px;
}

.feed-item-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.feed-item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid var(--border);
}

.feed-item-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: var(--text-dark);
  opacity: 0.7;
}

.stat-icon {
  font-size: 16px;
}

.feed-item-actions {
  display: flex;
  gap: 8px;
}

.load-more,
.loading-more {
  margin-top: 32px;
  text-align: center;
}

.feed-sidebar {
  position: sticky;
  top: 100px;
}

.sidebar-card {
  width: 100%;
}

.sidebar-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin: 0;
}

.topic-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.topic-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: rgba(255, 126, 138, 0.05);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.2s;
}

.topic-item:hover {
  background: rgba(255, 126, 138, 0.1);
}

.topic-tag {
  font-size: 14px;
  font-weight: 500;
  color: var(--coral-pink);
}

.topic-count {
  font-size: 12px;
  color: var(--text-dark);
  opacity: 0.6;
}

.creator-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.creator-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.creator-info {
  flex: 1;
}

.creator-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--midnight-purple);
  margin-bottom: 4px;
}

.creator-stats {
  font-size: 12px;
  color: var(--text-dark);
  opacity: 0.6;
}

.mt-20 {
  margin-top: 20px;
}

@media (max-width: 1024px) {
  .feed-container {
    grid-template-columns: 1fr;
  }

  .feed-sidebar {
    position: static;
    margin-top: 40px;
  }
}
</style>

