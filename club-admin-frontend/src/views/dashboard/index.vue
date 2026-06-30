<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { sysClubApi } from '@/api/sysClub'
import { activityApplyApi } from '@/api/activityApply'
import { sysUserApi } from '@/api/sysUser'
import { noticeInfoApi } from '@/api/noticeInfo'
import { useUserStore } from '@/stores/user'
import { OfficeBuilding, Calendar, User, Bell } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const stats = ref({
  clubCount: 0,
  activityCount: 0,
  memberCount: 0,
  noticeCount: 0,
})

const cards = [
  {
    key: 'clubCount',
    title: '社团总数',
    icon: OfficeBuilding,
    color: '#409eff',
    path: '/club',
  },
  {
    key: 'activityCount',
    title: '活动申请',
    icon: Calendar,
    color: '#67c23a',
    path: '/activity/apply',
  },
  {
    key: 'memberCount',
    title: '成员总数',
    icon: User,
    color: '#e6a23c',
    path: '/member',
  },
  {
    key: 'noticeCount',
    title: '通知公告',
    icon: Bell,
    color: '#f56c6c',
    path: '/notice',
  },
] as const

async function loadStats() {
  loading.value = true
  try {
    const [clubRes, activityRes, userRes, noticeRes] = await Promise.all([
      sysClubApi.listF({ page: 1, limit: 1, status: 1 }),
      activityApplyApi.listF({ page: 1, limit: 1 }),
      sysUserApi.listF({ page: 1, limit: 1, status: 1 }),
      noticeInfoApi.listF({ page: 1, limit: 1, status: 1 }),
    ])
    stats.value = {
      clubCount: clubRes.data?.total || 0,
      activityCount: activityRes.data?.total || 0,
      memberCount: userRes.data?.total || 0,
      noticeCount: noticeRes.data?.total || 0,
    }
  } finally {
    loading.value = false
  }
}

function navigate(path: string) {
  router.push(path)
}

onMounted(loadStats)
</script>

<template>
  <div v-loading="loading" class="dashboard">
    <el-card shadow="never" class="welcome-card">
      <h2>欢迎回来，{{ userStore.userInfo?.realName || userStore.username }}</h2>
      <p>社团综合管理平台 — 数据概览</p>
    </el-card>

    <el-row :gutter="16" class="stat-row">
      <el-col v-for="card in cards" :key="card.key" :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card" @click="navigate(card.path)">
          <div class="stat-inner">
            <div
              class="stat-icon"
              :style="{ background: `${card.color}20`, color: card.color }"
            >
              <el-icon :size="28"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats[card.key] }}</div>
              <div class="stat-title">{{ card.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>快捷入口</template>
          <div class="quick-links">
            <el-button @click="navigate('/club')">社团管理</el-button>
            <el-button @click="navigate('/activity/apply')">活动申请</el-button>
            <el-button @click="navigate('/member')">成员管理</el-button>
            <el-button @click="navigate('/notice')">通知公告</el-button>
            <el-button @click="navigate('/statistics')">统计看板</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>系统提示</template>
          <ul class="tips-list">
            <li>活动申请提交后可在「审批流程」页面查看进度</li>
            <li>已通过的活动可进行签到管理与手动补签</li>
            <li>普通成员无法查看预算相关字段</li>
            <li>统计数据支持按社团与日期范围筛选</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped lang="scss">
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.welcome-card {
  h2 {
    margin: 0 0 8px;
    font-size: 22px;
    color: #303133;
  }
  p {
    margin: 0;
    color: #909399;
    font-size: 14px;
  }
}

.stat-card {
  cursor: pointer;
  margin-bottom: 16px;
}

.stat-inner {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.quick-links {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  color: #606266;
  line-height: 2;
  font-size: 14px;
}
</style>
