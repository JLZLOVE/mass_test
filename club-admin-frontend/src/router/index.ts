import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { setupDynamicRoutes } from '@/router/dynamic'

const MainLayout = () => import('@/layouts/MainLayout.vue')

/** 动态路由是否已注册 */
let dynamicAdded = false

const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true },
  },
  {
    path: '/',
    name: 'layout',
    component: MainLayout,
    redirect: '/member',
    children: [],
  },
  // 不在此处添加 catch-all：动态路由未注册时 /member 会陷入无限重定向
]

const router = createRouter({
  history: createWebHashHistory(),
  routes: constantRoutes,
})

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()

  // 1. 公开路由（如 login）
  if (to.meta.public === true) {
    if (userStore.isLoggedIn && to.path === '/login') {
      next('/member')
    } else {
      next()
    }
    return
  }

  // 2. 未登录 → 跳转登录页
  if (!userStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 3. 已登录但动态路由尚未注册
  if (!dynamicAdded) {
    try {
      await setupDynamicRoutes(router)
      dynamicAdded = true

      const resolved = router.resolve(to.fullPath)
      if (resolved.matched.length > 0 && resolved.name !== 'not-found') {
        next({ ...to, replace: true })
      } else {
        next({ path: '/member', replace: true })
      }
    } catch (error) {
      console.error('动态路由注册失败:', error)
      dynamicAdded = false
      userStore.logout()
      next('/login')
    }
    return
  }

  // 4. 正常放行
  next()
})

export default router
