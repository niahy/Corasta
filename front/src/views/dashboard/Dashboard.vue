<template>
  <div class="dashboard-page">
    <div class="app-shell">
      <!-- 页面头部 -->
      <header class="dashboard-header">
        <div class="header-content">
          <div class="user-info">
            <Avatar
              :src="userStore.userInfo?.avatar"
              :name="userStore.userInfo?.nickname"
              size="large"
            />
            <div class="user-details">
              <Badge variant="primary">创作者 · @{{ userStore.userInfo?.nickname }}</Badge>
              <h1 class="page-title">内容管理后台</h1>
              <div class="user-stats">
                <span>文章 {{ statistics.articleCount || 0 }}</span>
                <span>问答 {{ statistics.questionCount || 0 }}</span>
                <span>粉丝 {{ userStore.userInfo?.followerCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </header>

      <!-- 标签页切换 -->
      <div class="dashboard-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          :class="['tab-btn', { active: activeTab === tab.value }]"
          @click="handleTabChange(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- 内容管理仪表盘 -->
      <div v-show="activeTab === 'content'" class="tab-content">
        <!-- 统计卡片 -->
        <div class="stats-grid">
          <Card class="stat-card">
            <div class="stat-content">
              <div class="stat-label">本周发布</div>
              <div class="stat-value">{{ weeklyPublishCount }}</div>
            </div>
          </Card>
          <Card class="stat-card">
            <div class="stat-content">
              <div class="stat-label">总访问量</div>
              <div class="stat-value">{{ formatLargeNumber(statistics.totalViewCount || 0) }}</div>
            </div>
          </Card>
          <Card class="stat-card">
            <div class="stat-content">
              <div class="stat-label">总互动数</div>
              <div class="stat-value">
                {{ formatLargeNumber((statistics.totalLikeCount || 0) + (statistics.totalCommentCount || 0)) }}
              </div>
            </div>
          </Card>
          <Card class="stat-card">
            <div class="stat-content">
              <div class="stat-label">待办事项</div>
              <div class="stat-value">{{ todoCount }}</div>
            </div>
          </Card>
        </div>

        <!-- 最近内容 -->
        <div class="dashboard-section">
          <Card>
            <template #header>
              <div class="section-header">
                <h3 class="section-title">最近发布</h3>
                <Button variant="outline" size="small" to="/articles/create">新建内容</Button>
              </div>
            </template>
            <div v-if="contentLoading" class="loading-container">
              <Loading text="加载中..." />
            </div>
            <div v-else-if="recentContents.length === 0" class="empty-container">
              <p class="empty-text">暂无内容，开始创作吧~</p>
              <Button variant="primary" to="/articles/create" class="mt-16">发布第一篇文章</Button>
            </div>
            <div v-else class="recent-contents">
              <div
                v-for="item in recentContents"
                :key="`${item.type}-${item.id}`"
                class="content-item"
                @click="goToContent(item)"
              >
                <div class="content-item-main">
                  <Badge :variant="item.type === 'article' ? 'primary' : 'secondary'">
                    {{ item.type === 'article' ? '文章' : item.type === 'question' ? '问答' : '回答' }}
                  </Badge>
                  <div class="content-item-info">
                    <h4 class="content-item-title">{{ item.title }}</h4>
                    <div class="content-item-meta">
                      <span>{{ formatRelativeTime(item.createdAt) }}</span>
                      <span v-if="item.viewCount !== undefined">·</span>
                      <span v-if="item.viewCount !== undefined">阅读 {{ formatLargeNumber(item.viewCount) }}</span>
                      <span v-if="item.answerCount !== undefined">·</span>
                      <span v-if="item.answerCount !== undefined">回答 {{ formatLargeNumber(item.answerCount) }}</span>
                    </div>
                  </div>
                </div>
                <Button variant="text" size="small" @click.stop="handleEdit(item)">编辑</Button>
              </div>
            </div>
          </Card>
        </div>

        <!-- 我的内容列表 -->
        <div class="dashboard-section">
          <Card>
            <template #header>
              <div class="section-header">
                <h3 class="section-title">我的内容</h3>
                <div class="content-filters">
                  <select v-model="contentFilter" @change="handleContentFilterChange">
                    <option value="all">全部</option>
                    <option value="articles">文章</option>
                    <option value="questions">问答</option>
                    <option value="answers">回答</option>
                  </select>
                </div>
              </div>
            </template>
            <div v-if="myContentsLoading" class="loading-container">
              <Loading text="加载中..." />
            </div>
            <div v-else-if="myContents.length === 0" class="empty-container">
              <p class="empty-text">暂无内容</p>
            </div>
            <div v-else class="contents-table">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>标题</th>
                    <th>类型</th>
                    <th>状态</th>
                    <th>数据</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="item in myContents"
                    :key="`${item.type}-${item.id}`"
                    class="table-row"
                    @click="goToContent(item)"
                  >
                    <td class="table-title">{{ item.title }}</td>
                    <td>
                      <Badge :variant="item.type === 'article' ? 'primary' : 'secondary'">
                        {{ item.type === 'article' ? '文章' : item.type === 'question' ? '问答' : '回答' }}
                      </Badge>
                    </td>
                    <td>
                      <Badge
                        :variant="getStatusBadgeVariant(item.status)"
                        size="small"
                      >
                        {{ getStatusLabel(item.status) }}
                      </Badge>
                    </td>
                    <td class="table-stats">
                      <span v-if="item.viewCount !== undefined">
                        阅读 {{ formatLargeNumber(item.viewCount) }}
                      </span>
                      <span v-if="item.answerCount !== undefined">
                        回答 {{ formatLargeNumber(item.answerCount) }}
                      </span>
                      <span v-if="item.likeCount !== undefined">
                        点赞 {{ formatLargeNumber(item.likeCount) }}
                      </span>
                    </td>
                    <td class="table-actions">
                      <Button variant="text" size="small" @click.stop="handleEdit(item)">编辑</Button>
                      <Button variant="text" size="small" class="danger" @click.stop="handleDelete(item)">
                        删除
                      </Button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-if="hasMoreContents && !myContentsLoading" class="load-more">
              <Button variant="outline" :loading="loadingMoreContents" @click="loadMoreContents">
                加载更多
              </Button>
            </div>
          </Card>
        </div>
      </div>

      <!-- 数据分析仪表盘 -->
      <div v-show="activeTab === 'analytics'" class="tab-content">
        <!-- 核心指标 -->
        <div class="stats-grid">
          <Card class="stat-card">
            <div class="stat-content">
              <div class="stat-label">总访问量</div>
              <div class="stat-value">{{ formatLargeNumber(analytics.overview?.totalViews || 0) }}</div>
            </div>
          </Card>
          <Card class="stat-card">
            <div class="stat-content">
              <div class="stat-label">总点赞数</div>
              <div class="stat-value">{{ formatLargeNumber(analytics.overview?.totalLikes || 0) }}</div>
            </div>
          </Card>
          <Card class="stat-card">
            <div class="stat-content">
              <div class="stat-label">总评论数</div>
              <div class="stat-value">{{ formatLargeNumber(analytics.overview?.totalComments || 0) }}</div>
            </div>
          </Card>
          <Card class="stat-card">
            <div class="stat-content">
              <div class="stat-label">总收藏数</div>
              <div class="stat-value">{{ formatLargeNumber(analytics.overview?.totalFavorites || 0) }}</div>
            </div>
          </Card>
        </div>

        <!-- 趋势分析 -->
        <div class="dashboard-section">
          <Card>
            <template #header>
              <div class="section-header">
                <h3 class="section-title">内容趋势</h3>
                <div class="trend-tabs">
                  <button
                    :class="['trend-tab-btn', { active: trendPeriod === '7d' }]"
                    @click="trendPeriod = '7d'; fetchAnalytics()"
                  >
                    7 天
                  </button>
                  <button
                    :class="['trend-tab-btn', { active: trendPeriod === '30d' }]"
                    @click="trendPeriod = '30d'; fetchAnalytics()"
                  >
                    30 天
                  </button>
                </div>
              </div>
            </template>
            <div v-if="analyticsLoading" class="loading-container">
              <Loading text="加载中..." />
            </div>
            <div v-else class="trend-chart">
              <div class="chart-placeholder">
                <p class="chart-note">趋势图（待集成图表库）</p>
                <div class="chart-bars">
                  <div
                    v-for="(item, index) in trendData"
                    :key="index"
                    class="chart-bar"
                    :style="{ height: `${(item.count / maxTrendValue) * 100}%` }"
                  ></div>
                </div>
              </div>
            </div>
          </Card>
        </div>

        <!-- 内容排行 -->
        <div class="dashboard-section">
          <Card>
            <template #header>
              <h3 class="section-title">热门内容排行</h3>
            </template>
            <div v-if="analyticsLoading" class="loading-container">
              <Loading text="加载中..." />
            </div>
            <div v-else class="top-content-list">
              <div class="top-content-section">
                <h4 class="top-content-title">热门文章</h4>
                <div v-if="analytics.topContent?.articles?.length === 0" class="empty-content">
                  <p>暂无数据</p>
                </div>
                <div v-else class="top-content-items">
                  <div
                    v-for="(article, index) in analytics.topContent?.articles"
                    :key="`article-${article.id}`"
                    class="top-content-item"
                    @click="goToArticle(article.id)"
                  >
                    <span class="rank">{{ index + 1 }}</span>
                    <div class="top-content-info">
                      <h5 class="top-content-name">{{ article.title }}</h5>
                      <div class="top-content-stats">
                        <span>阅读 {{ formatLargeNumber(article.viewCount) }}</span>
                        <span>点赞 {{ formatLargeNumber(article.likeCount) }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="top-content-section">
                <h4 class="top-content-title">热门问答</h4>
                <div v-if="analytics.topContent?.questions?.length === 0" class="empty-content">
                  <p>暂无数据</p>
                </div>
                <div v-else class="top-content-items">
                  <div
                    v-for="(question, index) in analytics.topContent?.questions"
                    :key="`question-${question.id}`"
                    class="top-content-item"
                    @click="goToQuestion(question.id)"
                  >
                    <span class="rank">{{ index + 1 }}</span>
                    <div class="top-content-info">
                      <h5 class="top-content-name">{{ question.title }}</h5>
                      <div class="top-content-stats">
                        <span>回答 {{ formatLargeNumber(question.answerCount) }}</span>
                        <span>关注 {{ formatLargeNumber(question.followerCount) }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getDashboardContent, getMyContents, getDashboardAnalytics } from '@/api/dashboard'
import { deleteArticle } from '@/api/article'
import { deleteQuestion } from '@/api/question'
import { deleteAnswer } from '@/api/question'
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
  { label: '内容管理', value: 'content' },
  { label: '数据分析', value: 'analytics' },
]

const activeTab = ref('content')
const trendPeriod = ref('7d')

// 统计数据
const statistics = reactive({
  articleCount: 0,
  questionCount: 0,
  answerCount: 0,
  totalViewCount: 0,
  totalLikeCount: 0,
  totalCommentCount: 0,
})

// 最近内容
const recentContents = ref([])
const contentLoading = ref(false)

// 我的内容列表
const myContents = ref([])
const myContentsLoading = ref(false)
const loadingMoreContents = ref(false)
const contentFilter = ref('all')
const contentsPagination = ref({
  page: 1,
  pageSize: 20,
  total: 0,
  totalPages: 0,
})

const hasMoreContents = computed(() => {
  return contentsPagination.value.page < contentsPagination.value.totalPages
})

// 数据分析
const analytics = reactive({
  overview: {
    totalViews: 0,
    totalLikes: 0,
    totalComments: 0,
    totalFavorites: 0,
  },
  trends: {
    views: [],
    likes: [],
    comments: [],
  },
  topContent: {
    articles: [],
    questions: [],
    answers: [],
  },
})
const analyticsLoading = ref(false)

// 计算属性
const weeklyPublishCount = computed(() => {
  // 计算本周发布的内容数量
  const now = new Date()
  const weekStart = new Date(now.setDate(now.getDate() - now.getDay()))
  weekStart.setHours(0, 0, 0, 0)

  return recentContents.value.filter((item) => {
    const itemDate = new Date(item.createdAt)
    return itemDate >= weekStart
  }).length
})

const todoCount = computed(() => {
  // 计算待办事项（未回答的问题等）
  return 0 // TODO: 实现待办事项计算
})

const trendData = computed(() => {
  return analytics.trends.views || []
})

const maxTrendValue = computed(() => {
  if (trendData.value.length === 0) return 1
  return Math.max(...trendData.value.map((item) => item.count))
})

// 获取内容管理仪表盘
async function fetchDashboardOverview() {
  contentLoading.value = true
  try {
    const response = await getDashboardContent()
    if (response.data) {
      Object.assign(statistics, response.data.statistics || {})
      // 合并最近内容
      recentContents.value = [
        ...(response.data.recentArticles || []).map((item) => ({ ...item, type: 'article' })),
        ...(response.data.recentQuestions || []).map((item) => ({ ...item, type: 'question' })),
        ...(response.data.recentAnswers || []).map((item) => ({ ...item, type: 'answer' })),
      ].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    }
  } catch (error) {
    console.error('获取内容管理仪表盘失败:', error)
    alert(error.message || '获取数据失败，请重试')
  } finally {
    contentLoading.value = false
  }
}

// 获取我的内容列表
async function fetchMyContents(reset = true) {
  if (reset) {
    myContentsLoading.value = true
  } else {
    loadingMoreContents.value = true
  }

  try {
    const params = {
      page: contentsPagination.value.page,
      pageSize: contentsPagination.value.pageSize,
      type: contentFilter.value === 'all' ? undefined : contentFilter.value,
    }

    const response = await getMyContents(params)

    if (response.data) {
      if (reset) {
        myContents.value = response.data.items || []
      } else {
        myContents.value.push(...(response.data.items || []))
      }
      contentsPagination.value = {
        ...contentsPagination.value,
        ...response.data.pagination,
      }
    }
  } catch (error) {
    console.error('获取我的内容列表失败:', error)
    alert(error.message || '获取内容列表失败，请重试')
  } finally {
    myContentsLoading.value = false
    loadingMoreContents.value = false
  }
}

// 获取数据分析
async function fetchAnalytics() {
  analyticsLoading.value = true
  try {
    const now = new Date()
    const startDate = new Date()
    if (trendPeriod.value === '7d') {
      startDate.setDate(now.getDate() - 7)
    } else {
      startDate.setDate(now.getDate() - 30)
    }

    const response = await getDashboardAnalytics({
      startDate: startDate.toISOString().split('T')[0],
      endDate: now.toISOString().split('T')[0],
    })

    if (response.data) {
      Object.assign(analytics.overview, response.data.overview || {})
      Object.assign(analytics.trends, response.data.trends || {})
      Object.assign(analytics.topContent, response.data.topContent || {})
    }
  } catch (error) {
    console.error('获取数据分析失败:', error)
    alert(error.message || '获取数据分析失败，请重试')
  } finally {
    analyticsLoading.value = false
  }
}

// 标签切换
function handleTabChange(tab) {
  activeTab.value = tab
  if (tab === 'analytics') {
    fetchAnalytics()
  }
}

// 内容筛选变化
function handleContentFilterChange() {
  contentsPagination.value.page = 1
  fetchMyContents()
}

// 加载更多
function loadMoreContents() {
  if (hasMoreContents.value && !loadingMoreContents.value) {
    contentsPagination.value.page += 1
    fetchMyContents(false)
  }
}

// 获取状态标签
function getStatusLabel(status) {
  const statusMap = {
    0: '草稿',
    1: '已发布',
    2: '私密',
  }
  return statusMap[status] || '未知'
}

function getStatusBadgeVariant(status) {
  const variantMap = {
    0: 'outline',
    1: 'primary',
    2: 'secondary',
  }
  return variantMap[status] || 'outline'
}

// 编辑内容
function handleEdit(item) {
  if (item.type === 'article') {
    router.push({ name: 'article-edit', params: { id: item.id } })
  } else if (item.type === 'question') {
    router.push({ name: 'question-edit', params: { id: item.id } })
  } else {
    alert('回答编辑功能开发中...')
  }
}

// 删除内容
async function handleDelete(item) {
  if (!confirm(`确定要删除"${item.title}"吗？`)) {
    return
  }

  try {
    if (item.type === 'article') {
      await deleteArticle(item.id)
    } else if (item.type === 'question') {
      await deleteQuestion(item.id)
    } else if (item.type === 'answer') {
      await deleteAnswer(item.id)
    }
    alert('删除成功')
    // 刷新列表
    fetchMyContents()
    fetchDashboardOverview()
  } catch (error) {
    console.error('删除失败:', error)
    alert(error.message || '删除失败，请重试')
  }
}

// 跳转到内容
function goToContent(item) {
  if (item.type === 'article') {
    goToArticle(item.id)
  } else if (item.type === 'question') {
    goToQuestion(item.id)
  } else {
    // 回答跳转到问题详情
    goToQuestion(item.questionId || item.id)
  }
}

function goToArticle(id) {
  router.push({ name: 'article-detail', params: { id } })
}

function goToQuestion(id) {
  router.push({ name: 'question-detail', params: { id } })
}

// 组件挂载时获取数据
onMounted(() => {
  fetchDashboardOverview()
  fetchMyContents()
})
</script>

<style scoped>
.dashboard-page {
  min-height: 100vh;
  padding: 40px 0;
}

.dashboard-header {
  margin-bottom: 32px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.user-info {
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.user-details {
  flex: 1;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--midnight-purple);
  margin: 12px 0 16px 0;
}

.user-stats {
  display: flex;
  gap: 24px;
  font-size: 15px;
  color: var(--text-dark);
  opacity: 0.8;
}

.dashboard-tabs {
  display: flex;
  gap: 8px;
  border-bottom: 2px solid var(--border);
  margin-bottom: 32px;
}

.tab-btn {
  padding: 12px 24px;
  background: transparent;
  border: none;
  font-size: 16px;
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

.tab-content {
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  padding: 24px;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-dark);
  opacity: 0.7;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--coral-pink);
}

.dashboard-section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin: 0;
}

.loading-container,
.empty-container {
  padding: 60px 20px;
  text-align: center;
}

.empty-text {
  font-size: 16px;
  color: var(--text-dark);
  opacity: 0.6;
}

.recent-contents {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.content-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
}

.content-item:hover {
  border-color: var(--coral-pink);
  background: rgba(255, 126, 138, 0.05);
}

.content-item-main {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.content-item-info {
  flex: 1;
}

.content-item-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin-bottom: 4px;
}

.content-item-meta {
  font-size: 13px;
  color: var(--text-dark);
  opacity: 0.7;
}

.content-filters {
  display: flex;
  gap: 12px;
}

.content-filters select {
  padding: 6px 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-dark);
  background: white;
  cursor: pointer;
  outline: none;
}

.contents-table {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table thead {
  background: rgba(166, 140, 224, 0.1);
}

.data-table th {
  padding: 12px 16px;
  text-align: left;
  font-size: 14px;
  font-weight: 600;
  color: var(--midnight-purple);
  border-bottom: 2px solid var(--border);
}

.data-table td {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
}

.table-row {
  cursor: pointer;
  transition: background-color 0.2s;
}

.table-row:hover {
  background: rgba(255, 126, 138, 0.05);
}

.table-title {
  font-weight: 500;
  color: var(--midnight-purple);
}

.table-stats {
  font-size: 13px;
  color: var(--text-dark);
  opacity: 0.7;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.table-actions .danger {
  color: #ff4757;
}

.load-more {
  margin-top: 24px;
  text-align: center;
}

.trend-tabs {
  display: flex;
  gap: 8px;
}

.trend-tab-btn {
  padding: 6px 12px;
  background: transparent;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-dark);
  cursor: pointer;
  transition: all 0.2s;
}

.trend-tab-btn:hover {
  border-color: var(--coral-pink);
  color: var(--coral-pink);
}

.trend-tab-btn.active {
  background: var(--coral-pink);
  border-color: var(--coral-pink);
  color: white;
}

.trend-chart {
  padding: 20px;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(166, 140, 224, 0.05);
  border-radius: var(--radius-sm);
}

.chart-note {
  font-size: 14px;
  color: var(--text-dark);
  opacity: 0.6;
  margin-bottom: 20px;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  height: 200px;
  width: 100%;
  padding: 0 20px;
}

.chart-bar {
  flex: 1;
  background: linear-gradient(to top, var(--coral-pink), var(--stardust-purple));
  border-radius: 4px 4px 0 0;
  min-height: 4px;
  transition: height 0.3s;
}

.top-content-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
}

.top-content-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.top-content-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin: 0;
}

.empty-content {
  padding: 40px 20px;
  text-align: center;
  color: var(--text-dark);
  opacity: 0.6;
}

.top-content-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.top-content-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
}

.top-content-item:hover {
  border-color: var(--coral-pink);
  background: rgba(255, 126, 138, 0.05);
}

.rank {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: var(--coral-pink);
  color: white;
  border-radius: 50%;
  font-weight: 600;
  flex-shrink: 0;
}

.top-content-info {
  flex: 1;
}

.top-content-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--midnight-purple);
  margin-bottom: 4px;
}

.top-content-stats {
  font-size: 13px;
  color: var(--text-dark);
  opacity: 0.7;
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .user-info {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .top-content-list {
    grid-template-columns: 1fr;
  }

  .contents-table {
    font-size: 12px;
  }

  .data-table th,
  .data-table td {
    padding: 8px;
  }
}
</style>

