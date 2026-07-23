<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { activityApplyApi } from '@/api/activityApply'

const route = useRoute()
const loading = ref(false)
const detail = ref<Record<string, unknown> | null>(null)

async function load() {
  const id = Number(route.params.applyId)
  if (!id) return
  loading.value = true
  try {
    const res = await activityApplyApi.detail(id)
    detail.value = (res.data as Record<string, unknown>) || null
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page" v-loading="loading">
    <h2>审批详情</h2>
    <p class="hint">写操作（通过/驳回）仅在此二级页完成，工作台禁止硬编码处置按钮。</p>
    <el-empty v-if="!detail && !loading" description="未找到申请详情" />
    <pre v-else-if="detail" class="json">{{ JSON.stringify(detail, null, 2) }}</pre>
  </div>
</template>

<style scoped>
.page {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
}
h2 {
  margin: 0 0 8px;
  color: var(--brand-deep);
}
.hint {
  color: #909399;
  font-size: 13px;
}
.json {
  background: var(--brand-bg);
  padding: 16px;
  border-radius: 8px;
  overflow: auto;
  font-size: 12px;
}
</style>
