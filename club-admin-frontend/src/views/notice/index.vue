<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { noticeInfoApi } from '@/api/noticeInfo'
import type { NoticeInfo } from '@/types/generated'

const route = useRoute()
const loading = ref(false)
const rows = ref<NoticeInfo[]>([])
const total = ref(0)

async function load() {
  loading.value = true
  try {
    const res = await noticeInfoApi.inbox({ page: 1, limit: 20 })
    rows.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(() => route.query.id, load)
</script>

<template>
  <div class="page">
    <div class="page-head">
      <h2>通知中心</h2>
      <p class="hint">收件箱 · 共 {{ total }} 条 · query={{ JSON.stringify(route.query) }}</p>
    </div>
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="publisherName" label="发布人" width="120" />
      <el-table-column prop="publishTime" label="发布时间" width="180" />
    </el-table>
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
</style>
