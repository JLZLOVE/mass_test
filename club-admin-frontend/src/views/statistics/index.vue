<script setup lang="ts">
import { onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { portalApi } from '@/api/portal'
import type { PortalClub } from '@/types/generated'

const loading = ref(false)
const pieRef = ref<HTMLDivElement>()
let pieChart: echarts.ECharts | null = null

async function load() {
  loading.value = true
  try {
    const res = await portalApi.clubs({})
    const data = res.data
    const clubs: PortalClub[] = Array.isArray(data) ? data : data?.records || []

    const categoryMap: Record<string, number> = {}
    for (const c of clubs) {
      const key = c.category || '未分类'
      categoryMap[key] = (categoryMap[key] || 0) + 1
    }

    await new Promise((r) => requestAnimationFrame(r))
    if (!pieRef.value) return
    pieChart ||= echarts.init(pieRef.value)
    pieChart.setOption({
      title: { text: '社团分类占比', left: 'center', textStyle: { color: '#1E3A5F', fontSize: 14 } },
      tooltip: { trigger: 'item' },
      series: [
        {
          type: 'pie',
          radius: ['35%', '60%'],
          data: Object.entries(categoryMap).map(([name, value]) => ({ name, value })),
          color: ['#1E3A5F', '#4A90D9', '#D4AF37', '#7BA3C9', '#A8C5E2'],
        },
      ],
    })
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page" v-loading="loading">
    <h2>统计看板</h2>
    <p class="hint">
      月度活动柱状图 / 成员趋势依赖 club-statistics 接口；当前后端未开放时，先展示门户社团分类占比。
    </p>
    <div ref="pieRef" class="chart" />
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
  margin-bottom: 16px;
}
.chart {
  height: 420px;
}
</style>
