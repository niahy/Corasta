<template>
  <div class="question-list-page">
    <div class="app-shell">
      <div class="question-list-container">
        <!-- 筛选和搜索栏 -->
        <div class="question-list-header">
          <div class="header-left">
            <h1 class="page-title">问答</h1>
            <div class="filter-tabs">
              <button
                v-for="tab in sortTabs"
                :key="tab.value"
                :class="['tab-btn', { active: filters.sort === tab.value }]"
                @click="handleSortChange(tab.value)"
              >
                {{ tab.label }}
              </button>
            </div>
          </div>
          <div class="header-right">
            <Input
              v-model="searchKeyword"
              placeholder="搜索问题..."
              class="search-input"
              @keyup.enter="handleSearch"
            >
              <template #prefix>🔍</template>
            </Input>
            <Button variant="primary" to="/questions/create">提问</Button>
          </div>
        </div>

        <!-- 筛选 -->
        <div class="question-list-filters">
          <div class="filter-group">
            <label>标签：</label>
            <select v-model="filters.tagId" @change="handleFilterChange">
              <option :value="null">全部</option>
              <option v-for="tag in tags" :key="tag.id" :value="tag.id">
                {{ tag.name }}
              </option>
            </select>
          </div>
          <div class="filter-group">
            <label>提问者：</label>
            <Input
              v-model="filters.userId"
              type="number"
              placeholder="用户ID"
              class="filter-input"
              @keyup.enter="handleFilterChange"
            />
          </div>
        </div>

        <!-- 问题列表 -->
        <div v-if="loading && items.length === 0" class="loading-container">
          <Loading text="加载中..." />
        </div>

        <div v-else-if="items.length === 0" class="empty-container">
          <div class="empty-content">
            <p class="empty-text">暂无问题</p>
            <Button variant="primary" to="/questions/create">提出第一个问题</Button>
          </div>
        </div>

        <div v-else class="question-list">
          <article
            v-for="question in items"
            :key="question.id"
            class="question-item"
            @click="goToDetail(question.id)"
          >
            <Card hover>
              <div class="question-item-content">
                <div class="question-header">
                  <div class="question-meta">
                    <Avatar
                      :src="question.author.avatar"
                      :name="question.author.nickname"
                      size="small"
                    />
                    <span class="question-author">{{ question.author.nickname }}</span>
                    <span class="question-time">{{ formatRelativeTime(question.createdAt) }}</span>
                  </div>
                  <div v-if="question.relatedArticle" class="question-related">
                    <Badge variant="outline">关联文章</Badge>
                  </div>
                </div>
                <h3 class="question-title">{{ question.title }}</h3>
                <p v-if="question.description" class="question-description">
                  {{ question.description }}
                </p>
                <div class="question-footer">
                  <div class="question-tags">
                    <Badge
                      v-for="tag in question.tags"
                      :key="tag.id"
                      variant="secondary"
                      size="small"
                    >
                      {{ tag.name }}
                    </Badge>
                  </div>
                  <div class="question-stats">
                    <span class="stat-item">
                      <span class="stat-icon">💬</span>
                      {{ formatLargeNumber(question.answerCount) }} 回答
                    </span>
                    <span class="stat-item">
                      <span class="stat-icon">👁️</span>
                      {{ formatLargeNumber(question.followerCount) }} 关注
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
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getQuestionList } from '@/api/question'
import { formatRelativeTime, formatLargeNumber } from '@/utils/format'
import Card from '@/components/Card.vue'
import Button from '@/components/Button.vue'
import Input from '@/components/Input.vue'
import Badge from '@/components/Badge.vue'
import Avatar from '@/components/Avatar.vue'
import Loading from '@/components/Loading.vue'

const router = useRouter()

// 排序选项
const sortTabs = [
  { label: '最新', value: 'latest' },
  { label: '热门', value: 'popular' },
  { label: '回答数', value: 'answers' },
]

const items = ref([])
const tags = ref([]) // TODO: 从API获取标签列表
const searchKeyword = ref('')
const loading = ref(false)
const loadingMore = ref(false)
const filters = ref({
  sort: 'latest',
  tagId: null,
  userId: null,
  keyword: '',
})
const pagination = ref({
  page: 1,
  pageSize: 20,
  total: 0,
  totalPages: 0,
})

const hasMore = computed(() => {
  return pagination.value.page < pagination.value.totalPages
})

// 获取问题列表
async function fetchQuestions(reset = true) {
  if (reset) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const params = {
      page: pagination.value.page,
      pageSize: pagination.value.pageSize,
      sort: filters.value.sort,
      keyword: filters.value.keyword || undefined,
      tagId: filters.value.tagId || undefined,
      userId: filters.value.userId || undefined,
    }

    const response = await getQuestionList(params)

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
    console.error('获取问题列表失败:', error)
    alert(error.message || '获取问题列表失败，请重试')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 排序切换
function handleSortChange(sort) {
  filters.value.sort = sort
  resetAndFetch()
}

// 筛选变化
function handleFilterChange() {
  resetAndFetch()
}

// 搜索
function handleSearch() {
  filters.value.keyword = searchKeyword.value
  resetAndFetch()
}

// 重置并获取
function resetAndFetch() {
  items.value = []
  pagination.value.page = 1
  fetchQuestions()
}

// 加载更多
function loadMore() {
  if (hasMore.value && !loadingMore.value) {
    pagination.value.page += 1
    fetchQuestions(false)
  }
}

// 跳转到详情
function goToDetail(id) {
  router.push({ name: 'question-detail', params: { id } })
}

// 组件挂载时获取数据
onMounted(() => {
  fetchQuestions()
})
</script>

<style scoped>
.question-list-page {
  min-height: 100vh;
  padding: 40px 0;
}

.question-list-container {
  max-width: 1200px;
  margin: 0 auto;
}

.question-list-header {
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
  margin-bottom: 16px;
}

.filter-tabs {
  display: flex;
  gap: 8px;
  border-bottom: 2px solid var(--border);
}

.tab-btn {
  padding: 10px 20px;
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

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 300px;
}

.question-list-filters {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border);
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
  padding: 6px 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-dark);
  background: white;
  cursor: pointer;
  outline: none;
}

.filter-input {
  width: 120px;
}

.loading-container,
.empty-container {
  padding: 80px 20px;
  text-align: center;
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.empty-text {
  font-size: 16px;
  color: var(--text-dark);
  opacity: 0.6;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.question-item {
  cursor: pointer;
}

.question-item-content {
  padding: 20px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.question-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: var(--text-dark);
  opacity: 0.7;
}

.question-author {
  font-weight: 500;
}

.question-time {
  font-size: 13px;
}

.question-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin-bottom: 12px;
  line-height: 1.4;
}

.question-description {
  font-size: 15px;
  color: var(--text-dark);
  opacity: 0.8;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.question-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.question-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.question-stats {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: var(--text-dark);
  opacity: 0.7;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-icon {
  font-size: 16px;
}

.load-more {
  margin-top: 32px;
  text-align: center;
}

@media (max-width: 768px) {
  .question-list-header {
    flex-direction: column;
  }

  .header-right {
    width: 100%;
  }

  .search-input {
    width: 100%;
  }

  .question-list-filters {
    flex-direction: column;
    gap: 12px;
  }

  .question-footer {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

