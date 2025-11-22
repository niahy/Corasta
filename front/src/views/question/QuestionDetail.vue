<template>
  <div class="question-detail-page">
    <div class="app-shell">
      <!-- 返回按钮 -->
      <router-link to="/questions" class="back-link">← 返回问答列表</router-link>

      <!-- 加载中 -->
      <div v-if="loading" class="loading-container">
        <Loading text="加载中..." />
      </div>

      <!-- 问题内容 -->
      <article v-else-if="question" class="question-detail">
        <!-- 问题头部 -->
        <header class="question-header">
          <div class="question-tags">
            <Badge
              v-for="tag in question.tags"
              :key="tag.id"
              variant="secondary"
              size="small"
            >
              {{ tag.name }}
            </Badge>
            <Badge v-if="question.relatedArticle" variant="outline" size="small">
              关联文章
            </Badge>
          </div>
          <h1 class="question-title">{{ question.title }}</h1>
          <div class="question-author-info">
            <Avatar
              :src="question.author.avatar"
              :name="question.author.nickname"
              size="medium"
            />
            <div class="author-details">
              <div class="author-name">{{ question.author.nickname }}</div>
              <div class="question-meta">
                <span>{{ formatRelativeTime(question.createdAt) }}</span>
                <span>·</span>
                <span>{{ formatLargeNumber(question.answerCount) }} 回答</span>
                <span>·</span>
                <span>{{ formatLargeNumber(question.followerCount) }} 关注</span>
              </div>
            </div>
          </div>
        </header>

        <!-- 问题描述 -->
        <div class="question-content">
          <div class="markdown-body" v-html="renderedDescription"></div>
        </div>

        <!-- 关联文章 -->
        <div v-if="question.relatedArticle" class="question-related-article">
          <Card>
            <div class="related-article-content">
              <Badge variant="outline">关联文章</Badge>
              <router-link
                :to="`/articles/${question.relatedArticle.id}`"
                class="related-article-link"
              >
                {{ question.relatedArticle.title }}
              </router-link>
            </div>
          </Card>
        </div>

        <!-- 互动操作栏 -->
        <div class="question-actions">
          <Button
            :variant="question.isFollowing ? 'primary' : 'outline'"
            :loading="followLoading"
            @click="handleFollow"
          >
            {{ question.isFollowing ? '已关注' : '关注问题' }}
          </Button>
          <Button
            v-if="isAuthor"
            variant="outline"
            @click="handleEdit"
          >
            编辑问题
          </Button>
          <Button
            v-if="isAuthor"
            variant="outline"
            @click="handleDelete"
          >
            删除问题
          </Button>
        </div>

        <!-- 回答区域 -->
        <section class="question-answers">
          <div class="answers-header">
            <h3 class="answers-title">回答 · {{ question.answerCount }}</h3>
            <div class="answers-sort">
              <button
                :class="['sort-btn', { active: answerSort === 'votes' }]"
                @click="answerSort = 'votes'"
              >
                按投票排序
              </button>
              <button
                :class="['sort-btn', { active: answerSort === 'latest' }]"
                @click="answerSort = 'latest'"
              >
                按时间排序
              </button>
            </div>
          </div>

          <!-- 回答编辑器 -->
          <div v-if="userStore.isLoggedIn" class="answer-editor-section">
            <Card>
              <div class="answer-editor-header">
                <Avatar
                  :src="userStore.userInfo?.avatar"
                  :name="userStore.userInfo?.nickname"
                  size="small"
                />
                <span class="answer-editor-hint">写下你的回答...</span>
              </div>
              <div class="answer-editor-body">
                <textarea
                  v-model="answerContent"
                  class="answer-textarea"
                  placeholder="支持 Markdown 语法，最多 10000 字符"
                  :maxlength="10000"
                  rows="6"
                ></textarea>
                <div class="answer-editor-footer">
                  <span class="answer-char-count">{{ answerContent.length }}/10000</span>
                  <Button
                    variant="primary"
                    :loading="submittingAnswer"
                    :disabled="!answerContent.trim()"
                    @click="handleSubmitAnswer"
                  >
                    发布回答
                  </Button>
                </div>
              </div>
            </Card>
          </div>
          <div v-else class="answer-login-prompt">
            <Card>
              <p>请先登录后再回答问题</p>
              <Button variant="primary" to="/login">立即登录</Button>
            </Card>
          </div>

          <!-- 回答列表 -->
          <div v-if="answersLoading && answers.length === 0" class="loading-container">
            <Loading text="加载中..." />
          </div>
          <div v-else-if="answers.length === 0" class="empty-answers">
            <p>暂无回答，来发表第一个回答吧~</p>
          </div>
          <div v-else class="answers-list">
            <div
              v-for="answer in sortedAnswers"
              :key="answer.id"
              :class="['answer-item', { 'answer-item-best': answer.isBest }]"
            >
              <Card>
                <div class="answer-header">
                  <div class="answer-author">
                    <Avatar
                      :src="answer.author.avatar"
                      :name="answer.author.nickname"
                      size="small"
                    />
                    <div class="answer-author-info">
                      <span class="answer-author-name">{{ answer.author.nickname }}</span>
                      <span class="answer-time">{{ formatRelativeTime(answer.createdAt) }}</span>
                    </div>
                  </div>
                  <div v-if="answer.isBest" class="answer-best-badge">
                    <Badge variant="primary">最佳回答</Badge>
                  </div>
                </div>
                <div class="answer-content">
                  <div class="markdown-body" v-html="renderAnswerContent(answer.content)"></div>
                </div>
                <div class="answer-footer">
                  <div class="answer-votes">
                    <button
                      :class="['vote-btn', 'vote-up', { active: answer.voteType === 'upvote' }]"
                      @click="handleVote(answer, 'upvote')"
                      :disabled="voteLoading"
                    >
                      ▲ {{ answer.upvoteCount || 0 }}
                    </button>
                    <button
                      :class="['vote-btn', 'vote-down', { active: answer.voteType === 'downvote' }]"
                      @click="handleVote(answer, 'downvote')"
                      :disabled="voteLoading"
                    >
                      ▼ {{ answer.downvoteCount || 0 }}
                    </button>
                  </div>
                  <div class="answer-actions">
                    <button
                      v-if="isAuthor && !answer.isBest"
                      class="action-btn"
                      @click="handleMarkBest(answer.id)"
                    >
                      设为最佳回答
                    </button>
                    <button
                      v-if="canEditAnswer(answer)"
                      class="action-btn"
                      @click="handleEditAnswer(answer)"
                    >
                      编辑
                    </button>
                    <button
                      v-if="canDeleteAnswer(answer)"
                      class="action-btn danger"
                      @click="handleDeleteAnswer(answer.id)"
                    >
                      删除
                    </button>
                  </div>
                </div>
              </Card>
            </div>
          </div>
        </section>

        <!-- 评论区域 -->
        <section class="question-comments">
          <h3 class="comments-title">评论 · {{ question.commentCount || 0 }}</h3>
          <CommentList
            :target-type="'answer'"
            :target-id="question.id"
            :is-author="isAuthor"
            :show-input="false"
            @comment-added="handleCommentAdded"
            @comment-updated="handleCommentUpdated"
          />
        </section>
      </article>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  getQuestionDetail,
  deleteQuestion,
  followQuestion,
  unfollowQuestion,
  getAnswerList,
  createAnswer,
  deleteAnswer,
  voteAnswer,
  cancelVote,
  markBestAnswer,
} from '@/api/question'
import { formatRelativeTime, formatLargeNumber } from '@/utils/format'
import Card from '@/components/Card.vue'
import Button from '@/components/Button.vue'
import Badge from '@/components/Badge.vue'
import Avatar from '@/components/Avatar.vue'
import Loading from '@/components/Loading.vue'
import CommentList from '@/components/CommentList.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const question = ref(null)
const answers = ref([])
const loading = ref(false)
const answersLoading = ref(false)
const followLoading = ref(false)
const submittingAnswer = ref(false)
const voteLoading = ref(false)
const answerContent = ref('')
const answerSort = ref('votes') // 'votes' 或 'latest'

const isAuthor = computed(() => {
  return question.value && userStore.userInfo && question.value.author.id === userStore.userInfo.id
})

// 简单的 Markdown 渲染
const renderedDescription = computed(() => {
  if (!question.value || !question.value.description) return ''
  
  let content = question.value.description
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
  
  // 换行
  content = content.replace(/\n/g, '<br>')
  
  return content
})

function renderAnswerContent(content) {
  if (!content) return ''
  
  let text = content
  // 转义 HTML
  text = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
  
  // 粗体和斜体
  text = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  text = text.replace(/\*(.*?)\*/g, '<em>$1</em>')
  
  // 代码
  text = text.replace(/`([^`]+)`/g, '<code>$1</code>')
  
  // 链接
  text = text.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
  
  // 换行
  text = text.replace(/\n/g, '<br>')
  
  return text
}

// 排序后的回答
const sortedAnswers = computed(() => {
  const sorted = [...answers.value]
  if (answerSort.value === 'votes') {
    sorted.sort((a, b) => {
      // 最佳回答在前
      if (a.isBest && !b.isBest) return -1
      if (!a.isBest && b.isBest) return 1
      // 按投票数排序
      const aVotes = (a.upvoteCount || 0) - (a.downvoteCount || 0)
      const bVotes = (b.upvoteCount || 0) - (b.downvoteCount || 0)
      return bVotes - aVotes
    })
  } else {
    sorted.sort((a, b) => {
      // 最佳回答在前
      if (a.isBest && !b.isBest) return -1
      if (!a.isBest && b.isBest) return 1
      // 按时间排序
      return new Date(b.createdAt) - new Date(a.createdAt)
    })
  }
  return sorted
})

// 获取问题详情
async function fetchQuestionDetail() {
  loading.value = true
  try {
    const id = route.params.id
    const response = await getQuestionDetail(id)

    if (response.data) {
      question.value = response.data
      // 获取回答列表
      fetchAnswers()
    }
  } catch (error) {
    console.error('获取问题详情失败:', error)
    alert(error.message || '获取问题详情失败，请重试')
  } finally {
    loading.value = false
  }
}

// 获取回答列表
async function fetchAnswers() {
  if (!question.value) return

  answersLoading.value = true
  try {
    const response = await getAnswerList(question.value.id, {
      sort: answerSort.value,
    })

    if (response.data && response.data.items) {
      answers.value = response.data.items
    }
  } catch (error) {
    console.error('获取回答列表失败:', error)
    alert(error.message || '获取回答列表失败，请重试')
  } finally {
    answersLoading.value = false
  }
}

// 关注/取消关注
async function handleFollow() {
  if (!userStore.isLoggedIn) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }

  followLoading.value = true
  try {
    if (question.value.isFollowing) {
      await unfollowQuestion(question.value.id)
      question.value.isFollowing = false
      question.value.followerCount -= 1
    } else {
      await followQuestion(question.value.id)
      question.value.isFollowing = true
      question.value.followerCount += 1
    }
  } catch (error) {
    console.error('关注操作失败:', error)
    alert(error.message || '操作失败，请重试')
  } finally {
    followLoading.value = false
  }
}

// 编辑问题
function handleEdit() {
  router.push({ name: 'question-edit', params: { id: question.value.id } })
}

// 删除问题
async function handleDelete() {
  if (!confirm('确定要删除这个问题吗？')) {
    return
  }

  try {
    await deleteQuestion(question.value.id)
    alert('删除成功')
    router.push({ name: 'questions' })
  } catch (error) {
    console.error('删除问题失败:', error)
    alert(error.message || '删除失败，请重试')
  }
}

// 提交回答
async function handleSubmitAnswer() {
  if (!userStore.isLoggedIn) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }

  if (!answerContent.value.trim()) {
    alert('请输入回答内容')
    return
  }

  submittingAnswer.value = true
  try {
    const response = await createAnswer(question.value.id, {
      content: answerContent.value.trim(),
    })

    if (response.data) {
      answerContent.value = ''
      // 刷新回答列表
      fetchAnswers()
      // 更新问题回答数
      question.value.answerCount += 1
    }
  } catch (error) {
    console.error('发布回答失败:', error)
    alert(error.message || '发布回答失败，请重试')
  } finally {
    submittingAnswer.value = false
  }
}

// 投票
async function handleVote(answer, type) {
  if (!userStore.isLoggedIn) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }

  voteLoading.value = true
  try {
    if (answer.voteType === type) {
      // 取消投票
      await cancelVote(answer.id)
      if (type === 'upvote') {
        answer.upvoteCount = Math.max(0, (answer.upvoteCount || 0) - 1)
      } else {
        answer.downvoteCount = Math.max(0, (answer.downvoteCount || 0) - 1)
      }
      answer.voteType = null
    } else {
      // 投票
      await voteAnswer(answer.id, type)
      if (answer.voteType) {
        // 如果之前有投票，先取消
        if (answer.voteType === 'upvote') {
          answer.upvoteCount = Math.max(0, (answer.upvoteCount || 0) - 1)
        } else {
          answer.downvoteCount = Math.max(0, (answer.downvoteCount || 0) - 1)
        }
      }
      if (type === 'upvote') {
        answer.upvoteCount = (answer.upvoteCount || 0) + 1
      } else {
        answer.downvoteCount = (answer.downvoteCount || 0) + 1
      }
      answer.voteType = type
    }
  } catch (error) {
    console.error('投票操作失败:', error)
    alert(error.message || '操作失败，请重试')
  } finally {
    voteLoading.value = false
  }
}

// 标记最佳回答
async function handleMarkBest(answerId) {
  if (!confirm('确定要将此回答设为最佳回答吗？')) {
    return
  }

  try {
    await markBestAnswer(question.value.id, answerId)
    // 刷新回答列表
    fetchAnswers()
    alert('已设为最佳回答')
  } catch (error) {
    console.error('标记最佳回答失败:', error)
    alert(error.message || '操作失败，请重试')
  }
}

// 编辑回答
function handleEditAnswer(answer) {
  // TODO: 实现回答编辑功能
  alert('回答编辑功能开发中...')
}

// 删除回答
async function handleDeleteAnswer(answerId) {
  if (!confirm('确定要删除这个回答吗？')) {
    return
  }

  try {
    await deleteAnswer(answerId)
    // 刷新回答列表
    fetchAnswers()
    // 更新问题回答数
    question.value.answerCount -= 1
  } catch (error) {
    console.error('删除回答失败:', error)
    alert(error.message || '删除失败，请重试')
  }
}

// 权限判断
function canEditAnswer(answer) {
  return userStore.userInfo && answer.author.id === userStore.userInfo.id
}

function canDeleteAnswer(answer) {
  return (
    (userStore.userInfo && answer.author.id === userStore.userInfo.id) ||
    isAuthor.value
  )
}

// 评论相关
function handleCommentAdded() {
  if (question.value) {
    question.value.commentCount = (question.value.commentCount || 0) + 1
  }
}

function handleCommentUpdated() {
  fetchQuestionDetail()
}

// 监听排序变化
watch(answerSort, () => {
  fetchAnswers()
})

// 组件挂载时获取数据
onMounted(() => {
  fetchQuestionDetail()
})
</script>

<style scoped>
.question-detail-page {
  min-height: 100vh;
  padding: 40px 0;
}

.back-link {
  display: inline-block;
  margin-bottom: 24px;
  color: var(--midnight-purple);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.back-link:hover {
  color: var(--coral-pink);
}

.loading-container {
  padding: 80px 20px;
  text-align: center;
}

.question-detail {
  max-width: 900px;
  margin: 0 auto;
}

.question-header {
  margin-bottom: 32px;
}

.question-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.question-title {
  font-size: 36px;
  font-weight: 700;
  color: var(--midnight-purple);
  line-height: 1.3;
  margin-bottom: 20px;
}

.question-author-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.author-details {
  flex: 1;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin-bottom: 4px;
}

.question-meta {
  font-size: 14px;
  color: var(--text-dark);
  opacity: 0.7;
}

.question-content {
  margin-bottom: 32px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
  line-height: 1.8;
}

.question-related-article {
  margin-bottom: 24px;
}

.related-article-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
}

.related-article-link {
  color: var(--coral-pink);
  text-decoration: none;
  font-weight: 500;
  transition: opacity 0.2s;
}

.related-article-link:hover {
  opacity: 0.8;
}

.question-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 40px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border);
}

.question-answers {
  margin-bottom: 40px;
}

.answers-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.answers-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--midnight-purple);
}

.answers-sort {
  display: flex;
  gap: 8px;
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

.answer-editor-section,
.answer-login-prompt {
  margin-bottom: 24px;
}

.answer-editor-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.answer-editor-hint {
  font-size: 14px;
  color: var(--text-dark);
  opacity: 0.7;
}

.answer-textarea {
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

.answer-textarea:focus {
  border-color: var(--coral-pink);
}

.answer-editor-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.answer-char-count {
  font-size: 12px;
  color: var(--text-dark);
  opacity: 0.6;
}

.empty-answers {
  padding: 60px 20px;
  text-align: center;
  color: var(--text-dark);
  opacity: 0.6;
}

.answers-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.answer-item {
  position: relative;
}

.answer-item-best {
  border-left: 3px solid var(--coral-pink);
}

.answer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.answer-author {
  display: flex;
  align-items: center;
  gap: 12px;
}

.answer-author-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.answer-author-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--midnight-purple);
}

.answer-time {
  font-size: 13px;
  color: var(--text-dark);
  opacity: 0.6;
}

.answer-content {
  margin-bottom: 16px;
  line-height: 1.8;
}

.answer-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid var(--border);
}

.answer-votes {
  display: flex;
  gap: 8px;
}

.vote-btn {
  padding: 6px 12px;
  background: transparent;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-dark);
  cursor: pointer;
  transition: all 0.2s;
}

.vote-btn:hover {
  border-color: var(--coral-pink);
  color: var(--coral-pink);
}

.vote-btn.active {
  background: var(--coral-pink);
  border-color: var(--coral-pink);
  color: white;
}

.vote-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.answer-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 4px 8px;
  background: transparent;
  border: none;
  font-size: 13px;
  color: var(--text-dark);
  opacity: 0.7;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  opacity: 1;
  color: var(--coral-pink);
}

.action-btn.danger:hover {
  color: #ff4757;
}

.question-comments {
  margin-top: 40px;
  padding-top: 40px;
  border-top: 1px solid var(--border);
}

.comments-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin-bottom: 24px;
}

@media (max-width: 768px) {
  .question-title {
    font-size: 28px;
  }

  .question-actions {
    flex-wrap: wrap;
  }

  .answers-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>

