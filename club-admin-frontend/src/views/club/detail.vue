<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sysClubApi } from '@/api/sysClub'
import { activityApplyApi } from '@/api/activityApply'
import { useUserStore } from '@/stores/user'
import { LEVEL } from '@/utils/level'
import { formatDateTime } from '@/utils/format'
import type { ActivityApply, SysClubItem, SysDepartment, SysUserRole } from '@/types/generated'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const clubCode = computed(() => String(route.params.clubCode || ''))

/** Level 3 部长：只读视图，仅可查看本部门成员 */
const isDeptLeader = computed(() => userStore.effectiveLevel === LEVEL.DEPT_LEADER)
const loading = ref(false)
const club = ref<SysClubItem | null>(null)
const departments = ref<SysDepartment[]>([])
const members = ref<SysUserRole[]>([])
const memberTotal = ref(0)
const activities = ref<ActivityApply[]>([])

const memberQuery = reactive({
  page: 1,
  limit: 10,
  roleCode: '' as string,
})

const CATEGORY_COLOR: Record<string, string> = {
  SZ: '#2563eb',
  XS: '#2563eb',
  CX: '#f97316',
  WH: '#c9a227',
  GY: '#16a34a',
  ZL: '#7c3aed',
}

function buildDeptTree(list: SysDepartment[]) {
  const map = new Map<number, SysDepartment & { children?: SysDepartment[] }>()
  list.forEach((d) => map.set(d.id!, { ...d, children: [] }))
  const roots: (SysDepartment & { children?: SysDepartment[] })[] = []
  map.forEach((node) => {
    if (node.parentId && map.has(node.parentId)) {
      map.get(node.parentId)!.children!.push(node)
    } else {
      roots.push(node)
    }
  })
  return roots
}

const deptTree = computed(() => buildDeptTree(departments.value))

async function loadDetail() {
  loading.value = true
  try {
    const res = await sysClubApi.detail(clubCode.value)
    club.value = res.data || null
  } catch {
    ElMessage.warning('您无权查看该社团详情')
    router.replace('/club')
    return
  } finally {
    loading.value = false
  }
  await Promise.all([loadDepartments(), loadMembers(), loadActivities()])
}

async function loadDepartments() {
  try {
    const res = await sysClubApi.departments(clubCode.value)
    departments.value = res.data || []
  } catch {
    departments.value = []
  }
}

async function loadMembers() {
  try {
    const res = await sysClubApi.members(clubCode.value, {
      page: memberQuery.page,
      limit: memberQuery.limit,
      roleCode: memberQuery.roleCode || undefined,
    })
    members.value = res.data?.records || []
    memberTotal.value = res.data?.total || 0
  } catch {
    members.value = []
    memberTotal.value = 0
  }
}

async function loadActivities() {
  if (!club.value?.id) {
    activities.value = []
    return
  }
  try {
    const res = await activityApplyApi.list({
      page: 1,
      limit: 20,
      clubId: club.value.id,
    })
    activities.value = (res.data?.records || []).sort((a, b) =>
      String(b.startTime || '').localeCompare(String(a.startTime || '')),
    )
  } catch {
    activities.value = []
  }
}

function goBack() {
  router.push('/club')
}

onMounted(loadDetail)
</script>

<template>
  <div class="page-container" v-loading="loading">
    <el-page-header @back="goBack">
      <template #content>
        <span class="page-title">社团详情</span>
        <span v-if="club?.clubName" class="page-sub">{{ club.clubName }}</span>
      </template>
    </el-page-header>

    <el-alert
      v-if="isDeptLeader"
      type="info"
      :closable="true"
      show-icon
      class="dept-notice"
    >
      当前为只读视图，如需管理成员请前往「成员管理」。
    </el-alert>

    <el-card v-if="club" shadow="never" class="info-card">
      <div class="info-head">
        <span
          class="cat-bar"
          :style="{ background: CATEGORY_COLOR[club.categoryCode || ''] || '#94a3b8' }"
        />
        <div>
          <h2>{{ club.clubName }}</h2>
          <p class="club-code">{{ club.clubCode }}</p>
        </div>
        <el-tag :type="club.status === 1 ? 'success' : 'info'" class="status-tag">
          {{ club.status === 1 ? '正常' : '已解散' }}
        </el-tag>
      </div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="社团类别">{{ club.category || '—' }}</el-descriptions-item>
        <el-descriptions-item label="挂靠学院">{{ club.collegeName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="指导老师">
          {{ club.advisorName || '待指派' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(club.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">
          {{ club.description || '暂无简介' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-row :gutter="12">
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>部门架构</template>
          <el-tree
            v-if="deptTree.length"
            :data="deptTree"
            :props="{ label: 'deptName', children: 'children' }"
            default-expand-all
          />
          <el-empty v-else description="暂无部门" :image-size="64" />
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>成员列表</span>
              <el-select
                v-model="memberQuery.roleCode"
                clearable
                placeholder="按角色筛选"
                style="width: 160px"
                @change="() => { memberQuery.page = 1; loadMembers() }"
              >
                <el-option label="社长" value="CLUB_PRESIDENT" />
                <el-option label="部长" value="CLUB_MINISTER" />
                <el-option label="成员" value="MEMBER" />
              </el-select>
            </div>
          </template>
          <el-table :data="members" border stripe size="small">
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="realName" label="姓名" width="100" />
            <el-table-column prop="roleName" label="角色" width="120" />
            <el-table-column label="加入时间" min-width="160">
              <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
            </el-table-column>
          </el-table>
          <div class="pager">
            <el-pagination
              v-model:current-page="memberQuery.page"
              v-model:page-size="memberQuery.limit"
              :total="memberTotal"
              layout="total, prev, pager, next"
              small
              @current-change="loadMembers"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>历史活动</template>
      <el-timeline v-if="activities.length">
        <el-timeline-item
          v-for="act in activities"
          :key="act.id"
          :timestamp="formatDateTime(act.startTime)"
          placement="top"
        >
          <p class="act-name">{{ act.activityName }}</p>
          <p class="act-meta">{{ act.location || '地点待定' }} · 状态 {{ act.approveStatus }}</p>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无活动记录" :image-size="64" />
    </el-card>
  </div>
</template>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.page-title {
  font-weight: 600;
  color: var(--brand-deep);
}
.page-sub {
  margin-left: 10px;
  color: #606266;
  font-size: 14px;
}
.info-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.info-head h2 {
  margin: 0;
  font-size: 20px;
  color: var(--brand-deep);
}
.club-code {
  margin: 4px 0 0;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  color: #909399;
  font-size: 13px;
}
.cat-bar {
  width: 4px;
  height: 36px;
  border-radius: 2px;
}
.status-tag {
  margin-left: auto;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
.act-name {
  margin: 0;
  font-weight: 500;
}
.act-meta {
  margin: 4px 0 0;
  color: #909399;
  font-size: 12px;
}
.dept-notice {
  margin-bottom: 12px;
}
</style>
