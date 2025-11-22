<template>
  <div class="home-page">
    <!-- 未登录时展示欢迎页 -->
    <div v-if="!isLoggedIn" class="hero-section">
      <div class="glow glow-right"></div>
      <div class="app-shell">
        <section class="hero">
          <div class="hero-content">
            <Badge>Coral ✦ Stardust Community</Badge>
            <h1 class="hero-title">让青春与浪漫在星尘中相遇</h1>
            <p class="hero-description">
              文章、视频、问答多形态内容在这里交织，
              以动漫般的视觉语言，记录每一个灵感、情绪与互动。
            </p>
            <div class="hero-actions">
              <Button variant="primary" size="large" to="/feed">探索内容</Button>
              <Button variant="outline" size="large" to="/auth/register">立即加入</Button>
            </div>
            <div class="stats-bar">
              <div class="stat-item">
                <div class="stat-label">创作者</div>
                <div class="stat-value">1280</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">互动数</div>
                <div class="stat-value">84.2k</div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>

    <!-- 登录后展示Feed流 -->
    <Feed v-else />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import Feed from './Feed.vue'
import Button from '@/components/Button.vue'
import Badge from '@/components/Badge.vue'

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)
</script>

<style scoped>
.home-page {
  min-height: 100vh;
}

.hero-section {
  position: relative;
  min-height: 80vh;
  display: flex;
  align-items: center;
  padding: 80px 0;
}

.glow-right {
  position: absolute;
  right: -120px;
  top: -80px;
  width: 320px;
  height: 320px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 126, 138, 0.35), transparent 70%);
  filter: blur(40px);
  z-index: 0;
}

.hero {
  position: relative;
  z-index: 1;
}

.hero-content {
  max-width: 800px;
  text-align: center;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  color: var(--midnight-purple);
  margin: 24px 0;
  line-height: 1.2;
}

.hero-description {
  font-size: 18px;
  color: var(--text-dark);
  opacity: 0.8;
  line-height: 1.8;
  margin-bottom: 40px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 60px;
}

.stats-bar {
  display: flex;
  gap: 40px;
  justify-content: center;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: var(--text-dark);
  opacity: 0.7;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--coral-pink);
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 36px;
  }

  .hero-description {
    font-size: 16px;
  }

  .hero-actions {
    flex-direction: column;
    align-items: center;
  }

  .stats-bar {
    flex-direction: column;
    gap: 24px;
  }
}
</style>

