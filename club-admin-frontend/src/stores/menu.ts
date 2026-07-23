import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import { sysMenuApi } from '@/api/sysMenu'
import { buildTree } from '@/utils/format'
import type { SysMenu } from '@/types/generated'

const COMPONENT_ALIASES: Record<string, string> = {
  'dashboard/index': 'dashboard/index',
  'member/index': 'member/index',
  'statistics/index': 'statistics/index',
  'club/list': 'club/list',
  'activity/apply': 'activity/apply',
  'notice/index': 'notice/index',
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

/** 补充数据库菜单未覆盖的页面路由（minLevel：数值越小权限越高，当前用户 effectiveLevel ≤ minLevel 方可访问） */
const STATIC_ROUTES: RouteRecordRaw[] = [
  {
    path: 'dashboard',
    name: 'dashboard',
    component: () => import('@/views/dashboard/index.vue'),
    meta: { title: '工作台', icon: 'Odometer', minLevel: 4 },
  },
  {
    path: 'member',
    name: 'member',
    component: () => import('@/views/member/index.vue'),
    meta: { title: '成员管理', icon: 'User', minLevel: 2 },
  },
  {
    path: 'club',
    name: 'club',
    component: () => import('@/views/club/list.vue'),
    meta: { title: '社团管理', icon: 'OfficeBuilding', minLevel: 3 },
  },
  {
    path: 'club/detail/:clubCode',
    name: 'club-detail',
    component: () => import('@/views/club/detail.vue'),
    meta: { title: '社团详情', icon: 'OfficeBuilding', minLevel: 3 },
  },
  {
    path: 'club/council/:clubId',
    name: 'club-council',
    component: () => import('@/views/club/council.vue'),
    meta: { title: '合议签字', icon: 'EditPen', minLevel: 1 },
  },
  {
    path: 'activity/apply',
    name: 'activity-apply',
    component: () => import('@/views/activity/apply.vue'),
    meta: { title: '活动管理', icon: 'Calendar', minLevel: 4 },
  },
  {
    path: 'activity/approve-flow/:applyId',
    name: 'activity-approve-flow',
    component: () => import('@/views/activity/approve-flow.vue'),
    meta: { title: '审批详情', icon: 'Finished', minLevel: 2 },
  },
  {
    path: 'activity/sign/:activityId?',
    name: 'activity-sign',
    component: () => import('@/views/activity/sign.vue'),
    meta: { title: '签到管理', icon: 'Checked', minLevel: 2 },
  },
  {
    path: 'notice',
    name: 'notice',
    component: () => import('@/views/notice/index.vue'),
    meta: { title: '通知中心', icon: 'Bell', minLevel: 4 },
  },
  {
    path: 'statistics',
    name: 'statistics',
    component: () => import('@/views/statistics/index.vue'),
    meta: { title: '统计看板', icon: 'DataAnalysis', minLevel: 2 },
  },
  {
    path: 'profile',
    name: 'profile',
    component: () => import('@/views/profile/index.vue'),
    meta: { title: '个人中心', icon: 'User', minLevel: 4 },
  },
  {
    path: 'security',
    name: 'security',
    component: () => import('@/views/profile/security.vue'),
    meta: { title: '安全设置', icon: 'Lock', minLevel: 4 },
  },
]

export const useMenuStore = defineStore('menu', () => {
  const menuTree = ref<SysMenu[]>([])
  const permissions = ref<string[]>([])
  const routesLoaded = ref(false)

  async function loadMenus() {
    const res = await sysMenuApi.list()
    const payload = res.data
    if (payload && Array.isArray(payload.tree)) {
      menuTree.value = payload.tree
      permissions.value = payload.permissions || []
    } else if (Array.isArray(payload)) {
      menuTree.value = buildTree(payload as unknown as SysMenu[], 0)
      permissions.value = []
    } else {
      menuTree.value = []
      permissions.value = []
    }
    routesLoaded.value = true
    return menuTree.value
  }

  function getDynamicRoutes(): RouteRecordRaw[] {
    const dynamic = menuToRoutes(menuTree.value)
    const existingPaths = new Set(dynamic.map((r) => r.path))
    const extras = STATIC_ROUTES.filter((r) => !existingPaths.has(r.path as string))
    return [...dynamic, ...extras]
  }

  return { menuTree, permissions, routesLoaded, loadMenus, getDynamicRoutes }
})
