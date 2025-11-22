<template>
  <div id="app">
    <div class="glow glow-right"></div>
    <header class="app-shell">
      <Navbar />
    </header>
    <main class="app-main">
      <router-view />
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

.glow-right {
  right: -120px;
  top: -80px;
}

.app-shell {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 5%;
  position: relative;
  z-index: 2;
}

.app-main {
  position: relative;
  z-index: 2;
}
</style>
