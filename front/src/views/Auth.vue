<template>
  <div class="auth-page">
    <div class="glow glow-left"></div>
    <div class="app-shell page-space-lg">
      <router-link class="back-link" to="/">
        ← 返回 Corasta
      </router-link>

      <section class="dual-column align-center">
        <div class="auth-intro">
          <Badge>Account Center</Badge>
          <h1 class="mt-16">拥抱星尘，立即成为创作者</h1>
          <p class="mt-18 mb-18 max-420">
            通过用户名和密码快速注册，设置密保问题可帮助找回密码。
          </p>
          <ul class="list-soft">
            <li>· 用户名登录，安全便捷</li>
            <li>· 支持密保问题找回密码</li>
            <li>· 注册后可立即使用全部功能</li>
          </ul>
        </div>

        <Card size="large" class="auth-card">
          <div class="tabs mb-24">
            <button
              :class="['tab-btn', { active: activeTab === 'login' }]"
              @click="activeTab = 'login'"
            >
              登录
            </button>
            <button
              :class="['tab-btn', { active: activeTab === 'register' }]"
              @click="activeTab = 'register'"
            >
              注册
            </button>
          </div>

          <!-- 登录面板 -->
          <div v-show="activeTab === 'login'" class="tab-panel">
            <form @submit.prevent="handleLogin">
              <Input
                v-model="loginForm.username"
                label="用户名"
                type="text"
                placeholder="请输入用户名"
                :error="loginErrors.username"
                required
              />
              <Input
                v-model="loginForm.password"
                label="密码"
                type="password"
                placeholder="•••••••"
                :error="loginErrors.password"
                required
                class="mt-16"
              />
              <div v-if="loginCaptchaImage" class="captcha-container mt-16">
                <Input
                  v-model="loginForm.captcha"
                  label="验证码"
                  type="text"
                  placeholder="请输入验证码"
                  :error="loginErrors.captcha"
                  required
                >
                  <template #suffix>
                    <img
                      :src="loginCaptchaImage"
                      alt="验证码"
                      class="captcha-image"
                      @click="refreshLoginCaptcha"
                    />
                  </template>
                </Input>
              </div>
              <div class="field-inline mt-16">
                <label class="label-check">
                  <input
                    v-model="loginForm.rememberMe"
                    type="checkbox"
                  />
                  记住我（7天免登录）
                </label>
                <router-link to="/forgot-password" class="link-accent">
                  忘记密码？
                </router-link>
              </div>
              <Button
                type="submit"
                variant="primary"
                size="large"
                :loading="loginLoading"
                block
                class="mt-24"
              >
                登录
              </Button>
            </form>
          </div>

          <!-- 注册面板 -->
          <div v-show="activeTab === 'register'" class="tab-panel">
            <form @submit.prevent="handleRegister">
              <Input
                v-model="registerForm.username"
                label="用户名"
                type="text"
                placeholder="4-20字符，支持字母、数字、下划线"
                :error="registerErrors.username"
                :maxlength="20"
                required
              />
              <Input
                v-model="registerForm.password"
                label="设置密码"
                type="password"
                placeholder="8-32字符，需包含字母和数字"
                :error="registerErrors.password"
                :hint="passwordHint"
                required
                class="mt-16"
              />
              <Input
                v-model="registerForm.confirmPassword"
                label="确认密码"
                type="password"
                placeholder="再次输入密码"
                :error="registerErrors.confirmPassword"
                required
                class="mt-16"
              />
              <div v-if="registerCaptchaImage" class="captcha-container mt-16">
                <Input
                  v-model="registerForm.captcha"
                  label="验证码"
                  type="text"
                  placeholder="请输入验证码"
                  :error="registerErrors.captcha"
                  required
                >
                  <template #suffix>
                    <img
                      :src="registerCaptchaImage"
                      alt="验证码"
                      class="captcha-image"
                      @click="refreshRegisterCaptcha"
                    />
                  </template>
                </Input>
              </div>

              <!-- 密保问题（可选） -->
              <div class="security-questions mt-24">
                <div class="security-questions-header">
                  <label class="security-questions-label">密保问题（可选，最多3个）</label>
                  <Button
                    v-if="securityQuestions.length < 3"
                    variant="text"
                    size="small"
                    @click="addSecurityQuestion"
                  >
                    + 添加密保问题
                  </Button>
                </div>
                <div
                  v-for="(item, index) in securityQuestions"
                  :key="index"
                  class="security-question-item mt-12"
                >
                  <Input
                    v-model="item.question"
                    :label="`问题 ${index + 1}`"
                    type="text"
                    placeholder="请输入密保问题"
                    class="mt-8"
                  />
                  <Input
                    v-model="item.answer"
                    label="答案"
                    type="text"
                    placeholder="请输入答案"
                    class="mt-8"
                  />
                  <Button
                    variant="text"
                    size="small"
                    class="remove-question-btn"
                    @click="removeSecurityQuestion(index)"
                  >
                    删除
                  </Button>
                </div>
              </div>

              <Button
                type="submit"
                variant="primary"
                size="large"
                :loading="registerLoading"
                block
                class="mt-24"
              >
                创建账户
              </Button>
            </form>
          </div>
        </Card>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { login, register, getCaptcha } from '@/api/auth';
import { Button, Card, Input, Badge } from '@/components';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

// Tab 切换 - 根据路由名称决定默认 Tab
const activeTab = ref(route.name === 'register' ? 'register' : 'login');

// 监听路由变化，切换 Tab
watch(
  () => route.name,
  (newName) => {
    if (newName === 'register') {
      activeTab.value = 'register';
    } else if (newName === 'login') {
      activeTab.value = 'login';
    }
  }
);

// 登录表单
const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  captchaKey: '',
  rememberMe: false,
});
const loginErrors = reactive({});
const loginLoading = ref(false);
const loginCaptchaImage = ref('');
const loginCaptchaKey = ref('');

// 注册表单
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  captcha: '',
  captchaKey: '',
});
const registerErrors = reactive({});
const registerLoading = ref(false);
const registerCaptchaImage = ref('');
const registerCaptchaKey = ref('');

// 密保问题
const securityQuestions = ref([]);

// 密码提示
const passwordHint = computed(() => {
  if (!registerForm.password) return '';
  if (registerForm.password.length < 8) {
    return '密码长度至少 8 位';
  }
  if (registerForm.password.length > 32) {
    return '密码长度不能超过 32 位';
  }
  if (!/(?=.*[a-zA-Z])(?=.*\d)/.test(registerForm.password)) {
    return '密码需包含字母和数字';
  }
  return '';
});

// 获取登录验证码
async function fetchLoginCaptcha() {
  try {
    const response = await getCaptcha();
    if (response.data) {
      loginCaptchaImage.value = response.data.image;
      loginCaptchaKey.value = response.data.key;
      loginForm.captchaKey = response.data.key;
    }
  } catch (error) {
    console.error('获取验证码失败:', error);
  }
}

// 获取注册验证码
async function fetchRegisterCaptcha() {
  try {
    const response = await getCaptcha();
    if (response.data) {
      registerCaptchaImage.value = response.data.image;
      registerCaptchaKey.value = response.data.key;
      registerForm.captchaKey = response.data.key;
    }
  } catch (error) {
    console.error('获取验证码失败:', error);
  }
}

// 刷新登录验证码
function refreshLoginCaptcha() {
  fetchLoginCaptcha();
}

// 刷新注册验证码
function refreshRegisterCaptcha() {
  fetchRegisterCaptcha();
}

// 添加密保问题
function addSecurityQuestion() {
  if (securityQuestions.value.length < 3) {
    securityQuestions.value.push({
      question: '',
      answer: '',
    });
  }
}

// 删除密保问题
function removeSecurityQuestion(index) {
  securityQuestions.value.splice(index, 1);
}

// 登录处理
async function handleLogin() {
  // 清除之前的错误
  Object.keys(loginErrors).forEach((key) => {
    loginErrors[key] = '';
  });

  // 验证
  if (!loginForm.username) {
    loginErrors.username = '请输入用户名';
    return;
  }
  if (!loginForm.password) {
    loginErrors.password = '请输入密码';
    return;
  }
  if (!loginForm.captcha) {
    loginErrors.captcha = '请输入验证码';
    return;
  }

  loginLoading.value = true;
  try {
    const response = await login({
      username: loginForm.username,
      password: loginForm.password,
      captcha: loginForm.captcha,
      captchaKey: loginForm.captchaKey,
      rememberMe: loginForm.rememberMe,
    });

    if (response.data) {
      // 保存 token 和用户信息
      userStore.setToken(response.data.token);
      userStore.setUser(response.data.user);

      alert('登录成功');
      // 跳转到首页或之前访问的页面
      const redirect = router.currentRoute.value.query.redirect || '/';
      router.push(redirect);
    }
  } catch (error) {
    console.error('登录失败:', error);
    const errorMessage =
      error.response?.data?.message || '登录失败，请检查用户名和密码';
    alert(errorMessage);
    // 如果是验证码错误，刷新验证码
    if (errorMessage.includes('验证码')) {
      fetchLoginCaptcha();
    }
  } finally {
    loginLoading.value = false;
  }
}

// 注册处理
async function handleRegister() {
  // 清除之前的错误
  Object.keys(registerErrors).forEach((key) => {
    registerErrors[key] = '';
  });

  // 验证用户名
  if (!registerForm.username) {
    registerErrors.username = '请输入用户名';
    return;
  }
  if (registerForm.username.length < 4 || registerForm.username.length > 20) {
    registerErrors.username = '用户名长度必须在 4-20 字符之间';
    return;
  }
  if (!/^[a-zA-Z0-9_]+$/.test(registerForm.username)) {
    registerErrors.username = '用户名只能包含字母、数字和下划线';
    return;
  }

  // 验证密码
  if (!registerForm.password) {
    registerErrors.password = '请输入密码';
    return;
  }
  if (registerForm.password.length < 8 || registerForm.password.length > 32) {
    registerErrors.password = '密码长度必须在 8-32 字符之间';
    return;
  }
  if (!/(?=.*[a-zA-Z])(?=.*\d)/.test(registerForm.password)) {
    registerErrors.password = '密码需包含字母和数字';
    return;
  }

  // 验证确认密码
  if (registerForm.password !== registerForm.confirmPassword) {
    registerErrors.confirmPassword = '两次输入的密码不一致';
    return;
  }

  // 验证验证码
  if (!registerForm.captcha) {
    registerErrors.captcha = '请输入验证码';
    return;
  }

  registerLoading.value = true;
  try {
    // 处理密保问题（过滤空的问题）
    const validSecurityQuestions = securityQuestions.value.filter(
      (item) => item.question.trim() && item.answer.trim()
    );

    const response = await register({
      username: registerForm.username,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      captcha: registerForm.captcha,
      captchaKey: registerForm.captchaKey,
      securityQuestions:
        validSecurityQuestions.length > 0 ? validSecurityQuestions : undefined,
    });

    if (response.data) {
      alert('注册成功，请登录');
      // 切换到登录面板
      activeTab.value = 'login';
      router.push({ name: 'login' });
      // 预填充用户名
      loginForm.username = registerForm.username;
      // 清空注册表单
      Object.keys(registerForm).forEach((key) => {
        if (key !== 'captchaKey') {
          registerForm[key] = '';
        }
      });
      securityQuestions.value = [];
      // 刷新验证码
      fetchLoginCaptcha();
      fetchRegisterCaptcha();
    }
  } catch (error) {
    console.error('注册失败:', error);
    const message = error.response?.data?.message || '注册失败，请重试';
    alert(message);
    // 如果是验证码错误，刷新验证码
    if (message.includes('验证码')) {
      fetchRegisterCaptcha();
    }
  } finally {
    registerLoading.value = false;
  }
}

// 组件挂载时获取验证码
onMounted(() => {
  fetchLoginCaptcha();
  fetchRegisterCaptcha();
});
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  position: relative;
}

.glow-left {
  left: -120px;
  bottom: -80px;
}

.page-space-lg {
  padding: 60px 0;
}

.back-link {
  display: inline-block;
  margin-bottom: 40px;
  color: var(--midnight-purple);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.back-link:hover {
  color: var(--coral-pink);
}

.dual-column {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.align-center {
  align-items: center;
}

.auth-intro {
  max-width: 500px;
}

.mt-8 {
  margin-top: 8px;
}

.mt-12 {
  margin-top: 12px;
}

.mt-16 {
  margin-top: 16px;
}

.mt-18 {
  margin-top: 18px;
}

.mt-24 {
  margin-top: 24px;
}

.mb-18 {
  margin-bottom: 18px;
}

.mb-24 {
  margin-bottom: 24px;
}

.max-420 {
  max-width: 420px;
}

.list-soft {
  list-style: none;
  padding: 0;
  margin: 0;
  color: var(--text-dark);
  line-height: 1.8;
}

.list-soft li {
  margin-bottom: 8px;
}

.auth-card {
  max-width: 480px;
  margin: 0 auto;
}

.tabs {
  display: flex;
  gap: 8px;
  border-bottom: 2px solid var(--border);
}

.tab-btn {
  flex: 1;
  padding: 12px 0;
  background: transparent;
  border: none;
  font-size: 15px;
  font-weight: 500;
  color: var(--midnight-purple);
  cursor: pointer;
  position: relative;
  transition: color 0.2s;
}

.tab-btn:hover {
  color: var(--coral-pink);
}

.tab-btn.active {
  color: var(--coral-pink);
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--coral-pink), var(--stardust-purple));
}

.tab-panel {
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.field-inline {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.label-check {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-dark);
  cursor: pointer;
}

.label-check input[type='checkbox'] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.link-accent {
  color: var(--coral-pink);
  text-decoration: none;
  font-size: 14px;
  transition: opacity 0.2s;
}

.link-accent:hover {
  opacity: 0.8;
}

.captcha-container {
  margin-top: 16px;
}

.captcha-image {
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  transition: opacity 0.2s;
}

.captcha-image:hover {
  opacity: 0.8;
}

.security-questions {
  border-top: 1px solid var(--border);
  padding-top: 16px;
}

.security-questions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.security-questions-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-dark);
}

.security-question-item {
  position: relative;
  padding: 12px;
  background: rgba(255, 126, 138, 0.05);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border);
}

.remove-question-btn {
  margin-top: 8px;
  color: #ff4757;
}

.remove-question-btn:hover {
  color: #ff3838;
}

@media (max-width: 768px) {
  .dual-column {
    grid-template-columns: 1fr;
    gap: 40px;
  }

  .auth-intro {
    text-align: center;
    max-width: 100%;
  }
}
</style>
