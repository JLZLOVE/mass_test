<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { activityApplyApi } from '@/api/activityApply'
import type { ActivityApply } from '@/types/generated'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const rows = ref<ActivityApply[]>([])
const total = ref(0)

const APPROVE_STATUS: Record<number, string> = {
  1: '草稿',
  2: '待审批',
  3: '审批中',
  4: '已通过',
  5: '已驳回',
  6: '已取消',
  7: '变更中',
}

async function load() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: 1, limit: 20 }
    if (route.query.status === 'pending') params.approveStatus = 2
    if (route.query.status === 'draft') params.approveStatus = 1
    if (route.query.status === 'processing') params.approveStatus = 3
    const res = await activityApplyApi.list(params)
    rows.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

function openDetail(row: ActivityApply) {
  if (row.id) router.push(`/activity/approve-flow/${row.id}`)
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="page-head">
      <h2>活动申请</h2>
      <p class="hint">
        筛选条件来自工作台跳转：
        <code>{{ JSON.stringify(route.query) }}</code>
      </p>
    </div>
    <el-table v-loading="loading" :data="rows" stripe @row-click="openDetail">
      <el-table-column prop="activityName" label="活动名称" min-width="160" />
      <el-table-column prop="applyUsername" label="发起人" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          {{ APPROVE_STATUS[row.approveStatus] || row.approveStatus }}
        </template>
      </el-table-column>
      <el-table-column prop="applyTime" label="申请时间" width="180" />
    </el-table>
    <div class="footer">共 {{ total }} 条</div>
  </div>
</template>

<style scoped>
.page {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
}
.page-head h2 {
  margin: 0;
  color: var(--brand-deep);
}
.hint {
  color: #909399;
  font-size: 12px;
}
.footer {
  margin-top: 12px;
  color: #909399;
  font-size: 13px;
}
</style>
