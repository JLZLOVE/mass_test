<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { clubStatisticsApi } from '@/api/clubStatistics'
import { sysClubApi } from '@/api/sysClub'
import type { ClubStatistics, SysClub } from '@/types/generated'

const loading = ref(false)
const clubs = ref<SysClub[]>([])
const clubMap = ref<Record<number, string>>({})

const filters = reactive({
  clubId: undefined as number | undefined,
  dateRange: [] as string[],
})

const barRef = ref<HTMLDivElement>()
const lineRef = ref<HTMLDivElement>()
const pieRef = ref<HTMLDivElement>()

let barChart: echarts.ECharts | null = null
let lineChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

function getLast12Months(): string[] {
  const months: string[] = []
  const now = new Date()
  for (let i = 11; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    months.push(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`)
  }
  return months
}

function monthKey(dateStr?: string): string {
  if (!dateStr) return ''
  return dateStr.slice(0, 7)
}

function inDateRange(dateStr?: string): boolean {
  if (!filters.dateRange?.length) return true
  const d = dateStr?.slice(0, 10) || ''
  return d >= filters.dateRange[0]! && d <= filters.dateRange[1]!
}

async function loadClubs() {
  const res = await sysClubApi.list()
  clubs.value = (res.data || []).filter((c) => c.status === 1)
  clubMap.value = Object.fromEntries(clubs.value.map((c) => [c.id!, c.clubName || '']))
}

async function loadStatistics() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: 1, limit: 2000 }
    if (filters.clubId) params.clubId = filters.clubId

    const [statRes] = await Promise.all([clubStatisticsApi.listF(params)])
    let records: ClubStatistics[] = statRes.data?.records || []
    records = records.filter((r) => inDateRange(r.statDate))

    renderCharts(records)
    renderPieFromClubs()
  } finally {
    loading.value = false
  }
}

function renderCharts(records: ClubStatistics[]) {
  const months = getLast12Months()
  const activityByMonth: Record<string, number> = Object.fromEntries(months.map((m) => [m, 0]))
  const membersByMonth: Record<string, number> = Object.fromEntries(months.map((m) => [m, 0]))

  for (const row of records) {
    const m = monthKey(row.statDate)
    if (!m || !(m in activityByMonth)) continue
    activityByMonth[m] = (activityByMonth[m] ?? 0) + (row.activityCount || 0)
    if (row.totalMembers != null) {
      membersByMonth[m] = Math.max(membersByMonth[m] || 0, row.totalMembers)
    }
  }

  let lastMember = 0
  for (const m of months) {
    if (membersByMonth[m]) {
      lastMember = membersByMonth[m]!
    } else {
      membersByMonth[m] = lastMember
    }
  }

  barChart?.setOption({
    title: { text: '活动数量（按月）', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: months },
    yAxis: { type: 'value', name: '活动次数' },
    series: [
      {
        name: '活动数量',
        type: 'bar',
        data: months.map((m) => activityByMonth[m]),
        itemStyle: { color: '#409eff' },
      },
    ],
    grid: { left: 48, right: 24, bottom: 32, top: 48 },
  })

  lineChart?.setOption({
    title: { text: '成员人数趋势（近12月）', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: months },
    yAxis: { type: 'value', name: '成员数' },
    series: [
      {
        name: '成员总数',
        type: 'line',
        smooth: true,
        data: months.map((m) => membersByMonth[m]),
        itemStyle: { color: '#67c23a' },
        areaStyle: { color: 'rgba(103,194,58,0.15)' },
      },
    ],
    grid: { left: 48, right: 24, bottom: 32, top: 48 },
  })
}

function renderPieFromClubs() {
  const filtered = filters.clubId
    ? clubs.value.filter((c) => c.id === filters.clubId)
    : clubs.value
  const categoryCount: Record<string, number> = {}
  for (const club of filtered) {
    const cat = club.category || '未分类'
    categoryCount[cat] = (categoryCount[cat] || 0) + 1
  }
  const pieData = Object.entries(categoryCount).map(([name, value]) => ({ name, value }))

  pieChart?.setOption({
    title: { text: '社团分类占比', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left', top: 'middle' },
    series: [
      {
        name: '社团分类',
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['58%', '50%'],
        data: pieData.length ? pieData : [{ name: '暂无数据', value: 0 }],
        emphasis: {
          itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.2)' },
        },
      },
    ],
  })
}

function initCharts() {
  if (barRef.value) barChart = echarts.init(barRef.value)
  if (lineRef.value) lineChart = echarts.init(lineRef.value)
  if (pieRef.value) pieChart = echarts.init(pieRef.value)
}

function resizeCharts() {
  barChart?.resize()
  lineChart?.resize()
  pieChart?.resize()
}

function handleSearch() {
  loadStatistics()
}

function handleReset() {
  filters.clubId = undefined
  filters.dateRange = []
  loadStatistics()
}

onMounted(async () => {
  await loadClubs()
  await nextTick()
  initCharts()
  await loadStatistics()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  barChart?.dispose()
  lineChart?.dispose()
  pieChart?.dispose()
})

watch(
  () => filters.clubId,
  () => {
    if (pieChart) renderPieFromClubs()
  },
)
</script>

<template>
  <div v-loading="loading" class="page-container">
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true">
        <el-form-item label="社团">
          <el-select
            v-model="filters.clubId"
            placeholder="全部社团"
            clearable
            filterable
            style="width: 200px"
          >
            <el-option
              v-for="c in clubs"
              :key="c.id"
              :label="c.clubName"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <div ref="barRef" class="chart-box" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <div ref="lineRef" class="chart-box" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="pie-row">
      <el-col :span="12">
        <el-card shadow="never">
          <div ref="pieRef" class="chart-box" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="summary-card">
          <template #header>数据说明</template>
          <ul class="summary-list">
            <li>柱状图：按月份汇总各社团活动数量（activityCount）</li>
            <li>折线图：近 12 个月成员总数变化（totalMembers）</li>
            <li>饼图：按社团 category 字段统计分类占比</li>
            <li v-if="filters.clubId">
              当前筛选：{{ clubMap[filters.clubId] }}
            </li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped lang="scss">
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chart-box {
  height: 360px;
  width: 100%;
}

.pie-row {
  margin-top: 0;
}

.summary-card {
  height: 100%;
}

.summary-list {
  margin: 0;
  padding-left: 20px;
  color: #606266;
  line-height: 2;
  font-size: 14px;
}
</style>
