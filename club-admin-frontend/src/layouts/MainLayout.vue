<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu'
import SidebarMenu from '@/components/SidebarMenu.vue'
import { Fold, Expand, SwitchButton, User } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const menuStore = useMenuStore()

const isCollapse = ref(false)

const pageTitle = computed(() => (route.meta.title as string) || '社团管理平台')

const extraMenus = [
  { id: 9002, parentId: 0, menuName: '成员管理', menuType: 2, routePath: '/member', icon: 'User', componentPath: 'member/index', sort: 3, status: 1 },
  { id: 9004, parentId: 0, menuName: '统计看板', menuType: 2, routePath: '/statistics', icon: 'DataAnalysis', componentPath: 'statistics/index', sort: 5, status: 1 },
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
    if (!existingNames.has(item.menuName)) {
      tree.push(item as never)
    }
  }
  return tree.sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo">
        <span v-if="!isCollapse">社团管理平台</span>
        <span v-else>社</span>
      </div>
      <SidebarMenu :menus="displayMenus" :collapse="isCollapse" />
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ userStore.userInfo?.realName || userStore.username }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
}

.layout-aside {
  background: #001529;
  transition: width 0.2s;
  overflow: hidden;
}

.logo {
  height: 56px;
  line-height: 56px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #eee;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.collapse-btn {
  cursor: pointer;
  font-size: 20px;
}

.page-title {
  font-size: 16px;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.layout-main {
  background: #f5f7fa;
  padding: 16px;
}
</style>