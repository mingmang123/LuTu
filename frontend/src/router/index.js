import {createRouter, createWebHistory} from 'vue-router'
import LoginView from '../views/LoginView.vue'
import HomeView from '../views/HomeView.vue'
import UserPlatform from '../views/UserPlatform.vue'
import AiChatView from '../views/AiChatView.vue'
import TravelPlansView from '../views/TravelPlansView.vue'
import TravelPlanDetailView from '../views/TravelPlanDetailView.vue'
import UserProfileView from '../views/UserProfileView.vue'
import UserPortraitGuideView from '../views/UserPortraitGuideView.vue'
import ShareView from '../views/ShareView.vue'
import ShareDetailView from '../views/ShareDetailView.vue'
import ShareListView from '../views/ShareListView.vue'
import MyFavoritesView from '../views/MyFavoritesView.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomeView,
    meta: { requiresAuth: false }
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { requiresAuth: false, guestOnly: true }
  },
  // 用户平台（需要登录）
  {
    path: '/platform',
    component: UserPlatform,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'PlatformOverview',
        component: { template: '<div></div>' } // 默认显示概览，由父组件控制
      },
      {
        path: 'plans',
        name: 'PlatformPlans',
        component: TravelPlansView
      },
      {
        path: 'ai-chat',
        name: 'PlatformAiChat',
        component: AiChatView
      },
      {
        path: 'profile',
        name: 'PlatformProfile',
        component: UserProfileView
      },
      {
        path: 'favorites',
        name: 'PlatformFavorites',
        component: MyFavoritesView
      }
    ]
  },
  // 保留旧路由兼容
  {
    path: '/dashboard',
    redirect: '/platform'
  },
  {
    path: '/ai-chat',
    redirect: '/platform/ai-chat'
  },
  {
    path: '/travel-plans',
    redirect: '/platform/plans'
  },
  {
    path: '/profile',
    redirect: '/platform/profile'
  },
  {
    path: '/travel-plan/:id',
    name: 'TravelPlanDetail',
    component: TravelPlanDetailView,
    meta: { requiresAuth: true },
    props: true
  },
  {
    path: '/user-profile',
    redirect: '/platform/profile'
  },
  {
    path: '/portrait-guide',
    name: 'UserPortraitGuide',
    component: UserPortraitGuideView,
    meta: { requiresAuth: true }
  },
  {
    path: '/share/:code',
    name: 'Share',
    component: ShareView,
    meta: { requiresAuth: false }
  },
  {
    path: '/share-detail/:code',
    name: 'ShareDetail',
    component: ShareDetailView,
    meta: { requiresAuth: false }
  },
  {
    path: '/shares',
    name: 'ShareList',
    component: ShareListView,
    meta: { requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  // 首页任何人可访问
  if (to.path === '/') {
    next()
    return
  }

  // 需要登录的页面
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  // 仅限游客访问的页面（登录后不能访问）
  if (to.meta.guestOnly && token) {
    next('/platform')
    return
  }

  next()
})

export default router
