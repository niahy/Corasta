<template>
  <div class="search-page">
    <div class="app-shell">
      <header class="search-header">
        <h1 class="page-title">全站搜索</h1>
        <p class="page-description">覆盖文章、问答、用户，支持后端的分页与筛选参数</p>
        <div class="search-input-wrapper">
          <Input
            v-model="searchKeyword"
            placeholder="输入关键词，如：浪漫台词 / 星轨摄影"
            class="search-input"
            @keyup.enter="handleSearch"
            @input="handleInputChange"
          >
            <template #prefix>🔍</template>
            <template #suffix>
              <Button variant="primary" size="small" @click="handleSearch">搜索</Button>
            </template>
          </Input>
          <!-- 搜索建议 -->
          <div v-if="showSuggestions && suggestions.length > 0" class="suggestions-dropdown">
            <div
              v-for="(suggestion, index) in suggestions"
              :key="index"
              class="suggestion-item"
              @click="selectSuggestion(suggestion)"
            >
              {{ suggestion }}
            </div>
          </div>
        </div>

        <!-- 热门搜索词 -->
        <div v-if="!hasSearched && hotKeywords.length > 0" class="hot-keywords">
          <span class="hot-label">热门搜索：</span>
          <button
            v-for="(keyword, index) in hotKeywords"
            :key="index"
            class="hot-keyword-btn"
            @click="selectHotKeyword(keyword.keyword)"
          >
            {{ keyword.keyword }}
            <span class="hot-count">({{ keyword.count }})</span>
          </button>
        </div>

        <!-- 搜索历史 -->
        <div v-if="!hasSearched && searchHistory.length > 0" class="search-history">
          <div class="history-header">
            <span class="history-label">搜索历史</span>
            <Button variant="text" size="small" @click="clearHistory">清除历史</Button>
          </div>
          <div class="history-list">
            <button
              v-for="(keyword, index) in searchHistory"
              :key="index"
              class="history-item"
              @click="selectSuggestion(keyword)"
            >
              {{ keyword }}
            </button>
          </div>
        </div>

        <!-- 内容类型标签 -->
        <div v-if="hasSearched" class="content-tabs">
          <button
            v-for="tab in updatedTabs"
            :key="tab.value"
            :class="['tab-btn', { active: activeTab === tab.value }]"
            @click="handleTabChange(tab.value)"
          >
            {{ tab.label }}
            <span v-if="tab.count !== undefined" class="tab-count">({{ tab.count }})</span>
          </button>
        </div>
      </header>

      <div v-if="hasSearched" class="search-content">
        <div class="dual-column">
          <!-- 搜索结果 -->
          <div class="search-results">
            <!-- 文章结果 -->
            <div v-show="activeTab === 'all' || activeTab === 'articles'" class="result-section">
              <h3 class="result-title">文章 · {{ results.articles?.total || 0 }}</h3>
              <div v-if="loading && results.articles?.items?.length === 0" class="loading-container">
                <Loading text="加载中..." />
              </div>
              <div v-else-if="results.articles?.items?.length === 0" class="empty-result">
                <p>暂无文章结果</p>
              </div>
              <div v-else class="result-list">
                <article
                  v-for="item in results.articles?.items"
                  :key="`article-${item.id}`"
                  class="result-item"
                  @click="goToArticle(item.id)"
                >
                  <Card hover>
                    <div class="result-item-content">
                      <h4 class="result-item-title">{{ item.title }}</h4>
                      <p v-if="item.summary" class="result-item-summary">{{ item.summary }}</p>
                      <div class="result-item-meta">
                        <Avatar
                          :src="item.author?.avatar"
                          :name="item.author?.nickname"
                          size="small"
                        />
                        <span>{{ item.author?.nickname }}</span>
                        <span>·</span>
                        <span>{{ formatRelativeTime(item.createdAt) }}</span>
                        <span>·</span>
                        <span>{{ formatLargeNumber(item.viewCount) }} 阅读</span>
                      </div>
                    </div>
                  </Card>
                </article>
              </div>
            </div>

            <!-- 问答结果 -->
            <div v-show="activeTab === 'all' || activeTab === 'questions'" class="result-section">
              <h3 class="result-title">问答 · {{ results.questions?.total || 0 }}</h3>
              <div v-if="loading && results.questions?.items?.length === 0" class="loading-container">
                <Loading text="加载中..." />
              </div>
              <div v-else-if="results.questions?.items?.length === 0" class="empty-result">
                <p>暂无问答结果</p>
              </div>
              <div v-else class="result-list">
                <article
                  v-for="item in results.questions?.items"
                  :key="`question-${item.id}`"
                  class="result-item"
                  @click="goToQuestion(item.id)"
                >
                  <Card hover>
                    <div class="result-item-content">
                      <h4 class="result-item-title">{{ item.title }}</h4>
                      <p v-if="item.description" class="result-item-summary">{{ item.description }}</p>
                      <div class="result-item-meta">
                        <Avatar
                          :src="item.author?.avatar"
                          :name="item.author?.nickname"
                          size="small"
                        />
                        <span>{{ item.author?.nickname }}</span>
                        <span>·</span>
                        <span>{{ formatRelativeTime(item.createdAt) }}</span>
                        <span>·</span>
                        <span>{{ formatLargeNumber(item.answerCount) }} 回答</span>
                      </div>
                    </div>
                  </Card>
                </article>
              </div>
            </div>

            <!-- 用户结果 -->
            <div v-show="activeTab === 'all' || activeTab === 'users'" class="result-section">
              <h3 class="result-title">用户 · {{ results.users?.total || 0 }}</h3>
              <div v-if="loading && results.users?.items?.length === 0" class="loading-container">
                <Loading text="加载中..." />
              </div>
              <div v-else-if="results.users?.items?.length === 0" class="empty-result">
                <p>暂无用户结果</p>
              </div>
              <div v-else class="result-list">
                <article
                  v-for="item in results.users?.items"
                  :key="`user-${item.id}`"
                  class="result-item"
                  @click="goToUserProfile(item.id)"
                >
                  <Card hover>
                    <div class="result-item-content">
                      <div class="user-result-header">
                        <Avatar
                          :src="item.avatar"
                          :name="item.nickname"
                          size="medium"
                        />
                        <div class="user-info">
                          <h4 class="result-item-title">{{ item.nickname }}</h4>
                          <p v-if="item.bio" class="user-bio">{{ item.bio }}</p>
                        </div>
                        <Button
                          v-if="userStore.isLoggedIn && item.id !== userStore.userInfo?.id"
                          :variant="item.isFollowing ? 'primary' : 'outline'"
                          size="small"
                          @click.stop="handleFollow(item)"
                        >
                          {{ item.isFollowing ? '已关注' : '关注' }}
                        </Button>
                      </div>
                      <div class="user-stats">
                        <span>文章 {{ formatLargeNumber(item.articleCount || 0) }}</span>
                        <span>问答 {{ formatLargeNumber(item.questionCount || 0) }}</span>
                        <span>粉丝 {{ formatLargeNumber(item.followerCount || 0) }}</span>
                      </div>
                    </div>
                  </Card>
                </article>
              </div>
            </div>

            <!-- 空结果 -->
            <div v-if="!loading && totalResults === 0" class="empty-search">
              <p>未找到相关结果，试试其他关键词吧~</p>
            </div>
          </div>

          <!-- 筛选侧边栏 -->
          <aside class="filter-sidebar">
            <Card>
              <template #header>
                <h3 class="filter-title">筛选条件</h3>
              </template>
              <div class="filter-content">
                <div class="filter-group">
                  <label>排序方式</label>
                  <select v-model="filters.sort" @change="handleFilterChange">
                    <option value="relevance">相关度</option>
                    <option value="latest">最新</option>
                    <option value="popular">热门</option>
                  </select>
                </div>
                <div class="filter-group">
                  <label>分类</label>
                  <select v-model="filters.categoryId" @change="handleFilterChange">
                    <option :value="null">全部</option>
                    <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                      {{ cat.name }}
                    </option>
                  </select>
                </div>
                <div class="filter-group">
                  <label>标签</label>
                  <Input
                    v-model="filters.tagName"
                    placeholder="输入标签名称"
                    @keyup.enter="handleFilterChange"
                  />
                </div>
                <div class="filter-group">
                  <label>作者</label>
                  <Input
                    v-model="filters.authorId"
                    type="number"
                    placeholder="作者ID"
                    @keyup.enter="handleFilterChange"
                  />
                </div>
                <Button variant="primary" block @click="handleFilterChange">应用筛选</Button>
              </div>
            </Card>
          </aside>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { search, getSearchSuggestions, getHotKeywords, getSearchHistory, clearSearchHistory } from '@/api/search'
import { followUser, unfollowUser } from '@/api/follow'
import { formatRelativeTime, formatLargeNumber } from '@/utils/format'
import Card from '@/components/Card.vue'
import Button from '@/components/Button.vue'
import Input from '@/components/Input.vue'
import Avatar from '@/components/Avatar.vue'
import Loading from '@/components/Loading.vue'

const router = useRouter()
const userStore = useUserStore()

// 内容类型标签
const contentTabs = [
  { label: '全部', value: 'all' },
  { label: '文章', value: 'articles' },
  { label: '问答', value: 'questions' },
  { label: '用户', value: 'users' },
]

const searchKeyword = ref('')
const activeTab = ref('all')
const hasSearched = ref(false)
const loading = ref(false)
const showSuggestions = ref(false)
const suggestions = ref([])
const hotKeywords = ref([])
const searchHistory = ref([])
const categories = ref([]) // TODO: 从API获取分类列表

const results = reactive({
  articles: { items: [], total: 0 },
  questions: { items: [], total: 0 },
  users: { items: [], total: 0 },
})

const filters = reactive({
  sort: 'relevance',
  categoryId: null,
  tagName: '',
  authorId: null,
})

// 计算总结果数
const totalResults = computed(() => {
  return (
    (results.articles?.total || 0) +
    (results.questions?.total || 0) +
    (results.users?.total || 0)
  )
})

// 更新标签计数
const updatedTabs = computed(() => {
  return contentTabs.map((tab) => {
    if (tab.value === 'all') {
      return { ...tab, count: totalResults.value }
    } else if (tab.value === 'articles') {
      return { ...tab, count: results.articles?.total || 0 }
    } else if (tab.value === 'questions') {
      return { ...tab, count: results.questions?.total || 0 }
    } else if (tab.value === 'users') {
      return { ...tab, count: results.users?.total || 0 }
    }
    return tab
  })
})

// 获取搜索建议
async function fetchSuggestions(keyword) {
  if (!keyword.trim()) {
    showSuggestions.value = false
    return
  }

  try {
    const response = await getSearchSuggestions(keyword)
    if (response.data && response.data.suggestions) {
      suggestions.value = response.data.suggestions
      showSuggestions.value = true
    }
  } catch (error) {
    console.error('获取搜索建议失败:', error)
  }
}

// 获取热门搜索词
async function fetchHotKeywords() {
  try {
    const response = await getHotKeywords()
    if (response.data && response.data.keywords) {
      hotKeywords.value = response.data.keywords
    }
  } catch (error) {
    console.error('获取热门搜索词失败:', error)
  }
}

// 获取搜索历史
async function fetchSearchHistory() {
  if (!userStore.isLoggedIn) return

  try {
    const response = await getSearchHistory()
    if (response.data && response.data.items) {
      searchHistory.value = response.data.items.map((item) => item.keyword)
    }
  } catch (error) {
    console.error('获取搜索历史失败:', error)
  }
}

// 执行搜索
async function performSearch() {
  if (!searchKeyword.value.trim()) {
    alert('请输入搜索关键词')
    return
  }

  loading.value = true
  hasSearched.value = true
  showSuggestions.value = false

  try {
    const params = {
      keyword: searchKeyword.value.trim(),
      type: activeTab.value === 'all' ? undefined : activeTab.value,
      sort: filters.sort,
      categoryId: filters.categoryId || undefined,
      tagId: filters.tagName ? undefined : undefined, // TODO: 需要先通过标签名获取标签ID
      authorId: filters.authorId || undefined,
      page: 1,
      pageSize: 20,
    }

    const response = await search(params)

    if (response.data) {
      results.articles = response.data.articles || { items: [], total: 0 }
      results.questions = response.data.questions || { items: [], total: 0 }
      results.users = response.data.users || { items: [], total: 0 }
    }
  } catch (error) {
    console.error('搜索失败:', error)
    alert(error.message || '搜索失败，请重试')
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  performSearch()
}

// 输入变化
function handleInputChange() {
  if (searchKeyword.value.trim()) {
    fetchSuggestions(searchKeyword.value)
  } else {
    showSuggestions.value = false
  }
}

// 选择建议
function selectSuggestion(keyword) {
  searchKeyword.value = keyword
  showSuggestions.value = false
  performSearch()
}

// 选择热门关键词
function selectHotKeyword(keyword) {
  searchKeyword.value = keyword
  performSearch()
}

// 清除历史
async function clearHistory() {
  if (!confirm('确定要清除搜索历史吗？')) {
    return
  }

  try {
    await clearSearchHistory()
    searchHistory.value = []
  } catch (error) {
    console.error('清除搜索历史失败:', error)
    alert(error.message || '清除失败，请重试')
  }
}

// 标签切换
function handleTabChange(tab) {
  activeTab.value = tab
  if (hasSearched.value) {
    performSearch()
  }
}

// 筛选变化
function handleFilterChange() {
  if (hasSearched.value) {
    performSearch()
  }
}

// 关注用户
async function handleFollow(user) {
  if (!userStore.isLoggedIn) {
    router.push({ name: 'login' })
    return
  }

  try {
    if (user.isFollowing) {
      await unfollowUser(user.id)
      user.isFollowing = false
      user.followerCount = Math.max(0, (user.followerCount || 0) - 1)
    } else {
      await followUser(user.id)
      user.isFollowing = true
      user.followerCount = (user.followerCount || 0) + 1
    }
  } catch (error) {
    console.error('关注操作失败:', error)
    alert(error.message || '操作失败，请重试')
  }
}

// 跳转
function goToArticle(id) {
  router.push({ name: 'article-detail', params: { id } })
}

function goToQuestion(id) {
  router.push({ name: 'question-detail', params: { id } })
}

function goToUserProfile(id) {
  router.push({ name: 'user-profile', params: { id } })
}

// 监听搜索关键词变化（防抖）
let debounceTimer = null
watch(searchKeyword, (newVal) => {
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }
  debounceTimer = setTimeout(() => {
    if (newVal.trim()) {
      fetchSuggestions(newVal)
    }
  }, 300)
})

// 组件挂载时获取数据
onMounted(() => {
  fetchHotKeywords()
  fetchSearchHistory()
})
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  padding: 40px 0;
}

.search-header {
  margin-bottom: 32px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--midnight-purple);
  margin-bottom: 12px;
}

.page-description {
  font-size: 15px;
  color: var(--text-dark);
  opacity: 0.8;
  margin-bottom: 24px;
}

.search-input-wrapper {
  position: relative;
  margin-bottom: 24px;
}

.search-input {
  width: 100%;
  max-width: 800px;
}

.suggestions-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  max-width: 800px;
  background: white;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow);
  margin-top: 8px;
  max-height: 300px;
  overflow-y: auto;
  z-index: 100;
}

.suggestion-item {
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid var(--border);
}

.suggestion-item:last-child {
  border-bottom: none;
}

.suggestion-item:hover {
  background: rgba(255, 126, 138, 0.05);
}

.hot-keywords {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.hot-label {
  font-size: 14px;
  color: var(--text-dark);
  opacity: 0.7;
}

.hot-keyword-btn {
  padding: 6px 12px;
  background: rgba(255, 126, 138, 0.1);
  border: 1px solid var(--coral-pink);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--coral-pink);
  cursor: pointer;
  transition: all 0.2s;
}

.hot-keyword-btn:hover {
  background: var(--coral-pink);
  color: white;
}

.hot-count {
  font-size: 12px;
  opacity: 0.7;
}

.search-history {
  margin-bottom: 24px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.history-label {
  font-size: 14px;
  color: var(--text-dark);
  opacity: 0.7;
}

.history-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.history-item {
  padding: 6px 12px;
  background: rgba(166, 140, 224, 0.1);
  border: 1px solid var(--stardust-purple);
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--stardust-purple);
  cursor: pointer;
  transition: all 0.2s;
}

.history-item:hover {
  background: var(--stardust-purple);
  color: white;
}

.content-tabs {
  display: flex;
  gap: 8px;
  border-bottom: 2px solid var(--border);
  margin-bottom: 24px;
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

.tab-count {
  font-size: 13px;
  opacity: 0.7;
}

.search-content {
  margin-top: 32px;
}

.dual-column {
  display: grid;
  grid-template-columns: 2.5fr 1fr;
  gap: 40px;
  align-items: start;
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.result-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin-bottom: 16px;
}

.loading-container,
.empty-result,
.empty-search {
  padding: 60px 20px;
  text-align: center;
  color: var(--text-dark);
  opacity: 0.6;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-item {
  cursor: pointer;
}

.result-item-content {
  padding: 20px;
}

.result-item-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin-bottom: 8px;
  line-height: 1.4;
}

.result-item-summary {
  font-size: 14px;
  color: var(--text-dark);
  opacity: 0.8;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.result-item-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-dark);
  opacity: 0.7;
}

.user-result-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 12px;
}

.user-info {
  flex: 1;
}

.user-bio {
  font-size: 14px;
  color: var(--text-dark);
  opacity: 0.7;
  margin-top: 4px;
}

.user-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--text-dark);
  opacity: 0.7;
}

.filter-sidebar {
  position: sticky;
  top: 20px;
}

.filter-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin: 0;
}

.filter-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-group label {
  font-size: 14px;
  font-weight: 500;
  color: var(--midnight-purple);
}

.filter-group select {
  padding: 8px 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-dark);
  background: white;
  cursor: pointer;
  outline: none;
}

@media (max-width: 768px) {
  .dual-column {
    grid-template-columns: 1fr;
    gap: 30px;
  }

  .filter-sidebar {
    order: -1;
  }
}
</style>

