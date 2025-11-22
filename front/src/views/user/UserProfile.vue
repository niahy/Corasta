<template>
  <div class="user-profile-page">
    <div class="app-shell">
      <!-- 加载中 -->
      <div v-if="loading" class="loading-container">
        <Loading text="加载中..." />
      </div>

      <!-- 用户信息 -->
      <div v-else-if="profile" class="profile-content">
        <!-- 用户头部 -->
        <header class="profile-header">
          <Card class="profile-header-card" gradient>
            <div class="profile-header-content">
              <Avatar
                :src="profile.avatar"
                :name="profile.nickname"
                size="large"
                class="profile-avatar"
              />
              <div class="profile-info">
                <div class="profile-badges">
                  <Badge v-if="profile.level" variant="primary">
                    创作者等级 · L{{ profile.level }}
                  </Badge>
                </div>
                <h1 class="profile-name">{{ profile.nickname }}</h1>
                <p v-if="profile.bio" class="profile-bio">{{ profile.bio }}</p>
                <div class="profile-stats">
                  <span class="stat-item">
                    <strong>{{ formatLargeNumber(profile.followingCount || 0) }}</strong>
                    <span class="stat-label">关注</span>
                  </span>
                  <span class="stat-item">
                    <strong>{{ formatLargeNumber(profile.followerCount || 0) }}</strong>
                    <span class="stat-label">粉丝</span>
                  </span>
                  <span class="stat-item">
                    <strong>{{ formatLargeNumber(profile.totalLikeCount || 0) }}</strong>
                    <span class="stat-label">获赞</span>
                  </span>
                </div>
              </div>
              <div v-if="!isOwnProfile" class="profile-actions">
                <Button variant="outline" @click="handleMessage">发私信</Button>
                <Button
                  :variant="profile.isFollowing ? 'primary' : 'outline'"
                  :loading="followLoading"
                  @click="handleFollow"
                >
                  {{ profile.isFollowing ? '已关注' : '关注' }}
                </Button>
              </div>
              <div v-else class="profile-actions">
                <Button variant="outline" to="/dashboard">内容管理</Button>
                <Button variant="primary" to="/articles/create">发布文章</Button>
              </div>
            </div>
          </Card>
        </header>

        <!-- 内容标签页 -->
        <section class="profile-tabs-section">
          <div class="tabs">
            <button
              v-for="tab in tabs"
              :key="tab.value"
              :class="['tab-btn', { active: activeTab === tab.value }]"
              @click="handleTabChange(tab.value)"
            >
              {{ tab.label }}
              <span v-if="tab.count !== undefined" class="tab-count">({{ tab.count }})</span>
            </button>
          </div>

          <!-- 文章标签页 -->
          <div v-show="activeTab === 'articles'" class="tab-content">
            <div v-if="articlesLoading && articles.length === 0" class="loading-container">
              <Loading text="加载中..." />
            </div>
            <div v-else-if="articles.length === 0" class="empty-container">
              <p class="empty-text">暂无文章</p>
            </div>
            <div v-else class="content-grid">
              <article
                v-for="article in articles"
                :key="article.id"
                class="content-card"
                @click="goToArticle(article.id)"
              >
                <Card hover>
                  <div v-if="article.coverImage" class="content-cover">
                    <img :src="article.coverImage" :alt="article.title" />
                  </div>
                  <div class="content-body">
                    <div class="content-meta">
                      <Badge v-if="article.category" variant="secondary" size="small">
                        {{ article.category.name }}
                      </Badge>
                      <span class="content-time">{{ formatRelativeTime(article.createdAt) }}</span>
                    </div>
                    <h3 class="content-title">{{ article.title }}</h3>
                    <p v-if="article.summary" class="content-summary">{{ article.summary }}</p>
                    <div class="content-stats">
                      <span>👁️ {{ formatLargeNumber(article.viewCount) }}</span>
                      <span>❤️ {{ formatLargeNumber(article.likeCount) }}</span>
                      <span>💬 {{ formatLargeNumber(article.commentCount) }}</span>
                    </div>
                  </div>
                </Card>
              </article>
            </div>
          </div>

          <!-- 问答标签页 -->
          <div v-show="activeTab === 'questions'" class="tab-content">
            <div v-if="questionsLoading && questions.length === 0" class="loading-container">
              <Loading text="加载中..." />
            </div>
            <div v-else-if="questions.length === 0" class="empty-container">
              <p class="empty-text">暂无问答</p>
            </div>
            <div v-else class="content-list">
              <article
                v-for="question in questions"
                :key="question.id"
                class="content-item"
                @click="goToQuestion(question.id)"
              >
                <Card hover>
                  <div class="content-item-content">
                    <h3 class="content-title">{{ question.title }}</h3>
                    <p v-if="question.description" class="content-summary">
                      {{ question.description }}
                    </p>
                    <div class="content-meta">
                      <span>{{ formatRelativeTime(question.createdAt) }}</span>
                      <span>·</span>
                      <span>{{ formatLargeNumber(question.answerCount) }} 回答</span>
                      <span>·</span>
                      <span>{{ formatLargeNumber(question.followerCount) }} 关注</span>
                    </div>
                  </div>
                </Card>
              </article>
            </div>
          </div>

          <!-- 收藏标签页 -->
          <div v-show="activeTab === 'favorites'" class="tab-content">
            <div v-if="favoritesLoading && favorites.length === 0" class="loading-container">
              <Loading text="加载中..." />
            </div>
            <div v-else-if="favorites.length === 0" class="empty-container">
              <p class="empty-text">暂无收藏</p>
            </div>
            <div v-else class="content-grid">
              <article
                v-for="favorite in favorites"
                :key="`${favorite.targetType}-${favorite.targetId}`"
                class="content-card"
                @click="goToContent(favorite)"
              >
                <Card hover>
                  <div class="content-body">
                    <div class="content-meta">
                      <Badge variant="secondary" size="small">
                        {{ getTargetTypeLabel(favorite.targetType) }}
                      </Badge>
                      <span v-if="favorite.folder" class="folder-badge">
                        {{ favorite.folder.name }}
                      </span>
                      <span class="content-time">{{ formatRelativeTime(favorite.createdAt) }}</span>
                    </div>
                    <h3 class="content-title">{{ favorite.targetTitle }}</h3>
                    <div class="content-stats">
                      <span>❤️ {{ formatLargeNumber(favorite.likeCount || 0) }}</span>
                      <span>💬 {{ formatLargeNumber(favorite.commentCount || 0) }}</span>
                    </div>
                  </div>
                </Card>
              </article>
            </div>
          </div>

          <!-- 动态标签页 -->
          <div v-show="activeTab === 'activities'" class="tab-content">
            <div v-if="activitiesLoading && activities.length === 0" class="loading-container">
              <Loading text="加载中..." />
            </div>
            <div v-else-if="activities.length === 0" class="empty-container">
              <p class="empty-text">暂无动态</p>
            </div>
            <div v-else class="activity-list">
              <article
                v-for="activity in activities"
                :key="activity.id"
                class="activity-item"
              >
                <Card>
                  <div class="activity-content">
                    <div class="activity-type">
                      <Badge :variant="getActivityBadgeVariant(activity.type)">
                        {{ getActivityTypeLabel(activity.type) }}
                      </Badge>
                    </div>
                    <div class="activity-main">
                      <p class="activity-text">{{ activity.description }}</p>
                      <div class="activity-meta">
                        <span>{{ formatRelativeTime(activity.createdAt) }}</span>
                      </div>
                    </div>
                  </div>
                </Card>
              </article>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserProfile } from '@/api/user'
import { getArticleList } from '@/api/article'
import { getQuestionList } from '@/api/question'
import { getFavoriteList } from '@/api/favorite'
import { followUser, unfollowUser } from '@/api/follow'
import { formatRelativeTime, formatLargeNumber } from '@/utils/format'
import Card from '@/components/Card.vue'
import Button from '@/components/Button.vue'
import Badge from '@/components/Badge.vue'
import Avatar from '@/components/Avatar.vue'
import Loading from '@/components/Loading.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const profile = ref(null)
const loading = ref(false)
const followLoading = ref(false)

// 标签配置
const tabs = [
  { label: '文章', value: 'articles' },
  { label: '问答', value: 'questions' },
  { label: '收藏', value: 'favorites' },
  { label: '动态', value: 'activities' },
]

const activeTab = ref('articles')

// 内容数据
const articles = ref([])
const articlesLoading = ref(false)
const questions = ref([])
const questionsLoading = ref(false)
const favorites = ref([])
const favoritesLoading = ref(false)
const activities = ref([])
const activitiesLoading = ref(false)

// 是否自己的主页
const isOwnProfile = computed(() => {
  return (
    profile.value &&
    userStore.userInfo &&
    profile.value.id === userStore.userInfo.id
  )
})

// 更新标签计数
const updatedTabs = computed(() => {
  return tabs.map((tab) => {
    if (tab.value === 'articles') {
      return { ...tab, count: profile.value?.articleCount || 0 }
    } else if (tab.value === 'questions') {
      return { ...tab, count: profile.value?.questionCount || 0 }
    } else if (tab.value === 'favorites') {
      return { ...tab, count: profile.value?.favoriteCount || 0 }
    }
    return tab
  })
})

// 获取用户主页信息
async function fetchUserProfile() {
  loading.value = true
  try {
    const userId = route.params.id
    const response = await getUserProfile(userId, activeTab.value)

    if (response.data) {
      profile.value = response.data
      // 根据当前标签页加载对应内容
      loadTabContent()
    }
  } catch (error) {
    console.error('获取用户主页失败:', error)
    alert(error.message || '获取用户主页失败，请重试')
  } finally {
    loading.value = false
  }
}

// 加载标签页内容
async function loadTabContent() {
  if (!profile.value) return

  const userId = profile.value.id

  if (activeTab.value === 'articles') {
    await fetchArticles(userId)
  } else if (activeTab.value === 'questions') {
    await fetchQuestions(userId)
  } else if (activeTab.value === 'favorites') {
    await fetchFavorites()
  } else if (activeTab.value === 'activities') {
    await fetchActivities(userId)
  }
}

// 获取文章列表
async function fetchArticles(userId) {
  articlesLoading.value = true
  try {
    const response = await getArticleList({
      userId,
      page: 1,
      pageSize: 20,
    })

    if (response.data && response.data.items) {
      articles.value = response.data.items
    }
  } catch (error) {
    console.error('获取文章列表失败:', error)
  } finally {
    articlesLoading.value = false
  }
}

// 获取问题列表
async function fetchQuestions(userId) {
  questionsLoading.value = true
  try {
    const response = await getQuestionList({
      userId,
      page: 1,
      pageSize: 20,
    })

    if (response.data && response.data.items) {
      questions.value = response.data.items
    }
  } catch (error) {
    console.error('获取问题列表失败:', error)
  } finally {
    questionsLoading.value = false
  }
}

// 获取收藏列表
async function fetchFavorites() {
  if (!isOwnProfile.value) {
    // 非自己的主页，不显示收藏
    favorites.value = []
    return
  }

  favoritesLoading.value = true
  try {
    const response = await getFavoriteList({
      page: 1,
      pageSize: 20,
    })

    if (response.data && response.data.items) {
      favorites.value = response.data.items
    }
  } catch (error) {
    console.error('获取收藏列表失败:', error)
  } finally {
    favoritesLoading.value = false
  }
}

// 获取动态列表
async function fetchActivities(userId) {
  activitiesLoading.value = true
  try {
    // TODO: 实现动态API
    // const response = await getActivities(userId)
    activities.value = []
  } catch (error) {
    console.error('获取动态列表失败:', error)
  } finally {
    activitiesLoading.value = false
  }
}

// 标签切换
function handleTabChange(tab) {
  activeTab.value = tab
  loadTabContent()
}

// 关注/取消关注
async function handleFollow() {
  if (!userStore.isLoggedIn) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }

  followLoading.value = true
  try {
    if (profile.value.isFollowing) {
      await unfollowUser(profile.value.id)
      profile.value.isFollowing = false
      profile.value.followerCount = Math.max(0, (profile.value.followerCount || 0) - 1)
    } else {
      await followUser(profile.value.id)
      profile.value.isFollowing = true
      profile.value.followerCount = (profile.value.followerCount || 0) + 1
    }
  } catch (error) {
    console.error('关注操作失败:', error)
    alert(error.message || '操作失败，请重试')
  } finally {
    followLoading.value = false
  }
}

// 发私信
function handleMessage() {
  // TODO: 实现私信功能
  alert('私信功能开发中...')
}

// 获取目标类型标签
function getTargetTypeLabel(type) {
  const labelMap = {
    article: '文章',
    question: '问答',
    answer: '回答',
    video: '视频',
  }
  return labelMap[type] || '内容'
}

// 获取动态类型标签
function getActivityTypeLabel(type) {
  const labelMap = {
    publish_article: '发布文章',
    publish_question: '提出问题',
    publish_answer: '回答问题',
    like_content: '点赞',
    comment: '评论',
    favorite: '收藏',
  }
  return labelMap[type] || '动态'
}

// 获取动态徽章样式
function getActivityBadgeVariant(type) {
  const variantMap = {
    publish_article: 'primary',
    publish_question: 'secondary',
    publish_answer: 'secondary',
    like_content: 'outline',
    comment: 'outline',
    favorite: 'outline',
  }
  return variantMap[type] || 'outline'
}

// 跳转
function goToArticle(id) {
  router.push({ name: 'article-detail', params: { id } })
}

function goToQuestion(id) {
  router.push({ name: 'question-detail', params: { id } })
}

function goToContent(favorite) {
  const linkMap = {
    article: `/articles/${favorite.targetId}`,
    question: `/questions/${favorite.targetId}`,
    answer: `/questions/${favorite.targetId}`,
    video: `/videos/${favorite.targetId}`,
  }
  const link = linkMap[favorite.targetType] || '/'
  router.push(link)
}

// 监听路由参数变化
watch(
  () => route.params.id,
  () => {
    fetchUserProfile()
  }
)

// 组件挂载时获取数据
onMounted(() => {
  fetchUserProfile()
})
</script>

<style scoped>
.user-profile-page {
  min-height: 100vh;
  padding: 40px 0;
}

.loading-container {
  padding: 80px 20px;
  text-align: center;
}

.profile-header {
  margin-bottom: 32px;
}

.profile-header-card {
  padding: 40px;
}

.profile-header-content {
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.profile-avatar {
  flex-shrink: 0;
}

.profile-info {
  flex: 1;
}

.profile-badges {
  margin-bottom: 12px;
}

.profile-name {
  font-size: 32px;
  font-weight: 700;
  color: var(--midnight-purple);
  margin-bottom: 8px;
}

.profile-bio {
  font-size: 16px;
  color: var(--text-dark);
  opacity: 0.8;
  line-height: 1.6;
  margin-bottom: 16px;
}

.profile-stats {
  display: flex;
  gap: 32px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-item strong {
  font-size: 24px;
  font-weight: 700;
  color: var(--coral-pink);
}

.stat-label {
  font-size: 14px;
  color: var(--text-dark);
  opacity: 0.7;
}

.profile-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.profile-tabs-section {
  margin-top: 32px;
}

.tabs {
  display: flex;
  gap: 8px;
  border-bottom: 2px solid var(--border);
  margin-bottom: 24px;
}

.tab-btn {
  padding: 12px 24px;
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

.empty-container {
  padding: 80px 20px;
  text-align: center;
}

.empty-text {
  font-size: 16px;
  color: var(--text-dark);
  opacity: 0.6;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.content-card {
  cursor: pointer;
}

.content-cover {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: var(--radius-sm) var(--radius-sm) 0 0;
}

.content-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.content-body {
  padding: 20px;
}

.content-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--text-dark);
  opacity: 0.7;
}

.content-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.content-summary {
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

.content-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--text-dark);
  opacity: 0.7;
}

.content-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.content-item {
  cursor: pointer;
}

.content-item-content {
  padding: 20px;
}

.folder-badge {
  font-size: 12px;
  padding: 2px 8px;
  background: rgba(166, 140, 224, 0.1);
  color: var(--stardust-purple);
  border-radius: 4px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  width: 100%;
}

.activity-content {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  padding: 20px;
}

.activity-type {
  flex-shrink: 0;
}

.activity-main {
  flex: 1;
}

.activity-text {
  font-size: 15px;
  color: var(--text-dark);
  line-height: 1.6;
  margin-bottom: 8px;
}

.activity-meta {
  font-size: 13px;
  color: var(--text-dark);
  opacity: 0.6;
}

@media (max-width: 768px) {
  .profile-header-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .profile-actions {
    width: 100%;
    justify-content: center;
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>

