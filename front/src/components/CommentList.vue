<template>
  <div class="comment-list">
    <!-- 评论输入框（仅用于发布新评论，不用于回复） -->
    <div v-if="showInput" class="comment-input-section">
      <Card>
        <div class="comment-input-header">
          <Avatar
            :src="userStore.userInfo?.avatar"
            :name="userStore.userInfo?.nickname"
            size="small"
          />
          <span class="comment-input-hint">
            写下你的评论...
          </span>
        </div>
        <div class="comment-input-body">
          <textarea
            v-model="commentContent"
            class="comment-textarea"
            placeholder="支持 Markdown 语法，最多 1000 字符"
            :maxlength="1000"
            rows="4"
          ></textarea>
          <div class="comment-input-footer">
            <span class="comment-char-count">{{ commentContent.length }}/1000</span>
            <div class="comment-input-actions">
              <Button
                variant="primary"
                size="small"
                :loading="submitting"
                :disabled="!commentContent.trim()"
                @click="handleSubmit"
              >
                发布
              </Button>
            </div>
          </div>
        </div>
      </Card>
    </div>

    <!-- 排序选项 -->
    <div class="comment-sort">
      <button
        v-for="sort in sortOptions"
        :key="sort.value"
        :class="['sort-btn', { active: currentSort === sort.value }]"
        @click="handleSortChange(sort.value)"
      >
        {{ sort.label }}
      </button>
    </div>

    <!-- 评论列表 -->
    <div v-if="loading && comments.length === 0" class="loading-container">
      <Loading text="加载中..." />
    </div>

    <div v-else-if="comments.length === 0" class="empty-comments">
      <p>暂无评论，来发表第一条评论吧~</p>
    </div>

    <div v-else class="comments">
      <CommentItem
        v-for="comment in sortedComments"
        :key="comment.id"
        :comment="comment"
        :target-type="targetType"
        :target-id="targetId"
        :is-author="isAuthor"
        :depth="0"
        @reply="handleReply"
        @like="handleCommentLike"
        @delete="handleDelete"
        @edit="handleEdit"
        @pin="handlePin"
      />
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore && !loading" class="load-more">
      <Button variant="outline" :loading="loadingMore" @click="loadMore">
        加载更多
      </Button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { getCommentList, createComment } from '@/api/comment'
import Card from './Card.vue'
import Button from './Button.vue'
import Avatar from './Avatar.vue'
import Loading from './Loading.vue'
import CommentItem from './CommentItem.vue'

const props = defineProps({
  targetType: {
    type: String,
    required: true,
    validator: (value) => ['article', 'answer'].includes(value),
  },
  targetId: {
    type: [Number, String],
    required: true,
  },
  showInput: {
    type: Boolean,
    default: true,
  },
  isAuthor: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['comment-added', 'comment-updated'])

const userStore = useUserStore()

// 排序选项
const sortOptions = [
  { label: '最新', value: 'latest' },
  { label: '最早', value: 'oldest' },
  { label: '热度', value: 'hot' },
]

const currentSort = ref('latest')
const comments = ref([])
const commentContent = ref('')
const submitting = ref(false)
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

// 排序后的评论（置顶评论在前）
const sortedComments = computed(() => {
  const sorted = [...comments.value]
  // 置顶评论在前
  sorted.sort((a, b) => {
    if (a.isPinned && !b.isPinned) return -1
    if (!a.isPinned && b.isPinned) return 1
    return 0
  })
  return sorted
})

// 获取评论列表
async function fetchComments(reset = true) {
  if (reset) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const response = await getCommentList({
      targetType: props.targetType,
      targetId: props.targetId,
      page: pagination.value.page,
      pageSize: pagination.value.pageSize,
      sort: currentSort.value,
    })

    if (response.data) {
      if (reset) {
        comments.value = response.data.items || []
      } else {
        comments.value.push(...(response.data.items || []))
      }
      pagination.value = {
        ...pagination.value,
        ...response.data.pagination,
      }
    }
  } catch (error) {
    console.error('获取评论列表失败:', error)
    alert(error.message || '获取评论失败，请重试')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 排序切换
function handleSortChange(sort) {
  currentSort.value = sort
  resetAndFetch()
}

// 重置并获取
function resetAndFetch() {
  comments.value = []
  pagination.value.page = 1
  fetchComments()
}

// 提交评论（仅用于发布新评论，不用于回复）
async function handleSubmit() {
  if (!userStore.isLoggedIn) {
    alert('请先登录')
    return
  }

  if (!commentContent.value.trim()) {
    alert('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    // 发布新评论，parentId 为 null
    const response = await createComment({
      targetType: props.targetType,
      targetId: props.targetId,
      content: commentContent.value.trim(),
      parentId: null,
    })

    if (response.data) {
      commentContent.value = ''
      // 刷新评论列表
      resetAndFetch()
      emit('comment-added', response.data)
    }
  } catch (error) {
    console.error('发布评论失败:', error)
    alert(error.message || '发布评论失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 回复评论（由 CommentItem 触发，用于刷新列表）
function handleReply(comment) {
  // 刷新评论列表以显示新回复
  resetAndFetch()
}

// 点赞评论
async function handleCommentLike(commentId) {
  // CommentItem 组件内部处理，这里只需要刷新列表
  resetAndFetch()
}

// 删除评论
async function handleDelete(commentId) {
  if (confirm('确定要删除这条评论吗？')) {
    resetAndFetch()
    emit('comment-updated')
  }
}

// 编辑评论
function handleEdit(comment) {
  // CommentItem 组件内部处理
  resetAndFetch()
}

// 置顶评论
async function handlePin(commentId) {
  resetAndFetch()
}

// 加载更多
function loadMore() {
  if (hasMore.value && !loadingMore.value) {
    pagination.value.page += 1
    fetchComments(false)
  }
}

// 监听 targetId 变化，重新获取评论
watch(
  () => props.targetId,
  () => {
    resetAndFetch()
  }
)

// 组件挂载时获取数据
onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.comment-list {
  width: 100%;
}

.comment-input-section {
  margin-bottom: 24px;
}

.comment-input-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.comment-input-hint {
  font-size: 14px;
  color: var(--text-dark);
  opacity: 0.7;
}

.comment-input-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-family: inherit;
  line-height: 1.6;
  color: var(--text-dark);
  resize: vertical;
  outline: none;
  transition: border-color 0.2s;
}

.comment-textarea:focus {
  border-color: var(--coral-pink);
}

.comment-input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comment-char-count {
  font-size: 12px;
  color: var(--text-dark);
  opacity: 0.6;
}

.comment-input-actions {
  display: flex;
  gap: 8px;
}

.comment-sort {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border);
}

.sort-btn {
  padding: 6px 12px;
  background: transparent;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-dark);
  cursor: pointer;
  transition: all 0.2s;
}

.sort-btn:hover {
  border-color: var(--coral-pink);
  color: var(--coral-pink);
}

.sort-btn.active {
  background: var(--coral-pink);
  border-color: var(--coral-pink);
  color: white;
}

.loading-container {
  padding: 40px;
  text-align: center;
}

.empty-comments {
  padding: 60px 20px;
  text-align: center;
  color: var(--text-dark);
  opacity: 0.6;
}

.comments {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.load-more {
  margin-top: 24px;
  text-align: center;
}
</style>

