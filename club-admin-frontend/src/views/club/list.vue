<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { portalApi } from '@/api/portal'
import type { PortalClub } from '@/types/generated'

const route = useRoute()
const loading = ref(false)
const clubs = ref<PortalClub[]>([])

async function load() {
  loading.value = true
  try {
    const res = await portalApi.clubs({})
    const data = res.data
    clubs.value = Array.isArray(data) ? data : data?.records || []
  } catch {
    clubs.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <h2>社团管理</h2>
    <p class="hint">筛选：{{ JSON.stringify(route.query) }}</p>
    <el-table v-loading="loading" :data="clubs" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="clubName" label="社团名称" min-width="160" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="status" label="状态" width="90" />
    </el-table>
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
  font-size: 12px;
}
</style>
