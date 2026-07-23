<script setup lang="ts">
import { computed, inject, onActivated, onMounted, ref, type Ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { fetchDashboardBundle } from '@/api/dashboard'
import { useUserStore } from '@/stores/user'
import type { DashboardKpiCard, DashboardTaskItem, NoticeInfo } from '@/types/generated'

defineOptions({ name: 'DashboardWorkbench' })

const router = useRouter()
const userStore = useUserStore()
const dashboardRefreshKey = inject<Ref<number>>('dashboardRefreshKey', ref(0))

const loading = ref(true)
const kpis = ref<DashboardKpiCard[]>([])
const overdueCount = ref(0)
const tasks = ref<DashboardTaskItem[]>([])
const notices = ref<NoticeInfo[]>([])

/** 仅 Level 0~3 有活动管理权限，展示任务流；Level 4 普通学生无此区域 */
const hasActivityAccess = computed(() => userStore.effectiveLevel <= 3)

/** 当前用户能否访问指定路由（根据路由 meta.minLevel 判定） */
const ROUTE_MIN_LEVEL: Record<string, number> = {
  '/activity/apply': 4,
  '/activity/sign': 2,
  '/club': 2,
  '/member': 2,
  '/statistics': 2,
}
function canAccessRoute(route: string): boolean {
  const min = ROUTE_MIN_LEVEL[route]
  if (min === undefined) return true
  return userStore.effectiveLevel <= min
}

function relativeTime(value?: string): string {
  if (!value) return ''
  const t = new Date(value.replace('T', ' ')).getTime()
  if (Number.isNaN(t)) return value
  const diff = Date.now() - t
  const hours = Math.floor(diff / 3600000)
  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}天前`
  return value.slice(0, 10)
}

async function loadDashboard() {
  loading.value = true
  try {
    const data = await fetchDashboardBundle(userStore.effectiveLevel)
    kpis.value = data.kpis
    overdueCount.value = data.overdueCount
    tasks.value = data.tasks
    notices.value = data.notices
    userStore.setUnreadCount(data.unreadCount)
  } finally {
    loading.value = false
  }
}

function navigate(route: string, query?: Record<string, string>) {
  router.push({ path: route, query })
}

function onKpiClick(card: DashboardKpiCard) {
  if (!canAccessRoute(card.route)) return
  navigate(card.route, card.query)
}

function onTaskClick(item: DashboardTaskItem) {
  navigate(item.route, item.query)
}

function onOverdueClick() {
  navigate('/activity/apply', { overdue: 'true' })
}

function switchClub(clubId: number) {
  userStore.switchPrimaryClub(clubId)
  loadDashboard()
}

onMounted(loadDashboard)
onActivated(loadDashboard)

watch(dashboardRefreshKey, () => {
  loadDashboard()
})
</script>

<template>
  <div class="workbench">
    <!-- II区：系统预警条（条件渲染） -->
    <div v-if="overdueCount > 0" class="alert-bar">
      <span>
        系统检测到 {{ overdueCount }} 项活动审批严重超时（&gt;7天），建议尽快处理。
      </span>
      <button type="button" class="link" @click="onOverdueClick">查看详情</button>
    </div>

    <!-- III区：核心指标卡 -->
    <section class="kpi-section">
      <el-skeleton v-if="loading" animated :rows="0">
        <template #template>
          <div class="kpi-grid">
            <el-skeleton-item
              v-for="i in 4"
              :key="i"
              variant="rect"
              style="height: 120px; border-radius: 12px"
            />
          </div>
        </template>
      </el-skeleton>
      <div v-else class="kpi-grid" :style="{ '--kpi-cols': Math.max(kpis.length, 1) }">
        <button
          v-for="card in kpis"
          :key="card.key"
          type="button"
          class="kpi-card"
          :class="{
            'kpi-card--emphasis': (card.value ?? 0) > 0 && card.key.includes('pending'),
            'kpi-card--readonly': !canAccessRoute(card.route),
          }"
          @click="onKpiClick(card)"
        >
          <div class="kpi-card__label">{{ card.label }}</div>
          <div class="kpi-card__value">
            {{ card.value == null ? '—' : card.value }}
          </div>
          <div v-if="card.changePercent != null" class="kpi-card__change">
            <span :class="card.changePercent >= 0 ? 'up' : 'down'">
              {{ card.changePercent >= 0 ? '↑' : '↓' }}
              {{ Math.abs(card.changePercent) }}%
            </span>
          </div>
        </button>
      </div>
    </section>

    <!-- IV区：主内容 65% / 35%（Level 4 无任务流时右侧全宽） -->
    <section class="main-panel" :class="{ 'main-panel--single': !hasActivityAccess }">
      <div v-if="hasActivityAccess" class="panel-left">
        <div class="panel-head">
          <h3>
            {{ userStore.effectiveLevel <= 1 ? '待审批任务' : '我的申请进度' }}
          </h3>
        </div>
        <el-skeleton v-if="loading" :rows="5" animated />
        <div v-else-if="!tasks.length" class="empty">暂无相关任务</div>
        <div v-else class="task-list">
          <button
            v-for="item in tasks"
            :key="item.id"
            type="button"
            class="task-item"
            :class="{
              'is-warn': item.stayDays > 3 && item.stayDays <= 7,
              'is-danger': item.stayDays > 7,
            }"
            @click="onTaskClick(item)"
          >
            <div class="task-item__title">{{ item.title }}</div>
            <div class="task-item__sub">{{ item.subtitle }}</div>
            <div class="task-item__meta">
              <span>{{ item.statusText }}</span>
              <span v-if="item.stayDays > 0">停留 {{ item.stayDays }} 天</span>
            </div>
          </button>
        </div>
      </div>

      <div class="panel-right">
        <div class="side-block side-block--notices">
          <div class="panel-head">
            <h3>最新动态 / 通知</h3>
          </div>
          <el-skeleton v-if="loading" :rows="3" animated />
          <template v-else>
            <div v-if="!notices.length" class="empty">暂无通知</div>
            <button
              v-for="n in notices"
              :key="n.id"
              type="button"
              class="notice-item"
              @click="navigate('/notice', { id: String(n.id) })"
            >
              <div class="notice-item__title">{{ n.title }}</div>
              <div class="notice-item__time">{{ relativeTime(n.publishTime || n.createTime) }}</div>
            </button>
            <button type="button" class="link-all" @click="navigate('/notice')">
              查看全部通知
            </button>
          </template>
        </div>

        <div class="side-block side-block--profile">
          <div class="panel-head">
            <h3>我的画像</h3>
          </div>
          <div class="profile">
            <div
              class="profile__avatar"
              :class="{ 'profile__avatar--gold': userStore.hasGoldAvatarRing }"
            >
              {{ (userStore.displayName || 'U').slice(0, 1) }}
            </div>
            <div>
              <div class="profile__name">{{ userStore.displayName }}</div>
              <div class="profile__role">{{ userStore.roleName }}</div>
              <div class="profile__scope">
                <span v-if="userStore.primaryClubId">社团 #{{ userStore.primaryClubId }}</span>
                <span v-else-if="userStore.primaryDepartmentId">
                  部门 #{{ userStore.primaryDepartmentId }}
                </span>
                <span v-else>平台范围</span>
              </div>
            </div>
          </div>
          <div v-if="userStore.clubScopeIds.length > 1" class="club-switch">
            <span>切换社团：</span>
            <button
              v-for="cid in userStore.clubScopeIds"
              :key="cid"
              type="button"
              class="link"
              :class="{ active: cid === userStore.primaryClubId }"
              @click="switchClub(cid)"
            >
              #{{ cid }}
            </button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.workbench {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: calc(100vh - 64px - 40px);
}

.alert-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 40px;
  padding: 8px 16px;
  background: var(--brand-alert-bg);
  border-left: 4px solid var(--brand-gold);
  border-radius: 0 8px 8px 0;
  color: var(--brand-danger);
  font-size: 13px;
}

.link {
  border: none;
  background: transparent;
  color: var(--brand-interactive);
  cursor: pointer;
  font-size: 13px;
  white-space: nowrap;

  &:hover {
    text-decoration: underline;
  }

  &.active {
    color: var(--brand-gold);
    font-weight: 600;
  }
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(var(--kpi-cols, 4), minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 1100px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .kpi-grid {
    grid-template-columns: 1fr;
  }
}

.kpi-card {
  text-align: left;
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: var(--brand-radius);
  padding: 24px;
  cursor: pointer;
  background: var(--brand-glass);
  backdrop-filter: blur(12px);
  box-shadow: 0 4px 16px rgba(30, 58, 95, 0.06);
  transition: transform 0.15s ease, box-shadow 0.15s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(30, 58, 95, 0.1);
  }

  &--emphasis {
    background: linear-gradient(160deg, var(--brand-gold-soft), var(--brand-glass));
  }

  &--readonly {
    cursor: default;
    opacity: 0.7;

    &:hover {
      transform: none;
      box-shadow: 0 4px 16px rgba(30, 58, 95, 0.06);
    }
  }
}

.kpi-card__label {
  font-size: 13px;
  color: #607089;
}

.kpi-card__value {
  margin-top: 10px;
  font-size: 34px;
  font-weight: 700;
  color: var(--brand-gold);
  line-height: 1.1;
}

.kpi-card__change {
  margin-top: 8px;
  font-size: 12px;

  .up {
    color: #27ae60;
  }
  .down {
    color: var(--brand-danger);
  }
}

.main-panel {
  display: grid;
  grid-template-columns: 65fr 35fr;
  gap: 16px;
  flex: 1;
  min-height: 360px;

  &--single {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 960px) {
  .main-panel {
    grid-template-columns: 1fr;
  }
}

.panel-left,
.side-block {
  background: #fff;
  border-radius: var(--brand-radius);
  padding: 16px 18px;
  box-shadow: 0 2px 10px rgba(30, 58, 95, 0.04);
}

.panel-right {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
}

.side-block--notices {
  flex: 6;
}

.side-block--profile {
  flex: 4;
}

.panel-head h3 {
  margin: 0 0 12px;
  font-size: 15px;
  color: var(--brand-deep);
}

.empty {
  color: #909399;
  font-size: 13px;
  padding: 24px 0;
  text-align: center;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-item {
  position: relative;
  width: 100%;
  text-align: left;
  border: 1px solid #eef2f6;
  border-radius: 10px;
  background: #fff;
  padding: 12px 14px 12px 16px;
  cursor: pointer;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 8px;
    bottom: 8px;
    width: 3px;
    border-radius: 2px;
    background: transparent;
  }

  &:hover {
    border-color: var(--brand-interactive);
    background: var(--brand-hover);
  }

  &.is-warn::before {
    background: var(--brand-gold);
  }

  &.is-danger {
    background: rgba(231, 76, 60, 0.04);
    animation: pulse-danger 2.4s ease-in-out infinite;

    &::before {
      background: var(--brand-danger);
    }
  }
}

@keyframes pulse-danger {
  0%,
  100% {
    background: rgba(231, 76, 60, 0.03);
  }
  50% {
    background: rgba(231, 76, 60, 0.08);
  }
}

.task-item__title {
  font-size: 14px;
  font-weight: 600;
  color: var(--brand-deep);
}

.task-item__sub,
.task-item__meta {
  margin-top: 4px;
  font-size: 12px;
  color: #8090a0;
}

.task-item__meta {
  display: flex;
  justify-content: space-between;
}

.notice-item {
  width: 100%;
  border: none;
  background: transparent;
  text-align: left;
  padding: 10px 0;
  border-bottom: 1px solid #f2f4f7;
  cursor: pointer;

  &:hover .notice-item__title {
    color: var(--brand-interactive);
  }
}

.notice-item__title {
  font-size: 13px;
  color: #303133;
  line-height: 1.45;
  word-break: break-word;
}

.notice-item__time {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.link-all {
  margin-top: 8px;
  border: none;
  background: transparent;
  color: var(--brand-interactive);
  cursor: pointer;
  font-size: 13px;
  padding: 4px 0;
}

.profile {
  display: flex;
  gap: 12px;
  align-items: center;
}

.profile__avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--brand-interactive);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  border: 2px solid #fff;

  &--gold {
    box-shadow: 0 0 0 2px var(--brand-gold);
  }
}

.profile__name {
  font-weight: 700;
  color: var(--brand-deep);
}

.profile__role,
.profile__scope {
  font-size: 12px;
  color: #8090a0;
  margin-top: 2px;
}

.club-switch {
  margin-top: 12px;
  font-size: 12px;
  color: #8090a0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
</style>
