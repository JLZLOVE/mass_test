<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { ArrowDown, Bell, Lock, SwitchButton, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const menuVisible = ref(false)

const avatarText = computed(() => {
  const name = userStore.displayName
  return name ? name.slice(0, 1) : 'U'
})

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确认退出当前账号？', '退出登录', {
      confirmButtonText: '确认退出',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  await userStore.logout()
  router.push('/login')
}

function go(path: string) {
  menuVisible.value = false
  router.push(path)
}
</script>

<template>
  <el-popover
    v-model:visible="menuVisible"
    placement="bottom-end"
    :width="280"
    trigger="click"
    popper-class="user-menu-popper"
    :show-arrow="false"
  >
    <template #reference>
      <button type="button" class="user-trigger" aria-label="用户菜单">
        <span
          class="avatar"
          :class="{ 'avatar--gold': userStore.hasGoldAvatarRing }"
        >
          <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" alt="" />
          <span v-else>{{ avatarText }}</span>
        </span>
        <span class="user-name">{{ userStore.displayName }}</span>
        <el-icon class="caret"><ArrowDown /></el-icon>
      </button>
    </template>

    <div class="user-menu">
      <div class="user-menu__header">
        <span
          class="avatar avatar--lg"
          :class="{ 'avatar--gold': userStore.hasGoldAvatarRing }"
        >
          <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" alt="" />
          <span v-else>{{ avatarText }}</span>
        </span>
        <div class="user-menu__meta">
          <div class="user-menu__name">{{ userStore.displayName }}</div>
          <div class="user-menu__role">{{ userStore.roleName }}</div>
        </div>
        <span class="user-menu__level">Level: {{ userStore.effectiveLevel }}</span>
      </div>

      <div class="user-menu__body">
        <button type="button" class="menu-item" @click="go('/profile')">
          <el-icon><User /></el-icon>
          个人中心
        </button>
        <button type="button" class="menu-item" @click="go('/security')">
          <el-icon><Lock /></el-icon>
          安全设置
        </button>
        <button type="button" class="menu-item" @click="go('/notice')">
          <el-icon><Bell /></el-icon>
          消息中心
          <span v-if="userStore.unreadCount > 0" class="dot" />
        </button>
      </div>

      <div class="user-menu__footer">
        <button type="button" class="menu-item menu-item--danger" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </button>
      </div>
    </div>
  </el-popover>
</template>

<style scoped lang="scss">
.user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: none;
  background: transparent;
  cursor: pointer;
  color: #fff;
  padding: 4px 8px;
  border-radius: 8px;

  &:hover {
    background: rgba(255, 255, 255, 0.08);
  }
}

.user-name {
  max-width: 96px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.caret {
  font-size: 12px;
  opacity: 0.8;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--brand-interactive);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  border: 2px solid #fff;
  overflow: hidden;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &--lg {
    width: 40px;
    height: 40px;
  }

  &--gold {
    box-shadow: 0 0 0 2px var(--brand-gold);
  }
}

.user-menu {
  margin: -12px;
}

.user-menu__header {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  height: 72px;
  padding: 0 16px;
  background: var(--brand-deep);
  color: #fff;
  border-radius: 12px 12px 0 0;
}

.user-menu__name {
  font-weight: 700;
  font-size: 15px;
  line-height: 1.3;
}

.user-menu__role {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 2px;
}

.user-menu__level {
  position: absolute;
  right: 10px;
  bottom: 6px;
  font-size: 10px;
  color: rgba(255, 255, 255, 0.55);
}

.user-menu__body {
  padding: 8px 0;
}

.user-menu__footer {
  border-top: 1px solid #eee;
  padding: 8px 0;
}

.menu-item {
  position: relative;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  border: none;
  background: transparent;
  padding: 10px 16px;
  font-size: 14px;
  color: #303133;
  cursor: pointer;
  text-align: left;

  &:hover {
    background: var(--brand-hover);
  }

  &--danger {
    color: var(--brand-danger);
  }
}

.dot {
  margin-left: auto;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--brand-danger);
}
</style>

<style>
.user-menu-popper {
  padding: 0 !important;
  border-radius: 12px !important;
  border: 2px solid #e8e8e8 !important;
  box-shadow: 0 12px 32px rgba(30, 58, 95, 0.18) !important;
  overflow: hidden;
}
</style>
