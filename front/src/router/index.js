/**
 * 路由配置
 * 定义所有页面路由
 */

import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/Home.vue'),
      meta: {
        title: '首页',
        requiresAuth: false
      }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Auth.vue'),
      meta: {
        title: '登录',
        requiresAuth: false
      }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Auth.vue'),
      meta: {
        title: '注册',
        requiresAuth: false
      }
    },
    {
      path: '/feed',
      name: 'feed',
      component: () => import('@/views/Feed.vue'),
      meta: {
        title: '社区动态',
        requiresAuth: false
      }
    },
    {
      path: '/articles',
      name: 'articles',
      component: () => import('@/views/article/ArticleList.vue'),
      meta: {
        title: '文章列表',
        requiresAuth: false
      }
    },
    {
      path: '/articles/:id',
      name: 'article-detail',
      component: () => import('@/views/article/ArticleDetail.vue'),
      meta: {
        title: '文章详情',
        requiresAuth: false
      }
    },
    {
      path: '/articles/create',
      name: 'article-create',
      component: () => import('@/views/article/ArticleEditor.vue'),
      meta: {
        title: '发布文章',
        requiresAuth: true
      }
    },
    {
      path: '/articles/edit/:id',
      name: 'article-edit',
      component: () => import('@/views/article/ArticleEditor.vue'),
      meta: {
        title: '编辑文章',
        requiresAuth: true
      }
    },
    {
      path: '/questions',
      name: 'questions',
      component: () => import('@/views/question/QuestionList.vue'),
      meta: {
        title: '问答',
        requiresAuth: false
      }
    },
    {
      path: '/questions/:id',
      name: 'question-detail',
      component: () => import('@/views/question/QuestionDetail.vue'),
      meta: {
        title: '问题详情',
        requiresAuth: false
      }
    },
    {
      path: '/questions/create',
      name: 'question-create',
      component: () => import('@/views/question/QuestionCreate.vue'),
      meta: {
        title: '提问',
        requiresAuth: true
      }
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('@/views/Search.vue'),
      meta: {
        title: '搜索',
        requiresAuth: false
      }
    },
    {
      path: '/users/:id',
      name: 'user-profile',
      component: () => import('@/views/user/UserProfile.vue'),
      meta: {
        title: '个人主页',
        requiresAuth: false
      }
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/dashboard/Dashboard.vue'),
      meta: {
        title: '内容管理后台',
        requiresAuth: true
      }
    },
    {
      path: '/interaction',
      name: 'interaction',
      component: () => import('@/views/Interaction.vue'),
      meta: {
        title: '互动中心',
        requiresAuth: true
      }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFound.vue'),
      meta: {
        title: '页面不存在'
      }
    }
  ]
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - Corasta`
  }

  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn) {
      // 未登录，跳转到登录页
      next({
        name: 'login',
        query: { redirect: to.fullPath }
      })
      return
    }
  }

  // 如果已登录且访问登录/注册页，跳转到首页
  if (userStore.isLoggedIn && (to.name === 'login' || to.name === 'register')) {
    next({ name: 'home' })
    return
  }

  next()
})

export default router
