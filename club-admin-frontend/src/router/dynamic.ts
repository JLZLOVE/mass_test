import type { Router, RouteRecordRaw } from 'vue-router'
import { useMenuStore } from '@/stores/menu'

let setupPromise: Promise<void> | null = null

async function registerDynamicRoutes(router: Router): Promise<void> {
  const menuStore = useMenuStore()
  await menuStore.loadMenus()

  const dynamicRoutes = menuStore.getDynamicRoutes()
  for (const route of dynamicRoutes) {
    if (route.name && router.hasRoute(route.name as string)) continue
    router.addRoute('layout', route)
  }

  // 404 兜底：必须在动态路由注册后再添加，避免 /dashboard 在注册前被 catch-all 无限重定向
  if (!router.hasRoute('not-found')) {
    router.addRoute('layout', {
      path: ':pathMatch(.*)*',
      name: 'not-found',
      redirect: '/dashboard',
    } satisfies RouteRecordRaw)
  }
}

/** 注册动态路由（菜单驱动 + 静态补充页），多次调用只会执行一次 */
export function setupDynamicRoutes(router: Router): Promise<void> {
  if (!setupPromise) {
    setupPromise = registerDynamicRoutes(router).catch((err) => {
      setupPromise = null
      throw err
    })
  }
  return setupPromise
}
