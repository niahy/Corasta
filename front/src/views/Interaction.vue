<template>
  <div class="interaction-page">
    <div class="app-shell">
      <header class="interaction-header">
        <div class="header-content">
          <Badge>Interaction Hub</Badge>
          <h1 class="page-title">互动中心</h1>
          <p class="page-description">
            集中处理评论、点赞、收藏与通知，查看你的所有互动记录
          </p>
        </div>
        <div class="header-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.value"
            :class="['tab-btn', { active: activeTab === tab.value }]"
            @click="handleTabChange(tab.value)"
          >
            {{ tab.label }}
          </button>
        </div>
      </header>

      <!-- 通知标签页 -->
      <div v-show="activeTab === 'notifications'" class="tab-content">
        <div v-if="notificationsLoading && notifications.length === 0" class="loading-container">
          <Loading text="加载中..." />
        </div>
        <div v-else-if="notifications.length === 0" class="empty-container">
          <p class="empty-text">暂无通知</p>
        </div>
        <div v-else class="notifications-list">
          <div
            v-for="notification in notifications"
            :key="notification.id"
            :class="['notification-item', { unread: !notification.isRead }]"
            @click="handleNotificationClick(notification)"
          >
            <Card hover>
              <div class="notification-content">
                <Avatar
                  :src="notification.fromUser?.avatar"
                  :name="notification.fromUser?.nickname"
                  size="small"
                />
                <div class="notification-info">
                  <div class="notification-title">{{ notification.title }}</div>
                  <div class="notification-text">{{ notification.content }}</div>
                  <div class="notification-meta">
                    <span>{{ formatRelativeTime(notification.createdAt) }}</span>
                    <Badge v-if="!notification.isRead" variant="primary" size="small">
                      未读
                    </Badge>
                  </div>
                </div>
              </div>
            </Card>
          </div>
        </div>
        <div v-if="notificationsHasMore && !notificationsLoading" class="load-more">
          <Button variant="outline" :loading="notificationsLoadingMore" @click="loadMoreNotifications">
            加载更多
          </Button>
        </div>
      </div>

      <!-- 评论标签页 -->
      <div v-show="activeTab === 'comments'" class="tab-content">
        <div v-if="commentsLoading && comments.length === 0" class="loading-container">
          <Loading text="加载中..." />
        </div>
        <div v-else-if="comments.length === 0" class="empty-container">
          <p class="empty-text">暂无评论记录</p>
        </div>
        <div v-else class="interactions-list">
          <div
            v-for="item in comments"
            :key="item.id"
            class="interaction-item"
            @click="goToContent(item)"
          >
            <Card hover>
              <div class="interaction-content">
                <div class="interaction-type">
                  <Badge variant="secondary">评论</Badge>
                </div>
                <div class="interaction-main">
                  <div class="interaction-text">{{ item.content }}</div>
                  <div class="interaction-target">
                    <span class="target-type">{{ getTargetTypeLabel(item.targetType) }}</span>
                    <span class="target-title">{{ item.targetTitle }}</span>
                  </div>
                  <div class="interaction-meta">
                    <span>{{ formatRelativeTime(item.createdAt) }}</span>
                  </div>
                </div>
              </div>
            </Card>
          </div>
        </div>
        <div v-if="commentsHasMore && !commentsLoading" class="load-more">
          <Button variant="outline" :loading="commentsLoadingMore" @click="loadMoreComments">
            加载更多
          </Button>
        </div>
      </div>

      <!-- 点赞和收藏标签页 -->
      <div v-show="activeTab === 'likes'" class="tab-content">
        <div class="likes-tabs">
          <button
            :class="['sub-tab-btn', { active: likesSubTab === 'likes' }]"
            @click="likesSubTab = 'likes'"
          >
            点赞
          </button>
          <button
            :class="['sub-tab-btn', { active: likesSubTab === 'favorites' }]"
            @click="likesSubTab = 'favorites'"
          >
            收藏
          </button>
        </div>

        <!-- 点赞列表 -->
        <div v-show="likesSubTab === 'likes'" class="sub-tab-content">
          <div v-if="likesLoading && likes.length === 0" class="loading-container">
            <Loading text="加载中..." />
          </div>
          <div v-else-if="likes.length === 0" class="empty-container">
            <p class="empty-text">暂无点赞记录</p>
          </div>
          <div v-else class="interactions-list">
            <div
              v-for="item in likes"
              :key="`like-${item.targetType}-${item.targetId}`"
              class="interaction-item"
              @click="goToContent(item)"
            >
              <Card hover>
                <div class="interaction-content">
                  <div class="interaction-type">
                    <Badge variant="primary">点赞</Badge>
                  </div>
                  <div class="interaction-main">
                    <div class="interaction-target">
                      <span class="target-type">{{ getTargetTypeLabel(item.targetType) }}</span>
                      <span class="target-title">{{ item.targetTitle }}</span>
                    </div>
                    <div class="interaction-meta">
                      <span>{{ formatRelativeTime(item.createdAt) }}</span>
                    </div>
                  </div>
                </div>
              </Card>
            </div>
          </div>
          <div v-if="likesHasMore && !likesLoading" class="load-more">
            <Button variant="outline" :loading="likesLoadingMore" @click="loadMoreLikes">
              加载更多
            </Button>
          </div>
        </div>

        <!-- 收藏列表 -->
        <div v-show="likesSubTab === 'favorites'" class="sub-tab-content">
          <div v-if="favoritesLoading && favorites.length === 0" class="loading-container">
            <Loading text="加载中..." />
          </div>
          <div v-else-if="favorites.length === 0" class="empty-container">
            <p class="empty-text">暂无收藏记录</p>
          </div>
          <div v-else class="interactions-list">
            <div
              v-for="item in favorites"
              :key="`favorite-${item.targetType}-${item.targetId}`"
              class="interaction-item"
              @click="goToContent(item)"
            >
              <Card hover>
                <div class="interaction-content">
                  <div class="interaction-type">
                    <Badge variant="secondary">收藏</Badge>
                  </div>
                  <div class="interaction-main">
                    <div class="interaction-target">
                      <span class="target-type">{{ getTargetTypeLabel(item.targetType) }}</span>
                      <span class="target-title">{{ item.targetTitle }}</span>
                    </div>
                    <div v-if="item.folder" class="interaction-folder">
                      <span class="folder-label">收藏夹：</span>
                      <span class="folder-name">{{ item.folder.name }}</span>
                    </div>
                    <div class="interaction-meta">
                      <span>{{ formatRelativeTime(item.createdAt) }}</span>
                    </div>
                  </div>
                </div>
              </Card>
            </div>
          </div>
          <div v-if="favoritesHasMore && !favoritesLoading" class="load-more">
            <Button variant="outline" :loading="favoritesLoadingMore" @click="loadMoreFavorites">
              加载更多
            </Button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getNotificationList, markNotificationRead } from '@/api/notification'
import { getMyInteractions } from '@/api/dashboard'
import { formatRelativeTime } from '@/utils/format'
import Card from '@/components/Card.vue'
import Button from '@/components/Button.vue'
import Badge from '@/components/Badge.vue'
import Avatar from '@/components/Avatar.vue'
import Loading from '@/components/Loading.vue'

const router = useRouter()

// 标签配置
const tabs = [
  { label: '通知', value: 'notifications' },
  { label: '评论', value: 'comments' },
  { label: '点赞 & 收藏', value: 'likes' },
]

const activeTab = ref('notifications')
const likesSubTab = ref('likes') // 点赞/收藏子标签

// 通知相关
const notifications = ref([])
const notificationsLoading = ref(false)
const notificationsLoadingMore = ref(false)
const notificationsPagination = ref({
  page: 1,
  pageSize: 20,
  total: 0,
  totalPages: 0,
})

const notificationsHasMore = computed(() => {
  return notificationsPagination.value.page < notificationsPagination.value.totalPages
})

// 评论相关
const comments = ref([])
const commentsLoading = ref(false)
const commentsLoadingMore = ref(false)
const commentsPagination = ref({
  page: 1,
  pageSize: 20,
  total: 0,
  totalPages: 0,
})

const commentsHasMore = computed(() => {
  return commentsPagination.value.page < commentsPagination.value.totalPages
})

// 点赞相关
const likes = ref([])
const likesLoading = ref(false)
const likesLoadingMore = ref(false)
const likesPagination = ref({
  page: 1,
  pageSize: 20,
  total: 0,
  totalPages: 0,
})

const likesHasMore = computed(() => {
  return likesPagination.value.page < likesPagination.value.totalPages
})

// 收藏相关
const favorites = ref([])
const favoritesLoading = ref(false)
const favoritesLoadingMore = ref(false)
const favoritesPagination = ref({
  page: 1,
  pageSize: 20,
  total: 0,
  totalPages: 0,
})

const favoritesHasMore = computed(() => {
  return favoritesPagination.value.page < favoritesPagination.value.totalPages
})

// 切换标签
function handleTabChange(tab) {
  activeTab.value = tab
  if (tab === 'notifications') {
    if (notifications.value.length === 0) {
      fetchNotifications()
    }
  } else if (tab === 'comments') {
    if (comments.value.length === 0) {
      fetchComments()
    }
  } else if (tab === 'likes') {
    if (likesSubTab.value === 'likes' && likes.value.length === 0) {
      fetchLikes()
    } else if (likesSubTab.value === 'favorites' && favorites.value.length === 0) {
      fetchFavorites()
    }
  }
}

// 获取通知列表
async function fetchNotifications(reset = true) {
  if (reset) {
    notificationsLoading.value = true
  } else {
    notificationsLoadingMore.value = true
  }

  try {
    const response = await getNotificationList({
      page: notificationsPagination.value.page,
      pageSize: notificationsPagination.value.pageSize,
    })

    if (response.data) {
      if (reset) {
        notifications.value = response.data.items || []
      } else {
        notifications.value.push(...(response.data.items || []))
      }
      notificationsPagination.value = {
        ...notificationsPagination.value,
        ...response.data.pagination,
      }
    }
  } catch (error) {
    console.error('获取通知列表失败:', error)
    alert(error.message || '获取通知失败，请重试')
  } finally {
    notificationsLoading.value = false
    notificationsLoadingMore.value = false
  }
}

// 获取评论记录
async function fetchComments(reset = true) {
  if (reset) {
    commentsLoading.value = true
  } else {
    commentsLoadingMore.value = true
  }

  try {
    const response = await getMyInteractions({
      type: 'comments',
      page: commentsPagination.value.page,
      pageSize: commentsPagination.value.pageSize,
    })

    if (response.data) {
      if (reset) {
        comments.value = response.data.items || []
      } else {
        comments.value.push(...(response.data.items || []))
      }
      commentsPagination.value = {
        ...commentsPagination.value,
        ...response.data.pagination,
      }
    }
  } catch (error) {
    console.error('获取评论记录失败:', error)
    alert(error.message || '获取评论记录失败，请重试')
  } finally {
    commentsLoading.value = false
    commentsLoadingMore.value = false
  }
}

// 获取点赞记录
async function fetchLikes(reset = true) {
  if (reset) {
    likesLoading.value = true
  } else {
    likesLoadingMore.value = true
  }

  try {
    const response = await getMyInteractions({
      type: 'likes',
      page: likesPagination.value.page,
      pageSize: likesPagination.value.pageSize,
    })

    if (response.data) {
      if (reset) {
        likes.value = response.data.items || []
      } else {
        likes.value.push(...(response.data.items || []))
      }
      likesPagination.value = {
        ...likesPagination.value,
        ...response.data.pagination,
      }
    }
  } catch (error) {
    console.error('获取点赞记录失败:', error)
    alert(error.message || '获取点赞记录失败，请重试')
  } finally {
    likesLoading.value = false
    likesLoadingMore.value = false
  }
}

// 获取收藏记录
async function fetchFavorites(reset = true) {
  if (reset) {
    favoritesLoading.value = true
  } else {
    favoritesLoadingMore.value = true
  }

  try {
    const response = await getMyInteractions({
      type: 'favorites',
      page: favoritesPagination.value.page,
      pageSize: favoritesPagination.value.pageSize,
    })

    if (response.data) {
      if (reset) {
        favorites.value = response.data.items || []
      } else {
        favorites.value.push(...(response.data.items || []))
      }
      favoritesPagination.value = {
        ...favoritesPagination.value,
        ...response.data.pagination,
      }
    }
  } catch (error) {
    console.error('获取收藏记录失败:', error)
    alert(error.message || '获取收藏记录失败，请重试')
  } finally {
    favoritesLoading.value = false
    favoritesLoadingMore.value = false
  }
}

// 加载更多
function loadMoreNotifications() {
  if (notificationsHasMore.value && !notificationsLoadingMore.value) {
    notificationsPagination.value.page += 1
    fetchNotifications(false)
  }
}

function loadMoreComments() {
  if (commentsHasMore.value && !commentsLoadingMore.value) {
    commentsPagination.value.page += 1
    fetchComments(false)
  }
}

function loadMoreLikes() {
  if (likesHasMore.value && !likesLoadingMore.value) {
    likesPagination.value.page += 1
    fetchLikes(false)
  }
}

function loadMoreFavorites() {
  if (favoritesHasMore.value && !favoritesLoadingMore.value) {
    favoritesPagination.value.page += 1
    fetchFavorites(false)
  }
}

// 获取目标类型标签
function getTargetTypeLabel(type) {
  const labelMap = {
    article: '文章',
    question: '问答',
    answer: '回答',
    comment: '评论',
    video: '视频',
  }
  return labelMap[type] || '内容'
}

// 跳转到内容
function goToContent(item) {
  const linkMap = {
    article: `/articles/${item.targetId}`,
    question: `/questions/${item.targetId}`,
    answer: `/questions/${item.targetId}`, // 回答跳转到问题详情
    comment: `/articles/${item.targetId}`, // 评论跳转到所在内容
    video: `/videos/${item.targetId}`, // 第二阶段
  }
  const link = linkMap[item.targetType] || '/'
  router.push(link)
}

// 点击通知
async function handleNotificationClick(notification) {
  // 标记为已读
  if (!notification.isRead) {
    try {
      await markNotificationRead(notification.id)
      notification.isRead = true
    } catch (error) {
      console.error('标记通知已读失败:', error)
    }
  }

  // 跳转到相关内容
  if (notification.targetType && notification.targetId) {
    goToContent({
      targetType: notification.targetType,
      targetId: notification.targetId,
    })
  }
}

// 监听子标签切换
watch(likesSubTab, (newTab) => {
  if (newTab === 'likes' && likes.value.length === 0) {
    fetchLikes()
  } else if (newTab === 'favorites' && favorites.value.length === 0) {
    fetchFavorites()
  }
})

// 组件挂载时获取数据
onMounted(() => {
  fetchNotifications()
})
</script>

<style scoped>
.interaction-page {
  min-height: 100vh;
  padding: 40px 0;
}

.interaction-header {
  margin-bottom: 32px;
}

.header-content {
  margin-bottom: 24px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--midnight-purple);
  margin: 16px 0 12px 0;
}

.page-description {
  font-size: 15px;
  color: var(--text-dark);
  opacity: 0.8;
  line-height: 1.6;
}

.header-tabs {
  display: flex;
  gap: 8px;
  border-bottom: 2px solid var(--border);
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

.likes-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
}

.sub-tab-btn {
  padding: 8px 16px;
  background: transparent;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-dark);
  cursor: pointer;
  transition: all 0.2s;
}

.sub-tab-btn:hover {
  border-color: var(--coral-pink);
  color: var(--coral-pink);
}

.sub-tab-btn.active {
  background: var(--coral-pink);
  border-color: var(--coral-pink);
  color: white;
}

.loading-container,
.empty-container {
  padding: 80px 20px;
  text-align: center;
}

.empty-text {
  font-size: 16px;
  color: var(--text-dark);
  opacity: 0.6;
}

.notifications-list,
.interactions-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notification-item {
  cursor: pointer;
}

.notification-item.unread {
  border-left: 3px solid var(--coral-pink);
}

.notification-content {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.notification-info {
  flex: 1;
}

.notification-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin-bottom: 8px;
}

.notification-text {
  font-size: 14px;
  color: var(--text-dark);
  opacity: 0.8;
  line-height: 1.6;
  margin-bottom: 8px;
}

.notification-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--text-dark);
  opacity: 0.6;
}

.interaction-item {
  cursor: pointer;
}

.interaction-content {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.interaction-type {
  flex-shrink: 0;
}

.interaction-main {
  flex: 1;
}

.interaction-text {
  font-size: 15px;
  color: var(--text-dark);
  line-height: 1.6;
  margin-bottom: 8px;
}

.interaction-target {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.target-type {
  font-size: 12px;
  padding: 2px 8px;
  background: rgba(166, 140, 224, 0.1);
  color: var(--stardust-purple);
  border-radius: 4px;
}

.target-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--midnight-purple);
}

.interaction-folder {
  font-size: 13px;
  color: var(--text-dark);
  opacity: 0.7;
  margin-bottom: 8px;
}

.folder-label {
  opacity: 0.6;
}

.folder-name {
  color: var(--stardust-purple);
}

.interaction-meta {
  font-size: 12px;
  color: var(--text-dark);
  opacity: 0.6;
}

.load-more {
  margin-top: 32px;
  text-align: center;
}
</style>

