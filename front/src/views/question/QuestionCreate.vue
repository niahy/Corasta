<template>
  <div class="question-create-page">
    <div class="app-shell">
      <div class="editor-container">
        <!-- 编辑器头部 -->
        <header class="editor-header">
          <router-link to="/questions" class="back-link">← 返回问答</router-link>
          <div class="editor-actions">
            <Button variant="primary" @click="handleSubmit" :loading="submitting">
              发布问题
            </Button>
          </div>
        </header>

        <!-- 问题表单 -->
        <Card class="editor-meta">
          <div class="meta-form">
            <div class="meta-row">
              <Input
                v-model="form.title"
                label="问题标题"
                placeholder="请输入问题标题（1-100字符）"
                :maxlength="100"
                :error="errors.title"
                required
                class="title-input"
              />
            </div>
            <div class="meta-row">
              <div class="meta-field">
                <label>问题描述 <span class="required">*</span></label>
                <textarea
                  v-model="form.description"
                  class="description-textarea"
                  placeholder="详细描述你的问题，支持 Markdown 语法，最多 5000 字符"
                  :maxlength="5000"
                  rows="10"
                ></textarea>
                <div class="char-count">{{ form.description.length }}/5000</div>
                <div v-if="errors.description" class="error-message">{{ errors.description }}</div>
              </div>
            </div>
            <div class="meta-row">
              <div class="meta-field">
                <label>标签（最多5个）</label>
                <div class="tags-section">
                  <div class="tags-input-wrapper">
                    <Input
                      v-model="tagInput"
                      placeholder="输入标签名称，按回车添加"
                      @keyup.enter="addTag"
                    />
                    <Button variant="outline" size="small" @click="addTag">添加</Button>
                  </div>
                  <div v-if="form.tags.length > 0" class="tags-list">
                    <Badge
                      v-for="(tag, index) in form.tags"
                      :key="index"
                      variant="secondary"
                      class="tag-item"
                    >
                      {{ tag }}
                      <button class="tag-remove" @click="removeTag(index)">×</button>
                    </Badge>
                  </div>
                  <div v-if="errors.tags" class="error-message">{{ errors.tags }}</div>
                </div>
              </div>
            </div>
            <div class="meta-row">
              <div class="meta-field">
                <label>关联文章（可选）</label>
                <Input
                  v-model="relatedArticleInput"
                  placeholder="输入文章ID或标题搜索"
                  @keyup.enter="searchArticle"
                >
                  <template #suffix>
                    <Button variant="text" size="small" @click="searchArticle">搜索</Button>
                  </template>
                </Input>
                <div v-if="form.relatedArticleId" class="related-article-info">
                  <Badge variant="outline">已关联文章 ID: {{ form.relatedArticleId }}</Badge>
                  <Button variant="text" size="small" @click="clearRelatedArticle">清除</Button>
                </div>
              </div>
            </div>
          </div>
        </Card>

        <!-- 预览区域 -->
        <Card class="preview-section">
          <template #header>
            <h3 class="preview-title">预览</h3>
          </template>
          <div class="preview-content">
            <h2 class="preview-question-title">{{ form.title || '问题标题' }}</h2>
            <div class="markdown-body" v-html="renderedDescription"></div>
            <div v-if="form.tags.length > 0" class="preview-tags">
              <Badge
                v-for="(tag, index) in form.tags"
                :key="index"
                variant="secondary"
                size="small"
              >
                {{ tag }}
              </Badge>
            </div>
          </div>
        </Card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { createQuestion } from '@/api/question'
import Card from '@/components/Card.vue'
import Button from '@/components/Button.vue'
import Input from '@/components/Input.vue'
import Badge from '@/components/Badge.vue'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  title: '',
  description: '',
  tags: [],
  relatedArticleId: null,
})

const errors = reactive({
  title: '',
  description: '',
  tags: '',
})

const tagInput = ref('')
const relatedArticleInput = ref('')
const submitting = ref(false)

// 简单的 Markdown 渲染
const renderedDescription = computed(() => {
  if (!form.description) return '<p class="preview-placeholder">问题描述预览...</p>'
  
  let content = form.description
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

// 添加标签
function addTag() {
  const tag = tagInput.value.trim()
  if (!tag) return

  // 检查标签数量
  if (form.tags.length >= 5) {
    errors.tags = '最多只能添加5个标签'
    return
  }

  // 检查是否已存在
  if (form.tags.includes(tag)) {
    errors.tags = '标签已存在'
    return
  }

  form.tags.push(tag)
  tagInput.value = ''
  errors.tags = ''
}

// 移除标签
function removeTag(index) {
  form.tags.splice(index, 1)
  errors.tags = ''
}

// 搜索文章
function searchArticle() {
  const input = relatedArticleInput.value.trim()
  if (!input) return

  // 简单处理：如果是数字，直接作为ID
  const articleId = parseInt(input)
  if (!isNaN(articleId)) {
    form.relatedArticleId = articleId
    relatedArticleInput.value = ''
  } else {
    // TODO: 实现文章搜索功能
    alert('文章搜索功能开发中，请直接输入文章ID')
  }
}

// 清除关联文章
function clearRelatedArticle() {
  form.relatedArticleId = null
  relatedArticleInput.value = ''
}

// 表单验证
function validateForm() {
  let isValid = true

  // 清除之前的错误
  Object.keys(errors).forEach((key) => {
    errors[key] = ''
  })

  // 验证标题
  if (!form.title.trim()) {
    errors.title = '请输入问题标题'
    isValid = false
  } else if (form.title.length < 1 || form.title.length > 100) {
    errors.title = '标题长度必须在1-100字符之间'
    isValid = false
  }

  // 验证描述
  if (!form.description.trim()) {
    errors.description = '请输入问题描述'
    isValid = false
  } else if (form.description.length > 5000) {
    errors.description = '描述长度不能超过5000字符'
    isValid = false
  }

  // 验证标签
  if (form.tags.length > 5) {
    errors.tags = '最多只能添加5个标签'
    isValid = false
  }

  return isValid
}

// 提交问题
async function handleSubmit() {
  if (!userStore.isLoggedIn) {
    router.push({ name: 'login', query: { redirect: '/questions/create' } })
    return
  }

  if (!validateForm()) {
    return
  }

  submitting.value = true
  try {
    const data = {
      title: form.title.trim(),
      description: form.description.trim(),
      tagNames: form.tags,
      relatedArticleId: form.relatedArticleId || undefined,
    }

    const response = await createQuestion(data)

    if (response.data) {
      alert('问题发布成功')
      router.push({ name: 'question-detail', params: { id: response.data.id } })
    }
  } catch (error) {
    console.error('发布问题失败:', error)
    alert(error.message || '发布问题失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.question-create-page {
  min-height: 100vh;
  padding: 40px 0;
}

.editor-container {
  max-width: 1200px;
  margin: 0 auto;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.back-link {
  color: var(--midnight-purple);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.back-link:hover {
  color: var(--coral-pink);
}

.editor-actions {
  display: flex;
  gap: 12px;
}

.editor-meta {
  margin-bottom: 24px;
}

.meta-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.meta-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-field label {
  font-size: 14px;
  font-weight: 500;
  color: var(--midnight-purple);
}

.required {
  color: var(--coral-pink);
}

.title-input {
  width: 100%;
}

.description-textarea {
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

.description-textarea:focus {
  border-color: var(--coral-pink);
}

.char-count {
  font-size: 12px;
  color: var(--text-dark);
  opacity: 0.6;
  text-align: right;
}

.error-message {
  font-size: 12px;
  color: #ff4757;
  margin-top: 4px;
}

.tags-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tags-input-wrapper {
  display: flex;
  gap: 8px;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
}

.tag-remove {
  background: transparent;
  border: none;
  font-size: 18px;
  color: var(--text-dark);
  opacity: 0.6;
  cursor: pointer;
  padding: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.tag-remove:hover {
  opacity: 1;
  color: #ff4757;
}

.related-article-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.preview-section {
  margin-bottom: 24px;
}

.preview-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin: 0;
}

.preview-content {
  padding: 20px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border);
}

.preview-question-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--midnight-purple);
  margin-bottom: 16px;
}

.preview-placeholder {
  color: var(--text-dark);
  opacity: 0.5;
  font-style: italic;
}

.preview-tags {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  flex-wrap: wrap;
}

.markdown-body {
  line-height: 1.8;
  color: var(--text-dark);
}

.markdown-body :deep(strong) {
  font-weight: 600;
  color: var(--midnight-purple);
}

.markdown-body :deep(em) {
  font-style: italic;
}

.markdown-body :deep(code) {
  background: rgba(166, 140, 224, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
}

.markdown-body :deep(a) {
  color: var(--coral-pink);
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .editor-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .tags-input-wrapper {
    flex-direction: column;
  }
}
</style>

