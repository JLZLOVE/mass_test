<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Calendar, Bell, Checked } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const open = ref(false)

const items = computed(() => {
  const list: { label: string; path: string; query?: Record<string, string>; icon: unknown }[] = []
  if (userStore.effectiveLevel <= 2) {
    list.push({ label: '发起活动', path: '/activity/apply', query: { action: 'create' }, icon: Calendar })
  }
  if (userStore.canPublishNotice) {
    list.push({ label: '发布通知', path: '/notice', query: { action: 'create' }, icon: Bell })
  }
  if (userStore.effectiveLevel <= 2) {
    list.push({ label: '签到管理', path: '/activity/sign', query: { range: 'today' }, icon: Checked })
  }
  return list
})

function toggle() {
  open.value = !open.value
}

function go(item: (typeof items.value)[number]) {
  open.value = false
  router.push({ path: item.path, query: item.query })
}
</script>

<template>
  <div v-if="userStore.canUseFab" class="fab-wrap">
    <transition name="fab-fade">
      <div v-if="open" class="fab-menu">
        <button
          v-for="item in items"
          :key="item.label"
          type="button"
          class="fab-menu__item"
          @click="go(item)"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          {{ item.label }}
        </button>
      </div>
    </transition>
    <button type="button" class="fab-main" :class="{ 'is-open': open }" @click="toggle">
      <el-icon :size="24"><Plus /></el-icon>
    </button>
  </div>
</template>

<style scoped lang="scss">
.fab-wrap {
  position: fixed;
  right: 28px;
  bottom: 28px;
  z-index: 100;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.fab-main {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  color: #1e3a5f;
  background: linear-gradient(145deg, #e8d48b 0%, #d4af37 55%, #c49a2c 100%);
  box-shadow: 0 8px 20px rgba(212, 175, 55, 0.35);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s ease;

  &:hover {
    transform: scale(1.05);
  }

  &.is-open {
    transform: rotate(45deg) scale(1.05);
  }
}

.fab-menu {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.fab-menu__item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: none;
  cursor: pointer;
  padding: 10px 14px;
  border-radius: 10px;
  background: var(--brand-glass);
  backdrop-filter: blur(12px);
  color: var(--brand-deep);
  font-size: 13px;
  box-shadow: 0 4px 14px rgba(30, 58, 95, 0.12);

  &:hover {
    background: #fff;
    color: var(--brand-interactive);
  }
}

.fab-fade-enter-active,
.fab-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.fab-fade-enter-from,
.fab-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
