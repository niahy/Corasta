<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(() => {
  // 初始化用户信息
  userStore.initUser()
})
</script>

<style>
@import '@/assets/styles/variables.css';
@import '@/assets/styles/global.css';

#app {
  min-height: 100vh;
}
</style>
