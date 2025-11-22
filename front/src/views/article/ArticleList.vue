<template>
  <div class="article-list-page">
    <div class="app-shell">
      <div class="article-list-container">
        <!-- 筛选和搜索栏 -->
        <div class="article-list-header">
          <div class="header-left">
            <h1 class="page-title">文章列表</h1>
            <div class="filter-tabs">
              <button
                v-for="tab in statusTabs"
                :key="tab.value"
                :class="['tab-btn', { active: filters.status === tab.value }]"
                @click="handleStatusChange(tab.value)"
              >
                {{ tab.label }}
              </button>
            </div>
          </div>
          <div class="header-right">
            <Input
              v-model="searchKeyword"
              placeholder="搜索文章..."
              class="search-input"
              @keyup.enter="handleSearch"
            >
              <template #prefix>🔍</template>
            </Input>
            <Button variant="primary" to="/articles/create">发布文章</Button>
          </div>
        </div>

        <!-- 排序和筛选 -->
        <div class="article-list-filters">
          <div class="filter-group">
            <label>分类：</label>
            <select v-model="filters.categoryId" @change="handleFilterChange">
              <option :value="null">全部</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                {{ cat.name }}
              </option>
            </select>
          </div>
          <div class="filter-group">
            <label>排序：</label>
            <select v-model="filters.sort" @change="handleFilterChange">
              <option value="latest">最新</option>
              <option value="popular">热门</option>
              <option value="views">阅读量</option>
            </select>
          </div>
        </div>

        <!-- 文章列表 -->
        <div v-if="loading && items.length === 0" class="loading-container">
          <Loading text="加载中..." />
        </div>

        <div v-else-if="items.length === 0" class="empty-container">
          <div class="empty-content">
            <p class="empty-text">暂无文章</p>
            <Button variant="primary" to="/articles/create">发布第一篇文章</Button>
          </div>
        </div>

        <div v-else class="article-list">
          <article
            v-for="article in items"
            :key="article.id"
            class="article-item"
            @click="goToDetail(article.id)"
          >
            <Card hover>
              <div class="article-item-content">
                <div v-if="article.coverImage" class="article-cover">
                  <img :src="article.coverImage" :alt="article.title" />
                </div>
                <div class="article-info">
                  <div class="article-meta">
                    <Badge v-if="article.category" variant="secondary">
                      {{ article.category.name }}
                    </Badge>
                    <span class="article-author">
                      <Avatar
                        :src="article.author.avatar"
                        :name="article.author.nickname"
                        size="small"
                      />
                      <span>{{ article.author.nickname }}</span>
                    </span>
                    <span class="article-time">{{ formatRelativeTime(article.createdAt) }}</span>
                  </div>
                  <h3 class="article-title">{{ article.title }}</h3>
                  <p v-if="article.summary" class="article-summary">{{ article.summary }}</p>
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
                  </div>
                </div>
              </div>
            </Card>
          </article>
        </div>

        <!-- 分页 -->
        <div v-if="hasMore && !loading" class="load-more">
          <Button variant="outline" :loading="loadingMore" @click="loadMore">
            加载更多
          </Button>
        </div>

        <div v-if="loadingMore" class="loading-more">
          <Loading text="加载中..." />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getArticleList } from '@/api/article'
import { formatRelativeTime, formatLargeNumber } from '@/utils/format'
import Card from '@/components/Card.vue'
import Button from '@/components/Button.vue'
import Input from '@/components/Input.vue'
import Badge from '@/components/Badge.vue'
import Avatar from '@/components/Avatar.vue'
import Loading from '@/components/Loading.vue'

const router = useRouter()

// 状态标签
const statusTabs = [
  { label: '全部', value: null },
  { label: '已发布', value: 1 },
  { label: '草稿', value: 0 },
  { label: '私密', value: 2 },
]

// 筛选条件
const filters = reactive({
  status: null,
  categoryId: null,
  tagId: null,
  keyword: '',
  sort: 'latest',
})

const searchKeyword = ref('')
const items = ref([])
const categories = ref([]) // TODO: 从分类API获取
const loading = ref(false)
const loadingMore = ref(false)
const pagination = ref({
  page: 1,
  pageSize: 20,
  total: 0,
  totalPages: 0,
})

const hasMore = computed(() => {
  return pagination.value.page < pagination.value.totalPages
})

// 状态切换
function handleStatusChange(status) {
  filters.status = status
  resetAndFetch()
}

// 筛选变化
function handleFilterChange() {
  resetAndFetch()
}

// 搜索
function handleSearch() {
  filters.keyword = searchKeyword.value
  resetAndFetch()
}

// 重置并获取数据
function resetAndFetch() {
  items.value = []
  pagination.value.page = 1
  fetchArticles()
}

// 获取文章列表
async function fetchArticles(reset = true) {
  if (reset) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const params = {
      page: pagination.value.page,
      pageSize: pagination.value.pageSize,
      sort: filters.sort,
    }

    if (filters.status !== null) {
      params.status = filters.status
    }
    if (filters.categoryId) {
      params.categoryId = filters.categoryId
    }
    if (filters.tagId) {
      params.tagId = filters.tagId
    }
    if (filters.keyword) {
      params.keyword = filters.keyword
    }

    const response = await getArticleList(params)

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
    console.error('获取文章列表失败:', error)
    alert(error.message || '获取文章列表失败，请重试')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 加载更多
function loadMore() {
  if (hasMore.value && !loadingMore.value) {
    pagination.value.page += 1
    fetchArticles(false)
  }
}

// 跳转到详情页
function goToDetail(id) {
  router.push(`/articles/${id}`)
}

// 组件挂载时获取数据
onMounted(() => {
  fetchArticles()
})
</script>

<style scoped>
.article-list-page {
  min-height: 100vh;
  padding: 40px 0;
}

.article-list-container {
  max-width: 1200px;
  margin: 0 auto;
}

.article-list-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  gap: 20px;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--midnight-purple);
  margin: 0 0 16px 0;
}

.filter-tabs {
  display: flex;
  gap: 8px;
}

.tab-btn {
  padding: 8px 16px;
  background: transparent;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-dark);
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn:hover {
  border-color: var(--coral-pink);
  color: var(--coral-pink);
}

.tab-btn.active {
  background: var(--coral-pink);
  border-color: var(--coral-pink);
  color: white;
}

.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 300px;
}

.article-list-filters {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: var(--radius-sm);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-group label {
  font-size: 14px;
  color: var(--text-dark);
  font-weight: 500;
}

.filter-group select {
  padding: 8px 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-dark);
  background: white;
  cursor: pointer;
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
  margin-bottom: 24px;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.article-item {
  cursor: pointer;
}

.article-item-content {
  display: flex;
  gap: 20px;
}

.article-cover {
  flex-shrink: 0;
  width: 200px;
  height: 150px;
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.article-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.article-author {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-dark);
  opacity: 0.8;
}

.article-time {
  font-size: 12px;
  color: var(--text-dark);
  opacity: 0.6;
}

.article-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin: 0;
  line-height: 1.4;
}

.article-summary {
  font-size: 15px;
  color: var(--text-dark);
  opacity: 0.8;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.article-tag {
  font-size: 12px;
  color: var(--stardust-purple);
  padding: 4px 8px;
  background: rgba(166, 140, 224, 0.1);
  border-radius: 4px;
}

.article-stats {
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

.load-more,
.loading-more {
  margin-top: 32px;
  text-align: center;
}

@media (max-width: 768px) {
  .article-list-header {
    flex-direction: column;
  }

  .header-right {
    width: 100%;
  }

  .search-input {
    flex: 1;
  }

  .article-item-content {
    flex-direction: column;
  }

  .article-cover {
    width: 100%;
    height: 200px;
  }
}
</style>

