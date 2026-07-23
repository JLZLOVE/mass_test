<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import { noticeInfoApi } from '@/api/noticeInfo'
import { useUserStore } from '@/stores/user'
import type { NoticeInfo } from '@/types/generated'

const router = useRouter()
const userStore = useUserStore()
const visible = ref(false)
const loading = ref(false)
const recent = ref<NoticeInfo[]>([])

async function loadRecent() {
  loading.value = true
  try {
    const res = await noticeInfoApi.inbox({ page: 1, limit: 5 })
    recent.value = res.data?.records || []
    userStore.setUnreadCount(res.data?.total ?? 0)
  } catch {
    recent.value = []
  } finally {
    loading.value = false
  }
}

function onShow() {
  loadRecent()
}

function goAll() {
  visible.value = false
  router.push('/notice')
}

function goDetail(item: NoticeInfo) {
  visible.value = false
  if (item.id) router.push({ path: '/notice', query: { id: String(item.id) } })
}

onMounted(() => {
  loadRecent()
})

defineExpose({ refresh: loadRecent })
</script>

<template>
  <el-popover
    v-model:visible="visible"
    placement="bottom-end"
    :width="320"
    trigger="click"
    @show="onShow"
  >
    <template #reference>
      <button type="button" class="bell-btn" aria-label="通知">
        <el-badge :value="userStore.unreadCount" :hidden="!userStore.unreadCount" :max="99">
          <el-icon :size="20"><Bell /></el-icon>
        </el-badge>
      </button>
    </template>

    <div class="notice-panel">
      <div class="notice-panel__title">未读消息</div>
      <el-skeleton v-if="loading" :rows="3" animated />
      <template v-else>
        <div v-if="!recent.length" class="notice-panel__empty">暂无未读消息</div>
        <button
          v-for="item in recent"
          :key="item.id"
          type="button"
          class="notice-row"
          @click="goDetail(item)"
        >
          <div class="notice-row__title">{{ item.title }}</div>
          <div class="notice-row__time">{{ item.publishTime || item.createTime }}</div>
        </button>
      </template>
      <button type="button" class="notice-panel__all" @click="goAll">查看全部消息</button>
    </div>
  </el-popover>
</template>

<style scoped lang="scss">
.bell-btn {
  border: none;
  background: transparent;
  color: #fff;
  cursor: pointer;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;

  &:hover {
    background: rgba(255, 255, 255, 0.08);
  }
}

.notice-panel__title {
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--brand-deep);
}

.notice-panel__empty {
  color: #909399;
  font-size: 13px;
  padding: 16px 0;
  text-align: center;
}

.notice-row {
  width: 100%;
  border: none;
  background: transparent;
  text-align: left;
  padding: 10px 4px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;

  &:hover {
    background: var(--brand-hover);
  }
}

.notice-row__title {
  font-size: 13px;
  color: #303133;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notice-row__time {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.notice-panel__all {
  width: 100%;
  margin-top: 8px;
  border: none;
  background: transparent;
  color: var(--brand-interactive);
  cursor: pointer;
  padding: 8px;
  font-size: 13px;

  &:hover {
    text-decoration: underline;
  }
}
</style>
