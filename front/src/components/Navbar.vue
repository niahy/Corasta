<template>
  <nav class="navbar">
    <router-link class="brand" to="/">
      Corasta <span>星尘</span>
    </router-link>
    <div class="nav-links">
      <router-link
        v-for="link in navLinks"
        :key="link.path"
        :to="link.path"
        :class="{ active: $route.path === link.path }"
      >
        {{ link.label }}
      </router-link>
    </div>
    <div class="actions">
      <template v-if="!isLoggedIn">
        <router-link class="btn btn-outline" to="/login">登录</router-link>
        <router-link class="btn btn-primary" to="/register">立即加入</router-link>
      </template>
      <template v-else>
        <router-link class="btn btn-outline" to="/dashboard">创作后台</router-link>
        <div class="user-menu">
          <router-link :to="`/profile/${userStore.userInfo?.id}`" class="user-avatar">
            <img
              v-if="userStore.userInfo?.avatar"
              :src="userStore.userInfo.avatar"
              :alt="userStore.userInfo.username"
            />
            <span v-else class="avatar-placeholder">
              {{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() }}
            </span>
          </router-link>
        </div>
      </template>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue';
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();

const isLoggedIn = computed(() => userStore.isLoggedIn);

const navLinks = [
  { path: '/', label: '首页' },
  { path: '/feed', label: '社区' },
  { path: '/interaction', label: '互动中心' },
  { path: '/search', label: '搜索' },
];
</script>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
  width: 100%;
  gap: 24px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 700;
  font-size: 20px;
  color: var(--midnight-purple);
  letter-spacing: 0.04em;
  text-decoration: none;
  transition: opacity 0.2s;
}

.brand:hover {
  opacity: 0.8;
}

.brand span {
  color: var(--coral-pink);
}

.nav-links {
  display: flex;
  gap: 24px;
}

.nav-links a {
  text-decoration: none;
  color: var(--midnight-purple);
  font-weight: 500;
  font-size: 15px;
  position: relative;
  padding-bottom: 4px;
  transition: color 0.2s;
}

.nav-links a:hover {
  color: var(--coral-pink);
}

.nav-links a.active {
  color: var(--coral-pink);
}

.nav-links a.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--coral-pink), var(--stardust-purple));
  border-radius: 2px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-menu {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--coral-pink), var(--stardust-purple));
  text-decoration: none;
  transition: transform 0.2s;
}

.user-avatar:hover {
  transform: scale(1.05);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  color: var(--text-light);
  font-weight: 600;
  font-size: 16px;
}
</style>

