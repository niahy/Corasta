<template>
  <div class="not-found-page">
    <div class="app-shell">
      <div class="not-found-content">
        <div class="not-found-illustration">
          <div class="error-code">404</div>
          <div class="error-stars">
            <span>✦</span>
            <span>✦</span>
            <span>✦</span>
          </div>
        </div>
        <h1 class="not-found-title">页面不存在</h1>
        <p class="not-found-description">
          抱歉，你访问的页面已经消失在星尘中了...
        </p>
        <div class="not-found-actions">
          <Button variant="primary" to="/">返回首页</Button>
          <Button variant="outline" @click="goBack">返回上一页</Button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import Button from '@/components/Button.vue'

const router = useRouter()

function goBack() {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.not-found-content {
  text-align: center;
  max-width: 600px;
  margin: 0 auto;
}

.not-found-illustration {
  position: relative;
  margin-bottom: 40px;
}

.error-code {
  font-size: 120px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--coral-pink), var(--stardust-purple));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  margin-bottom: 20px;
}

.error-stars {
  display: flex;
  justify-content: center;
  gap: 20px;
  font-size: 32px;
  color: var(--stardust-purple);
  opacity: 0.6;
}

.not-found-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--midnight-purple);
  margin-bottom: 16px;
}

.not-found-description {
  font-size: 16px;
  color: var(--text-dark);
  opacity: 0.8;
  line-height: 1.6;
  margin-bottom: 32px;
}

.not-found-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

@media (max-width: 768px) {
  .error-code {
    font-size: 80px;
  }

  .not-found-title {
    font-size: 24px;
  }

  .not-found-actions {
    flex-direction: column;
    align-items: center;
  }

  .not-found-actions button {
    width: 100%;
    max-width: 300px;
  }
}
</style>

