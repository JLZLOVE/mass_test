<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { SysMenu } from '@/types/generated'
import * as ElementPlusIcons from '@element-plus/icons-vue'

const props = defineProps<{
  menus: SysMenu[]
  collapse?: boolean
}>()

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)

function getIcon(icon?: string) {
  if (!icon) return ElementPlusIcons.Menu
  const pascal = icon.charAt(0).toUpperCase() + icon.slice(1)
  return (ElementPlusIcons as Record<string, unknown>)[pascal] ||
    (ElementPlusIcons as Record<string, unknown>)[icon] ||
    ElementPlusIcons.Menu
}

function resolvePath(menu: SysMenu, parentPath = ''): string {
  const rp = menu.routePath || ''
  if (rp.startsWith('/')) return rp
  const base = parentPath.endsWith('/') ? parentPath.slice(0, -1) : parentPath
  return `${base}/${rp}`.replace(/\/+/g, '/') || '/'
}

function handleSelect(menu: SysMenu, parentPath = '') {
  const path = resolvePath(menu, parentPath)
  if (path && menu.menuType === 2) {
    router.push(path)
  }
}

function visibleMenus(list: SysMenu[]) {
  return list.filter((m) => m.menuType !== 3 && m.status !== 0)
}
</script>

<template>
  <el-menu
    :default-active="activeMenu"
    :collapse="collapse"
    background-color="#001529"
    text-color="#ffffffa6"
    active-text-color="#fff"
    class="sidebar-menu"
  >
    <template v-for="menu in visibleMenus(menus)" :key="menu.id">
      <el-sub-menu
        v-if="menu.menuType === 1 && menu.children?.length"
        :index="String(menu.id)"
      >
        <template #title>
          <el-icon><component :is="getIcon(menu.icon)" /></el-icon>
          <span>{{ menu.menuName }}</span>
        </template>
        <template v-for="child in visibleMenus(menu.children || [])" :key="child.id">
          <el-sub-menu
            v-if="child.menuType === 1 && child.children?.length"
            :index="String(child.id)"
          >
            <template #title>{{ child.menuName }}</template>
            <el-menu-item
              v-for="sub in visibleMenus(child.children || [])"
              :key="sub.id"
              :index="resolvePath(sub, resolvePath(child, resolvePath(menu)))"
              @click="handleSelect(sub, resolvePath(child, resolvePath(menu)))"
            >
              {{ sub.menuName }}
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item
            v-else-if="child.menuType === 2"
            :index="resolvePath(child, resolvePath(menu))"
            @click="handleSelect(child, resolvePath(menu))"
          >
            <el-icon><component :is="getIcon(child.icon)" /></el-icon>
            <span>{{ child.menuName }}</span>
          </el-menu-item>
        </template>
      </el-sub-menu>
      <el-menu-item
        v-else-if="menu.menuType === 2"
        :index="resolvePath(menu)"
        @click="handleSelect(menu)"
      >
        <el-icon><component :is="getIcon(menu.icon)" /></el-icon>
        <span>{{ menu.menuName }}</span>
      </el-menu-item>
    </template>
  </el-menu>
</template>

<style scoped>
.sidebar-menu {
  border-right: none;
  height: calc(100vh - 56px);
  overflow-y: auto;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}
</style>
