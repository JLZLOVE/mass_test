<script setup lang="ts">
import { computed, provide, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu'
import SidebarMenu from '@/components/SidebarMenu.vue'
import UserDropdown from '@/components/UserDropdown.vue'
import NotificationBell from '@/components/NotificationBell.vue'
import WorkbenchFab from '@/components/WorkbenchFab.vue'
import { Expand, Fold, Search } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const menuStore = useMenuStore()

const isCollapse = ref(false)
const searchKeyword = ref('')
const dashboardRefreshKey = ref(0)

provide('dashboardRefreshKey', dashboardRefreshKey)

const pageTitle = computed(() => (route.meta.title as string) || '社团综合管理平台')

/** 侧边栏菜单定义：minLevel = 可访问的最高有效等级（数值越小权限越高） */
const extraMenus = [
  {
    id: 9001, parentId: 0, menuName: '工作台', menuType: 2,
    routePath: '/dashboard', icon: 'Odometer', componentPath: 'dashboard/index',
    sort: 0, status: 1, minLevel: 4,
  },
  {
    id: 9005, parentId: 0, menuName: '活动申请', menuType: 2,
    routePath: '/activity/apply', icon: 'Calendar', componentPath: 'activity/apply',
    sort: 1, status: 1, minLevel: 2,
  },
  {
    id: 9003, parentId: 0, menuName: '社团管理', menuType: 2,
    routePath: '/club', icon: 'OfficeBuilding', componentPath: 'club/list',
    sort: 2, status: 1, minLevel: 2,
  },
  {
    id: 9002, parentId: 0, menuName: '成员管理', menuType: 2,
    routePath: '/member', icon: 'User', componentPath: 'member/index',
    sort: 3, status: 1, minLevel: 2,
  },
  {
    id: 9006, parentId: 0, menuName: '通知中心', menuType: 2,
    routePath: '/notice', icon: 'Bell', componentPath: 'notice/index',
    sort: 4, status: 1, minLevel: 4,
  },
  {
    id: 9004, parentId: 0, menuName: '统计看板', menuType: 2,
    routePath: '/statistics', icon: 'DataAnalysis', componentPath: 'statistics/index',
    sort: 5, status: 1, minLevel: 2,
  },
]

const displayMenus = computed(() => {
  const tree = [...menuStore.menuTree]
  const existingNames = new Set<string>()
  const collect = (items: typeof tree) => {
    items.forEach((m) => {
      existingNames.add(m.menuName || '')
      if (m.children) collect(m.children)
    })
  }
  collect(tree)
  for (const item of extraMenus) {
    if (!existingNames.has(item.menuName) && userStore.effectiveLevel <= (item as Record<string, unknown>).minLevel as number) {
      tree.push(item as never)
    }
  }
  return tree.sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
})

function goDashboard(forceRefresh = false) {
  if (forceRefresh) dashboardRefreshKey.value += 1
  router.push('/dashboard')
}
</script>

<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <button type="button" class="logo" @click="goDashboard(true)">
        <span v-if="!isCollapse">社团综合管理平台</span>
        <span v-else>社</span>
      </button>
      <SidebarMenu :menus="displayMenus" :collapse="isCollapse" />
    </el-aside>

    <el-container>
      <!-- I区：全局导航栏 -->
      <el-header class="layout-header" height="64px">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>
              <a href="javascript:;" @click="goDashboard(true)">工作台</a>
            </el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.path !== '/dashboard'">
              {{ pageTitle }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-center">
          <el-input
            v-model="searchKeyword"
            class="global-search"
            placeholder="全局模糊搜索（即将开放）"
            :prefix-icon="Search"
            disabled
          />
        </div>

        <div class="header-right">
          <NotificationBell />
          <UserDropdown />
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <keep-alive include="DashboardWorkbench">
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>

    <WorkbenchFab />
  </el-container>
</template>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  background: var(--brand-bg);
}

.layout-aside {
  background: var(--brand-deep);
  transition: width 0.2s;
  overflow: hidden;
}

.logo {
  width: 100%;
  height: 64px;
  line-height: 64px;
  text-align: center;
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  border: none;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: transparent;
  cursor: pointer;
  letter-spacing: 0.5px;

  &:hover {
    color: var(--brand-gold);
  }
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  background: var(--brand-deep);
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(30, 58, 95, 0.2);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 200px;

  :deep(.el-breadcrumb__inner),
  :deep(.el-breadcrumb__inner a),
  :deep(.el-breadcrumb__separator) {
    color: rgba(255, 255, 255, 0.85) !important;
  }

  :deep(.el-breadcrumb__inner a:hover) {
    color: var(--brand-gold) !important;
  }
}

.collapse-btn {
  cursor: pointer;
  font-size: 20px;
  color: #fff;
}

.header-center {
  flex: 1;
  max-width: 420px;
}

.global-search {
  :deep(.el-input__wrapper) {
    background: rgba(255, 255, 255, 0.12);
    box-shadow: none;
    border-radius: 20px;
  }

  :deep(.el-input__inner),
  :deep(.el-input__prefix) {
    color: rgba(255, 255, 255, 0.75);
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 160px;
  justify-content: flex-end;
}

.layout-main {
  background: var(--brand-bg);
  padding: 16px 20px 24px;
  overflow: auto;
}
</style>
