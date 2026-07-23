import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { setupDynamicRoutes } from '@/router/dynamic'
import { levelLabel } from '@/utils/level'

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
    redirect: '/dashboard',
    children: [],
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes: constantRoutes,
})

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()

  if (to.meta.public === true) {
    if (userStore.isLoggedIn && to.path === '/login') {
      next('/dashboard')
    } else {
      next()
    }
    return
  }

  if (!userStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  if (!dynamicAdded) {
    try {
      if (!userStore.userInfo) {
        await userStore.loadUserProfile().catch(() => undefined)
      }
      await setupDynamicRoutes(router)
      dynamicAdded = true

      const resolved = router.resolve(to.fullPath)
      if (resolved.matched.length > 0 && resolved.name !== 'not-found') {
        next({ ...to, replace: true })
      } else {
        next({ path: '/dashboard', replace: true })
      }
    } catch (error) {
      console.error('动态路由注册失败:', error)
      dynamicAdded = false
      userStore.logout()
      next('/login')
    }
    return
  }

  // 4. 检查路由权限等级：当前 effectiveLevel ≤ meta.minLevel 方可访问
  const minLevel = to.meta.minLevel as number | undefined
  if (minLevel !== undefined && userStore.effectiveLevel > minLevel) {
    ElMessage.warning(
      `权限不足：当前身份为「${levelLabel(userStore.effectiveLevel)}」，无法访问「${to.meta.title || to.path}」`,
    )
    next({ path: '/dashboard', replace: true })
    return
  }

  // 5. 正常放行
  next()
})

export default router
