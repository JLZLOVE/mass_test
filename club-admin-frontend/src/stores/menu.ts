import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import { sysMenuApi } from '@/api/sysMenu'
import { buildTree } from '@/utils/format'
import type { SysMenu } from '@/types/generated'

/** componentPath 到 views 路径的映射 */
const COMPONENT_ALIASES: Record<string, string> = {
  'member/index': 'member/index',
  'statistics/index': 'statistics/index',
}

const viewModules = import.meta.glob('@/views/**/*.vue')

function resolveViewComponent(componentPath?: string) {
  if (!componentPath) return undefined
  const normalized = COMPONENT_ALIASES[componentPath] || componentPath
  const candidates = [
    `/src/views/${normalized}.vue`,
    `/src/views/${normalized}/index.vue`,
  ]
  for (const key of Object.keys(viewModules)) {
    if (candidates.some((c) => key.endsWith(c.replace('/src', '')))) {
      return viewModules[key]
    }
  }
  const directKey = `@/views/${normalized}.vue`
  const indexKey = `@/views/${normalized}/index.vue`
  return viewModules[directKey] || viewModules[indexKey]
}

function joinRoutePath(parentPath: string, routePath?: string): string {
  if (!routePath) return parentPath
  if (routePath.startsWith('/')) return routePath
  const base = parentPath.endsWith('/') ? parentPath.slice(0, -1) : parentPath
  return `${base}/${routePath}`.replace(/\/+/g, '/')
}

function menuToRoutes(menus: SysMenu[], parentFullPath = ''): RouteRecordRaw[] {
  const routes: RouteRecordRaw[] = []
  for (const menu of menus) {
    if (menu.menuType === 3 || menu.status === 0) continue

    const fullPath = joinRoutePath(parentFullPath || '', menu.routePath || '')

    if (menu.menuType === 1) {
      const children = menu.children?.length ? menuToRoutes(menu.children, fullPath) : []
      if (children.length) routes.push(...children)
      continue
    }

    if (menu.menuType === 2 && menu.componentPath) {
      const component = resolveViewComponent(menu.componentPath)
      if (component) {
        routes.push({
          path: fullPath.startsWith('/') ? fullPath.slice(1) : fullPath,
          name: `menu-${menu.id}`,
          component,
          meta: {
            title: menu.menuName,
            icon: menu.icon,
            permissionCode: menu.permissionCode,
          },
        })
      }
    }

    if (menu.children?.length) {
      routes.push(...menuToRoutes(menu.children, fullPath))
    }
  }
  return routes
}

/** 补充数据库菜单未覆盖的页面路由（仅包含实际存在的页面） */
const STATIC_ROUTES: RouteRecordRaw[] = [
  {
    path: 'member',
    name: 'member',
    component: () => import('@/views/member/index.vue'),
    meta: { title: '成员管理', icon: 'User' },
  },
  {
    path: 'statistics',
    name: 'statistics',
    component: () => import('@/views/statistics/index.vue'),
    meta: { title: '统计看板', icon: 'DataAnalysis' },
  },
]

export const useMenuStore = defineStore('menu', () => {
  const menuTree = ref<SysMenu[]>([])
  const routesLoaded = ref(false)

  async function loadMenus() {
    const res = await sysMenuApi.list()
    const list = (res.data || []).filter((m) => m.status !== 0 && m.menuType !== 3)
    menuTree.value = buildTree(list, 0)
    routesLoaded.value = true
    return menuTree.value
  }

  function getDynamicRoutes(): RouteRecordRaw[] {
    const dynamic = menuToRoutes(menuTree.value)
    const existingPaths = new Set(dynamic.map((r) => r.path))
    const extras = STATIC_ROUTES.filter((r) => !existingPaths.has(r.path as string))
    return [...dynamic, ...extras]
  }

  return { menuTree, routesLoaded, loadMenus, getDynamicRoutes }
})