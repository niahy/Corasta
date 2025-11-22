<template>
  <div 
    :class="['comment-item', { 
      'comment-item-pinned': comment.isPinned, 
      'comment-item-reply': isReply,
      'comment-item-depth-1': depth === 1,
      'comment-item-depth-2': depth === 2,
      'comment-item-depth-max': depth >= 3
    }]"
    ref="commentItemRef"
  >
    <div class="comment-main">
      <div class="comment-avatar">
        <Avatar
          :src="comment.author.avatar"
          :name="comment.author.nickname"
          size="small"
        />
      </div>
      <div class="comment-body">
        <div class="comment-header">
          <div class="comment-author-info">
            <span class="comment-author-name">{{ comment.author.nickname }}</span>
            <span v-if="comment.isPinned" class="comment-pinned-badge">置顶</span>
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

        <div class="comment-actions">
          <button
            :class="['comment-action-btn', { active: comment.isLiked }]"
            @click="handleLike"
            :disabled="likeLoading"
          >
            <span class="action-icon">👍</span>
            <span>{{ comment.likeCount || 0 }}</span>
          </button>
          <button class="comment-action-btn" @click="toggleReplyInput">
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

        <!-- 回复输入框（在评论下方） -->
        <div v-if="showReplyInput" class="reply-input-container" @click.stop>
          <div class="reply-input-header">
            <Avatar
              :src="userStore.userInfo?.avatar"
              :name="userStore.userInfo?.nickname"
              size="small"
            />
            <span class="reply-input-hint">
              回复 @{{ comment.author.nickname }}
            </span>
          </div>
          <div class="reply-input-body">
            <textarea
              v-model="replyContent"
              class="reply-textarea"
              :placeholder="`回复 @${comment.author.nickname}：`"
              :maxlength="1000"
              rows="3"
              ref="replyTextareaRef"
            ></textarea>
            <div class="reply-input-footer">
              <span class="reply-char-count">{{ replyContent.length }}/1000</span>
              <div class="reply-input-actions">
                <Button
                  variant="text"
                  size="small"
                  @click="cancelReplyInput"
                >
                  取消
                </Button>
                <Button
                  variant="primary"
                  size="small"
                  :loading="submitting"
                  :disabled="!replyContent.trim()"
                  @click="handleSubmitReply"
                >
                  发布
                </Button>
              </div>
            </div>
          </div>
        </div>

        <!-- 回复列表（支持多级嵌套） -->
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
              :is-reply="true"
              :depth="depth + 1"
              @reply="handleReplyFromChild"
              @like="handleLike"
              @delete="handleDelete"
              @edit="handleEdit"
              @pin="handlePin"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { updateComment, deleteComment, likeComment, unlikeComment, pinComment, createComment } from '@/api/comment'
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
  isReply: {
    type: Boolean,
    default: false,
  },
  depth: {
    type: Number,
    default: 0,
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
const showReplyInput = ref(false)
const replyContent = ref('')
const submitting = ref(false)
const replyTextareaRef = ref(null)
const commentItemRef = ref(null)

// 权限判断
const canEdit = computed(() => {
  return userStore.userInfo && props.comment.author.id === userStore.userInfo.id
})

const canDelete = computed(() => {
  return (
    (userStore.userInfo && props.comment.author.id === userStore.userInfo.id) ||
    props.isAuthor // 内容作者可以删除自己内容下的评论
  )
})

const canPin = computed(() => {
  return props.isAuthor // 只有内容作者可以置顶
})

// 渲染评论内容（简单 Markdown）
const renderedContent = computed(() => {
  if (!props.comment.content) return ''
  
  let content = props.comment.content
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
  editContent.value = props.comment.content
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
    await updateComment(props.comment.id, {
      content: editContent.value.trim(),
    })
    isEditing.value = false
    emit('edit', props.comment.id)
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
    if (props.comment.isLiked) {
      await unlikeComment(props.comment.id)
      props.comment.isLiked = false
      props.comment.likeCount = (props.comment.likeCount || 0) - 1
    } else {
      await likeComment(props.comment.id)
      props.comment.isLiked = true
      props.comment.likeCount = (props.comment.likeCount || 0) + 1
    }
    emit('like', props.comment.id)
  } catch (error) {
    console.error('点赞操作失败:', error)
    alert(error.message || '操作失败，请重试')
  } finally {
    likeLoading.value = false
  }
}

// 切换回复输入框
function toggleReplyInput() {
  if (!userStore.isLoggedIn) {
    alert('请先登录')
    return
  }
  showReplyInput.value = !showReplyInput.value
  if (showReplyInput.value) {
    nextTick(() => {
      replyTextareaRef.value?.focus()
    })
  } else {
    replyContent.value = ''
  }
}

// 取消回复输入
function cancelReplyInput() {
  showReplyInput.value = false
  replyContent.value = ''
}

// 提交回复
async function handleSubmitReply() {
  if (!userStore.isLoggedIn) {
    alert('请先登录')
    return
  }

  if (!replyContent.value.trim()) {
    alert('请输入回复内容')
    return
  }

  submitting.value = true
  try {
    // 关键：使用当前评论的 ID 作为 parentId
    // 无论当前评论是顶级评论还是回复，都使用它的 ID
    // 这样当C回复B时，B的ID会作为parentId传递，确保C的回复是回复B的，而不是对文章的评论
    const response = await createComment({
      targetType: props.targetType,
      targetId: props.targetId,
      content: replyContent.value.trim(),
      parentId: props.comment.id, // 使用当前评论/回复的 ID 作为 parentId
    })

    if (response.data) {
      replyContent.value = ''
      showReplyInput.value = false
      // 通知父组件刷新评论列表
      emit('reply', props.comment)
    }
  } catch (error) {
    console.error('发布回复失败:', error)
    alert(error.message || '发布回复失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 处理子组件的回复事件（用于刷新回复列表）
function handleReplyFromChild(comment) {
  emit('reply', comment)
}

// 点击外部关闭回复输入框
function handleClickOutside(event) {
  if (!showReplyInput.value) {
    return
  }
  
  // 如果点击的是回复输入框内部，不关闭
  if (event.target.closest('.reply-input-container')) {
    return
  }
  
  // 如果点击的是当前评论项的其他部分，也不关闭（允许用户点击评论内容等）
  if (commentItemRef.value && commentItemRef.value.contains(event.target)) {
    // 但如果点击的是回复按钮，会切换状态，这里不处理
    return
  }
  
  // 点击页面其他地方，关闭输入框
  showReplyInput.value = false
  replyContent.value = ''
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

// 删除
async function handleDeleteClick() {
  if (!confirm('确定要删除这条评论吗？')) {
    return
  }

  try {
    await deleteComment(props.comment.id)
    emit('delete', props.comment.id)
  } catch (error) {
    console.error('删除评论失败:', error)
    alert(error.message || '删除失败，请重试')
  }
}

// 编辑
function handleEdit() {
  emit('edit', props.comment.id)
}

// 置顶
async function handlePinClick() {
  pinLoading.value = true
  try {
    await pinComment(props.comment.id)
    props.comment.isPinned = !props.comment.isPinned
    emit('pin', props.comment.id)
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
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-item-pinned {
  background: rgba(255, 107, 107, 0.05);
  padding: 12px;
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--coral-pink);
}

.comment-item-reply {
  padding: 8px 0;
}

/* depth=0: 一级评论（parentId: null），不缩进 */

/* depth=1: 二级评论（B回复A），缩进1级 - 16px */
.comment-item-depth-1 {
  transform: translateX(16px);
  padding-left: 16px;
  border-left: 2px solid var(--border);
}

/* depth=2: 三级评论（C回复B），缩进2级 - 32px */
.comment-item-depth-2 {
  transform: translateX(32px);
  padding-left: 16px;
  border-left: 2px solid var(--border);
}

/* depth>=3: 四级及以后（D回复C、E回复D等），保持缩进2级 - 32px，不再增加 */
.comment-item-depth-max {
  transform: translateX(32px);
  padding-left: 16px;
  border-left: 2px solid var(--border);
}

.comment-main {
  display: flex;
  gap: 12px;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  margin-bottom: 6px;
}

.comment-author-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.comment-author-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--midnight-purple);
}

.comment-pinned-badge {
  font-size: 11px;
  padding: 2px 6px;
  background: var(--coral-pink);
  color: white;
  border-radius: 4px;
}

.comment-time {
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
  margin-bottom: 8px;
}

.comment-text {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-dark);
  word-wrap: break-word;
  word-break: break-word;
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
  font-size: 13px;
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

.comment-actions {
  display: flex;
  gap: 16px;
  margin-top: 4px;
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
  border-radius: 4px;
}

.comment-action-btn:hover {
  opacity: 1;
  color: var(--coral-pink);
  background: rgba(255, 107, 107, 0.1);
}

.comment-action-btn.active {
  color: var(--coral-pink);
  opacity: 1;
}

.comment-action-btn.danger:hover {
  color: #ff4757;
  background: rgba(255, 71, 87, 0.1);
}

.comment-action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-icon {
  font-size: 14px;
}

.comment-replies {
  margin-top: 12px;
  /* 确保回复列表容器不添加额外缩进 */
  margin-left: 0 !important;
  padding-left: 0 !important;
  transform: none !important;
}

.replies-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.replies-count {
  font-size: 12px;
  color: var(--text-dark);
  opacity: 0.7;
}

.replies-toggle {
  font-size: 12px;
  color: var(--coral-pink);
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0;
  transition: opacity 0.2s;
}

.replies-toggle:hover {
  opacity: 0.8;
}

.replies-list {
  display: flex;
  flex-direction: column;
  gap: 0;
  /* 确保回复列表不添加额外缩进 */
  margin-left: 0 !important;
  padding-left: 0 !important;
}

.reply-input-container {
  margin-top: 12px;
  padding: 12px;
  background: var(--bg-light);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border);
}

.reply-input-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.reply-input-hint {
  font-size: 13px;
  color: var(--text-dark);
  opacity: 0.7;
}

.reply-input-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.reply-textarea {
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
  transition: border-color 0.2s;
}

.reply-textarea:focus {
  border-color: var(--coral-pink);
}

.reply-input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reply-char-count {
  font-size: 12px;
  color: var(--text-dark);
  opacity: 0.6;
}

.reply-input-actions {
  display: flex;
  gap: 8px;
}
</style>

