<template>
  <div class="article-editor-page">
    <div class="app-shell">
      <div class="editor-container">
        <!-- 编辑器头部 -->
        <header class="editor-header">
          <router-link to="/articles" class="back-link">← 返回</router-link>
          <div class="editor-actions">
            <Button variant="outline" @click="handleSaveDraft" :loading="saving">
              保存草稿
            </Button>
            <Button variant="primary" @click="handlePublish" :loading="publishing">
              {{ isEditMode ? '更新文章' : '发布文章' }}
            </Button>
          </div>
        </header>

        <!-- 文章元数据表单 -->
        <Card class="editor-meta">
          <div class="meta-form">
            <div class="meta-row">
              <Input
                v-model="form.title"
                label="文章标题"
                placeholder="请输入文章标题（1-100字符）"
                :maxlength="100"
                required
                class="title-input"
              />
            </div>
            <div class="meta-row">
              <Input
                v-model="form.summary"
                label="文章摘要"
                placeholder="可选，0-200字符"
                :maxlength="200"
                class="summary-input"
              />
            </div>
            <div class="meta-row meta-row-split">
              <div class="meta-field">
                <label>分类</label>
                <select v-model="form.categoryId">
                  <option :value="null">选择分类</option>
                  <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                    {{ cat.name }}
                  </option>
                </select>
              </div>
              <div class="meta-field">
                <label>发布状态</label>
                <select v-model="form.status">
                  <option :value="0">草稿</option>
                  <option :value="1">立即发布</option>
                  <option :value="2">私密</option>
                </select>
              </div>
            </div>
            <div class="meta-row">
              <div class="meta-field">
                <label>标签（最多10个，用逗号分隔）</label>
                <Input
                  v-model="tagsInput"
                  placeholder="例如：Java, Spring Boot, 后端开发"
                  @blur="handleTagsInput"
                />
              </div>
            </div>
            <div class="meta-row">
              <div class="meta-field">
                <label>封面图</label>
                <div class="cover-upload">
                  <img v-if="form.coverImage" :src="form.coverImage" alt="封面" class="cover-preview" />
                  <div v-else class="cover-placeholder">
                    <span>点击上传封面图</span>
                  </div>
                  <input
                    type="file"
                    accept="image/jpeg,image/png"
                    @change="handleCoverUpload"
                    class="cover-input"
                  />
                </div>
              </div>
            </div>
            <div class="meta-row">
              <div class="meta-field">
                <label>自定义URL（可选，SEO友好）</label>
                <Input
                  v-model="form.slug"
                  placeholder="例如：my-article-title"
                />
              </div>
            </div>
          </div>
        </Card>

        <!-- 编辑器主体 -->
        <div class="editor-main">
          <!-- 编辑器工具栏 -->
          <div class="editor-toolbar">
            <div class="toolbar-left">
              <button class="toolbar-btn" @click="insertMarkdown('**', '**')" title="加粗">
                <strong>B</strong>
              </button>
              <button class="toolbar-btn" @click="insertMarkdown('*', '*')" title="斜体">
                <em>I</em>
              </button>
              <button class="toolbar-btn" @click="insertMarkdown('`', '`')" title="代码">
                &lt;/&gt;
              </button>
              <button class="toolbar-btn" @click="insertMarkdown('[](', ')')" title="链接">
                🔗
              </button>
              <button class="toolbar-btn" @click="handleImageUpload" title="上传图片">
                🖼️
              </button>
            </div>
            <div class="toolbar-right">
              <button
                class="toolbar-btn"
                :class="{ active: previewMode === 'edit' }"
                @click="previewMode = 'edit'"
              >
                编辑
              </button>
              <button
                class="toolbar-btn"
                :class="{ active: previewMode === 'preview' }"
                @click="previewMode = 'preview'"
              >
                预览
              </button>
              <button
                class="toolbar-btn"
                :class="{ active: previewMode === 'split' }"
                @click="previewMode = 'split'"
              >
                分屏
              </button>
            </div>
          </div>

          <!-- 编辑器内容区 -->
          <div class="editor-content" :class="`mode-${previewMode}`">
            <!-- 编辑区 -->
            <div v-show="previewMode === 'edit' || previewMode === 'split'" class="editor-textarea">
              <textarea
                v-model="form.content"
                placeholder="开始撰写你的文章...支持 Markdown 语法"
                class="markdown-editor"
                @input="handleContentChange"
              ></textarea>
            </div>

            <!-- 预览区 -->
            <div v-show="previewMode === 'preview' || previewMode === 'split'" class="editor-preview">
              <div class="markdown-body" v-html="renderedPreview"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { createArticle, updateArticle, getArticleDetail, uploadArticleImage } from '@/api/article'
import Card from '@/components/Card.vue'
import Button from '@/components/Button.vue'
import Input from '@/components/Input.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 编辑模式判断
const isEditMode = computed(() => !!route.params.id)

// 表单数据
const form = reactive({
  title: '',
  content: '',
  summary: '',
  coverImage: '',
  slug: '',
  categoryId: null,
  tags: [],
  status: 0, // 0-草稿，1-已发布，2-私密
})

const tagsInput = ref('')
const categories = ref([]) // TODO: 从分类API获取
const previewMode = ref('split') // edit, preview, split
const saving = ref(false)
const publishing = ref(false)

// 自动保存定时器
let autoSaveTimer = null

// 简单的 Markdown 渲染（基础版本，后续可集成专业库）
const renderedPreview = computed(() => {
  if (!form.content) return '<p class="empty-preview">暂无内容，开始撰写吧...</p>'
  
  let content = form.content
  // 转义 HTML
  content = content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
  
  // 标题
  content = content.replace(/^### (.*$)/gim, '<h3>$1</h3>')
  content = content.replace(/^## (.*$)/gim, '<h2>$1</h2>')
  content = content.replace(/^# (.*$)/gim, '<h1>$1</h1>')
  
  // 粗体和斜体
  content = content.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  content = content.replace(/\*(.*?)\*/g, '<em>$1</em>')
  
  // 代码块
  content = content.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
  content = content.replace(/`([^`]+)`/g, '<code>$1</code>')
  
  // 链接
  content = content.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
  
  // 图片
  content = content.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" />')
  
  // 换行
  content = content.replace(/\n/g, '<br>')
  
  return content
})

// 监听内容变化，自动保存草稿
watch(
  () => form.content,
  () => {
    if (autoSaveTimer) {
      clearTimeout(autoSaveTimer)
    }
    autoSaveTimer = setTimeout(() => {
      if (form.title || form.content) {
        handleAutoSave()
      }
    }, 30000) // 30秒后自动保存
  }
)

// 内容变化处理
function handleContentChange() {
  // 可以在这里添加实时字数统计等功能
}

// 插入Markdown语法
function insertMarkdown(before, after) {
  const textarea = document.querySelector('.markdown-editor')
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = form.content.substring(start, end)
  const newText = before + selectedText + after

  form.content =
    form.content.substring(0, start) + newText + form.content.substring(end)

  // 恢复光标位置
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(
      start + before.length,
      start + before.length + selectedText.length
    )
  }, 0)
}

// 处理标签输入
function handleTagsInput() {
  const tags = tagsInput.value
    .split(',')
    .map((tag) => tag.trim())
    .filter((tag) => tag.length > 0)
    .slice(0, 10) // 最多10个标签
  form.tags = tags
  tagsInput.value = tags.join(', ')
}

// 处理封面图上传
async function handleCoverUpload(event) {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.match(/^image\/(jpeg|png)$/)) {
    alert('只支持 JPG 和 PNG 格式的图片')
    return
  }

  // 验证文件大小（最大5MB）
  if (file.size > 5 * 1024 * 1024) {
    alert('图片大小不能超过 5MB')
    return
  }

  try {
    const formData = new FormData()
    formData.append('file', file)

    const response = await uploadArticleImage(formData)
    if (response.data && response.data.url) {
      form.coverImage = response.data.url
    }
  } catch (error) {
    console.error('上传封面图失败:', error)
    alert(error.message || '上传失败，请重试')
  }
}

// 处理图片上传
async function handleImageUpload() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/jpeg,image/png,image/gif'
  input.onchange = async (event) => {
    const file = event.target.files[0]
    if (!file) return

    try {
      const formData = new FormData()
      formData.append('file', file)

      const response = await uploadArticleImage(formData)
      if (response.data && response.data.url) {
        // 在光标位置插入图片Markdown
        const textarea = document.querySelector('.markdown-editor')
        if (textarea) {
          const start = textarea.selectionStart
          const imageMarkdown = `![${file.name}](${response.data.url})`
          form.content =
            form.content.substring(0, start) +
            imageMarkdown +
            form.content.substring(start)
        }
      }
    } catch (error) {
      console.error('上传图片失败:', error)
      alert(error.message || '上传失败，请重试')
    }
  }
  input.click()
}

// 自动保存草稿
async function handleAutoSave() {
  if (!form.title && !form.content) return
  if (publishing.value) return

  try {
    // 静默保存，不显示loading
    if (isEditMode.value) {
      await updateArticle(route.params.id, {
        ...form,
        status: 0, // 自动保存为草稿
      })
    } else {
      // 创建新草稿
      await createArticle({
        ...form,
        status: 0,
      })
    }
    console.log('草稿已自动保存')
  } catch (error) {
    console.error('自动保存失败:', error)
  }
}

// 保存草稿
async function handleSaveDraft() {
  if (!form.title) {
    alert('请输入文章标题')
    return
  }

  saving.value = true
  try {
    if (isEditMode.value) {
      await updateArticle(route.params.id, {
        ...form,
        status: 0,
      })
      alert('草稿已保存')
    } else {
      const response = await createArticle({
        ...form,
        status: 0,
      })
      if (response.data) {
        router.replace(`/articles/edit/${response.data.id}`)
        alert('草稿已保存')
      }
    }
  } catch (error) {
    console.error('保存草稿失败:', error)
    alert(error.message || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

// 发布文章
async function handlePublish() {
  // 验证必填字段
  if (!form.title) {
    alert('请输入文章标题')
    return
  }
  if (form.title.length > 100) {
    alert('文章标题不能超过100字符')
    return
  }
  if (!form.content) {
    alert('请输入文章内容')
    return
  }

  publishing.value = true
  try {
    if (isEditMode.value) {
      await updateArticle(route.params.id, {
        ...form,
        status: form.status === 0 ? 1 : form.status, // 如果当前是草稿，发布时改为已发布
      })
      alert('文章已更新')
    } else {
      const response = await createArticle({
        ...form,
        status: form.status === 0 ? 1 : form.status,
      })
      if (response.data) {
        router.push(`/articles/${response.data.id}`)
        return
      }
    }
    router.push('/articles')
  } catch (error) {
    console.error('发布文章失败:', error)
    alert(error.message || '发布失败，请重试')
  } finally {
    publishing.value = false
  }
}

// 获取文章详情（编辑模式）
async function fetchArticleDetail() {
  try {
    const response = await getArticleDetail(route.params.id)
    if (response.data) {
      const article = response.data
      form.title = article.title || ''
      form.content = article.content || ''
      form.summary = article.summary || ''
      form.coverImage = article.coverImage || ''
      form.slug = article.slug || ''
      form.categoryId = article.category?.id || null
      form.tags = article.tags?.map((tag) => tag.name) || []
      form.status = article.status || 0

      tagsInput.value = form.tags.join(', ')
    }
  } catch (error) {
    console.error('获取文章详情失败:', error)
    alert(error.message || '获取文章详情失败，请重试')
    router.push('/articles')
  }
}

// 组件挂载
onMounted(() => {
  if (isEditMode.value) {
    fetchArticleDetail()
  }
})
</script>

<style scoped>
.article-editor-page {
  min-height: 100vh;
  padding: 40px 0;
}

.editor-container {
  max-width: 1400px;
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
  gap: 20px;
}

.meta-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-row-split {
  flex-direction: row;
  gap: 20px;
}

.meta-field {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-field label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-dark);
}

.meta-field select {
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-dark);
  background: white;
  cursor: pointer;
}

.title-input {
  width: 100%;
}

.summary-input {
  width: 100%;
}

.cover-upload {
  position: relative;
  width: 100%;
  max-width: 400px;
  height: 200px;
  border: 2px dashed var(--border);
  border-radius: var(--radius-sm);
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.2s;
}

.cover-upload:hover {
  border-color: var(--coral-pink);
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-dark);
  opacity: 0.6;
}

.cover-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

.editor-main {
  background: white;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow);
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
  background: #f8f9fa;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 8px;
}

.toolbar-btn {
  padding: 6px 12px;
  background: white;
  border: 1px solid var(--border);
  border-radius: 4px;
  font-size: 14px;
  color: var(--text-dark);
  cursor: pointer;
  transition: all 0.2s;
}

.toolbar-btn:hover {
  border-color: var(--coral-pink);
  color: var(--coral-pink);
}

.toolbar-btn.active {
  background: var(--coral-pink);
  border-color: var(--coral-pink);
  color: white;
}

.editor-content {
  display: flex;
  min-height: 600px;
}

.editor-content.mode-edit .editor-preview {
  display: none;
}

.editor-content.mode-preview .editor-textarea {
  display: none;
}

.editor-content.mode-split {
  display: grid;
  grid-template-columns: 1fr 1fr;
}

.editor-textarea {
  flex: 1;
  border-right: 1px solid var(--border);
}

.markdown-editor {
  width: 100%;
  height: 100%;
  min-height: 600px;
  padding: 20px;
  border: none;
  font-size: 15px;
  font-family: 'Courier New', monospace;
  line-height: 1.6;
  color: var(--text-dark);
  resize: none;
  outline: none;
}

.editor-preview {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #fafafa;
}

.markdown-body {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-dark);
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  color: var(--midnight-purple);
  margin-top: 24px;
  margin-bottom: 12px;
}

.markdown-body :deep(p) {
  margin-bottom: 12px;
}

.markdown-body :deep(code) {
  background: rgba(166, 140, 224, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
}

.markdown-body :deep(pre) {
  background: #f5f5f5;
  padding: 12px;
  border-radius: var(--radius-sm);
  overflow-x: auto;
  margin-bottom: 12px;
}

.markdown-body :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-sm);
  margin: 12px 0;
}

.empty-preview {
  color: var(--text-dark);
  opacity: 0.5;
  text-align: center;
  padding: 40px;
}

@media (max-width: 1024px) {
  .editor-content.mode-split {
    grid-template-columns: 1fr;
  }

  .editor-content.mode-split .editor-textarea {
    border-right: none;
    border-bottom: 1px solid var(--border);
  }
}
</style>

