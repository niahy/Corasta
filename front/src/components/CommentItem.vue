<template>
  <div :class="['comment-item', { 'comment-item-pinned': comment.isPinned }]">
    <Card>
      <div class="comment-header">
        <div class="comment-author">
          <Avatar
            :src="comment.author.avatar"
            :name="comment.author.nickname"
            size="small"
          />
          <div class="comment-author-info">
            <span class="comment-author-name">{{ comment.author.nickname }}</span>
            <span v-if="comment.isPinned" class="comment-pinned-badge">置顶</span>
          </div>
        </div>
        <div class="comment-meta">
          <span class="comment-time">{{ formatRelativeTime(comment.createdAt) }}</span>
          <span v-if="comment.updatedAt !== comment.createdAt" class="comment-edited">
            （已编辑）
          </span>
        </div>
      </div>

      <div class="comment-content">
        <div
          v-if="!isEditing"
          class="comment-text"
          v-html="renderedContent"
        ></div>
        <div v-else class="comment-edit">
          <textarea
            v-model="editContent"
            class="comment-edit-textarea"
            :maxlength="1000"
            rows="3"
          ></textarea>
          <div class="comment-edit-actions">
            <Button variant="text" size="small" @click="cancelEdit">取消</Button>
            <Button variant="primary" size="small" :loading="updating" @click="saveEdit">
              保存
            </Button>
          </div>
        </div>
      </div>

      <div class="comment-footer">
        <div class="comment-actions">
          <button
            :class="['comment-action-btn', { active: comment.isLiked }]"
            @click="handleLike"
            :disabled="likeLoading"
          >
            <span class="action-icon">❤️</span>
            <span>{{ comment.likeCount || 0 }}</span>
          </button>
          <button class="comment-action-btn" @click="handleReplyClick">
            <span class="action-icon">💬</span>
            <span>回复</span>
          </button>
          <button
            v-if="canEdit"
            class="comment-action-btn"
            @click="startEdit"
          >
            编辑
          </button>
          <button
            v-if="canDelete"
            class="comment-action-btn danger"
            @click="handleDeleteClick"
          >
            删除
          </button>
          <button
            v-if="canPin"
            class="comment-action-btn"
            @click="handlePinClick"
            :disabled="pinLoading"
          >
            {{ comment.isPinned ? '取消置顶' : '置顶' }}
          </button>
        </div>
      </div>

      <!-- 二级评论（回复） -->
      <div v-if="comment.replies && comment.replies.length > 0" class="comment-replies">
        <div class="replies-header">
          <span class="replies-count">{{ comment.replyCount }} 条回复</span>
          <button
            class="replies-toggle"
            @click="showReplies = !showReplies"
          >
            {{ showReplies ? '收起' : '展开' }}
          </button>
        </div>
        <div v-if="showReplies" class="replies-list">
          <CommentItem
            v-for="reply in comment.replies"
            :key="reply.id"
            :comment="reply"
            :target-type="targetType"
            :target-id="targetId"
            :is-author="isAuthor"
            @reply="handleReply"
            @like="handleLike"
            @delete="handleDelete"
            @edit="handleEdit"
            @pin="handlePin"
          />
        </div>
      </div>
    </Card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { updateComment, deleteComment, likeComment, unlikeComment, pinComment } from '@/api/comment'
import { formatRelativeTime } from '@/utils/format'
import Card from './Card.vue'
import Button from './Button.vue'
import Avatar from './Avatar.vue'

const props = defineProps({
  comment: {
    type: Object,
    required: true,
  },
  targetType: {
    type: String,
    required: true,
  },
  targetId: {
    type: [Number, String],
    required: true,
  },
  isAuthor: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['reply', 'like', 'delete', 'edit', 'pin'])

const userStore = useUserStore()

const isEditing = ref(false)
const editContent = ref('')
const updating = ref(false)
const likeLoading = ref(false)
const pinLoading = ref(false)
const showReplies = ref(true)

// 权限判断
const canEdit = computed(() => {
  return userStore.userInfo && comment.author.id === userStore.userInfo.id
})

const canDelete = computed(() => {
  return (
    (userStore.userInfo && comment.author.id === userStore.userInfo.id) ||
    props.isAuthor // 内容作者可以删除自己内容下的评论
  )
})

const canPin = computed(() => {
  return props.isAuthor // 只有内容作者可以置顶
})

// 渲染评论内容（简单 Markdown）
const renderedContent = computed(() => {
  if (!comment.content) return ''
  
  let content = comment.content
  // 转义 HTML
  content = content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
  
  // 粗体和斜体
  content = content.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  content = content.replace(/\*(.*?)\*/g, '<em>$1</em>')
  
  // 代码
  content = content.replace(/`([^`]+)`/g, '<code>$1</code>')
  
  // 链接
  content = content.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
  
  // @ 提及
  content = content.replace(/@(\w+)/g, '<span class="mention">@$1</span>')
  
  // 换行
  content = content.replace(/\n/g, '<br>')
  
  return content
})

// 开始编辑
function startEdit() {
  isEditing.value = true
  editContent.value = comment.content
}

// 取消编辑
function cancelEdit() {
  isEditing.value = false
  editContent.value = ''
}

// 保存编辑
async function saveEdit() {
  if (!editContent.value.trim()) {
    alert('评论内容不能为空')
    return
  }

  updating.value = true
  try {
    await updateComment(comment.id, {
      content: editContent.value.trim(),
    })
    isEditing.value = false
    emit('edit', comment.id)
  } catch (error) {
    console.error('更新评论失败:', error)
    alert(error.message || '更新失败，请重试')
  } finally {
    updating.value = false
  }
}

// 点赞/取消点赞
async function handleLike() {
  if (!userStore.isLoggedIn) {
    alert('请先登录')
    return
  }

  likeLoading.value = true
  try {
    if (comment.isLiked) {
      await unlikeComment(comment.id)
      comment.isLiked = false
      comment.likeCount = (comment.likeCount || 0) - 1
    } else {
      await likeComment(comment.id)
      comment.isLiked = true
      comment.likeCount = (comment.likeCount || 0) + 1
    }
    emit('like', comment.id)
  } catch (error) {
    console.error('点赞操作失败:', error)
    alert(error.message || '操作失败，请重试')
  } finally {
    likeLoading.value = false
  }
}

// 回复
function handleReplyClick() {
  emit('reply', comment)
}

// 删除
async function handleDeleteClick() {
  if (!confirm('确定要删除这条评论吗？')) {
    return
  }

  try {
    await deleteComment(comment.id)
    emit('delete', comment.id)
  } catch (error) {
    console.error('删除评论失败:', error)
    alert(error.message || '删除失败，请重试')
  }
}

// 编辑
function handleEdit() {
  emit('edit', comment.id)
}

// 置顶
async function handlePinClick() {
  pinLoading.value = true
  try {
    await pinComment(comment.id)
    comment.isPinned = !comment.isPinned
    emit('pin', comment.id)
  } catch (error) {
    console.error('置顶操作失败:', error)
    alert(error.message || '操作失败，请重试')
  } finally {
    pinLoading.value = false
  }
}
</script>

<style scoped>
.comment-item {
  width: 100%;
}

.comment-item-pinned {
  border-left: 3px solid var(--coral-pink);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.comment-author {
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-author-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--midnight-purple);
}

.comment-pinned-badge {
  font-size: 12px;
  padding: 2px 6px;
  background: var(--coral-pink);
  color: white;
  border-radius: 4px;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-dark);
  opacity: 0.6;
}

.comment-edited {
  font-size: 11px;
  color: var(--text-dark);
  opacity: 0.5;
}

.comment-content {
  margin-bottom: 12px;
}

.comment-text {
  font-size: 15px;
  line-height: 1.6;
  color: var(--text-dark);
}

.comment-text :deep(strong) {
  font-weight: 600;
  color: var(--midnight-purple);
}

.comment-text :deep(em) {
  font-style: italic;
}

.comment-text :deep(code) {
  background: rgba(166, 140, 224, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
}

.comment-text :deep(a) {
  color: var(--coral-pink);
  text-decoration: none;
}

.comment-text :deep(a:hover) {
  text-decoration: underline;
}

.comment-text :deep(.mention) {
  color: var(--stardust-purple);
  font-weight: 500;
}

.comment-edit {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comment-edit-textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-family: inherit;
  line-height: 1.6;
  color: var(--text-dark);
  resize: vertical;
  outline: none;
}

.comment-edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.comment-footer {
  padding-top: 12px;
  border-top: 1px solid var(--border);
}

.comment-actions {
  display: flex;
  gap: 16px;
}

.comment-action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: transparent;
  border: none;
  font-size: 13px;
  color: var(--text-dark);
  opacity: 0.7;
  cursor: pointer;
  transition: all 0.2s;
}

.comment-action-btn:hover {
  opacity: 1;
  color: var(--coral-pink);
}

.comment-action-btn.active {
  color: var(--coral-pink);
  opacity: 1;
}

.comment-action-btn.danger:hover {
  color: #ff4757;
}

.comment-action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-icon {
  font-size: 14px;
}

.comment-replies {
  margin-top: 16px;
  padding-left: 20px;
  border-left: 2px solid var(--border);
}

.replies-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.replies-count {
  font-size: 13px;
  color: var(--text-dark);
  opacity: 0.7;
}

.replies-toggle {
  font-size: 13px;
  color: var(--coral-pink);
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0;
}

.replies-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>

