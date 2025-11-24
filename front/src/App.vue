<template>
  <div id="app">
    <div class="orb orb-left"></div>
    <div class="orb orb-right"></div>
    <div class="grain"></div>
    <header class="app-shell glass-surface app-nav-shell">
      <Navbar />
    </header>
    <main class="app-main">
      <section class="app-content glass-surface">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useUserStore } from '@/stores/user';
import Navbar from '@/components/Navbar.vue';

const userStore = useUserStore();

onMounted(() => {
  // 初始化用户信息
  userStore.initUser();
});
</script>

<style>
@import '@/assets/styles/variables.css';
@import '@/assets/styles/global.css';

#app {
  min-height: 100vh;
  position: relative;
  padding: 32px 0 64px;
}

.orb {
  position: fixed;
  width: clamp(240px, 35vw, 420px);
  height: clamp(240px, 35vw, 420px);
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 126, 138, 0.3), transparent 70%);
  filter: blur(50px);
  z-index: 0;
  pointer-events: none;
}

.orb-left {
  top: -120px;
  left: -80px;
  background: radial-gradient(circle, rgba(166, 140, 224, 0.4), transparent 70%);
}

.orb-right {
  right: -120px;
  top: 20%;
}

.glow {
  position: fixed;
  width: 320px;
  height: 320px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 126, 138, 0.35), transparent 70%);
  filter: blur(60px);
  z-index: 0;
  pointer-events: none;
}

.grain {
  position: fixed;
  inset: 0;
  pointer-events: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='160' height='160' viewBox='0 0 160 160'%3E%3Cg fill='none' fill-opacity='0.12'%3E%3Cpath fill='%23ffffff' d='M0 0h1v1H0z'/%3E%3C/g%3E%3C/svg%3E");
  opacity: 0.6;
  mix-blend-mode: overlay;
  z-index: 1;
}

.app-shell {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 5%;
  position: relative;
  z-index: 2;
}

.app-nav-shell {
  padding: 28px clamp(16px, 4vw, 48px);
  border-radius: 32px;
  position: sticky;
  top: 24px;
  z-index: 5;
  backdrop-filter: blur(32px);
  -webkit-backdrop-filter: blur(32px);
}

.app-main {
  position: relative;
  z-index: 2;
  max-width: 1200px;
  margin: 40px auto 0;
  padding: 0 clamp(16px, 4vw, 48px);
}

.app-content {
  min-height: calc(100vh - 200px);
  padding: clamp(24px, 5vw, 48px);
  border-radius: 36px;
}
</style>
