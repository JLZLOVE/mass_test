<script setup lang="ts">
import { ref, reactive, onMounted, onActivated, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshRight, View } from '@element-plus/icons-vue'
import { sysClubApi } from '@/api/sysClub'
import { clubApplicationApi } from '@/api/clubApplication'
import { sysCollegeApi } from '@/api/sysCollege'
import { useUserStore } from '@/stores/user'
import { useMemberCount } from '@/composables/useMemberCount'
import { LEVEL } from '@/utils/level'
import { formatDateTime } from '@/utils/format'
import type { ClubCategoryItem, SysClubItem, SysCollege } from '@/types/generated'

defineOptions({ name: 'ClubList' })

const router = useRouter()
const userStore = useUserStore()
const { getCount, isLoading, isFailed, fetchCounts, retryOne, invalidate } = useMemberCount()

type TabMode = 'normal' | 'dissolving' | 'council'

const loading = ref(false)
const tableData = ref<SysClubItem[]>([])
const total = ref(0)
const activeTab = ref<TabMode>('normal')

const categories = ref<ClubCategoryItem[]>([])
const colleges = ref<SysCollege[]>([])
const collegeLoading = ref(false)

const query = reactive({
  page: 1,
  limit: 10,
  category: '' as string,
  collegeId: undefined as number | undefined,
  status: undefined as number | undefined,
  keyword: '',
})

let searchTimer: ReturnType<typeof setTimeout> | null = null

const CATEGORY_COLOR: Record<string, string> = {
  SZ: '#2563eb',
  XS: '#2563eb',
  CX: '#f97316',
  WH: '#c9a227',
  GY: '#16a34a',
  ZL: '#7c3aed',
}

const canCreate = computed(() => userStore.effectiveLevel <= LEVEL.ADMIN)

/** Level 3 部长：只读视图，仅可查看挂靠社团基本信息 */
const isDeptLeader = computed(() => userStore.effectiveLevel === LEVEL.DEPT_LEADER)

/** Level 0/1 超管/管理员：可查看创建时间 */
const canViewCreateTime = computed(() => userStore.effectiveLevel <= LEVEL.ADMIN)

async function loadDicts() {
  const [catRes] = await Promise.all([clubApplicationApi.categories(), searchColleges('')])
  const raw = catRes.data
  if (Array.isArray(raw) && raw.length && typeof raw[0] === 'object') {
    categories.value = raw as ClubCategoryItem[]
  } else if (Array.isArray(raw)) {
    // 兼容旧接口返回纯字符串
    categories.value = (raw as unknown as string[]).map((label) => ({ code: label, label }))
  }
}

async function searchColleges(keyword: string) {
  collegeLoading.value = true
  try {
    const res = await sysCollegeApi.list({ keyword: keyword || undefined })
    colleges.value = res.data || []
  } catch {
    colleges.value = []
  } finally {
    collegeLoading.value = false
  }
}

async function fetchList() {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      page: query.page,
      limit: query.limit,
      tabMode: activeTab.value,
      category: query.category || undefined,
      collegeId: query.collegeId,
      status: activeTab.value === 'normal' ? query.status : undefined,
      keyword: query.keyword || undefined,
    }

    // 社长传社团ID，部长传部门ID做数据范围过滤（与 JWT 范围取交集，不得扩大）
    const level = userStore.effectiveLevel
    if (level === LEVEL.CLUB_LEADER) {
      const clubId = userStore.primaryClubId ?? userStore.clubScopeIds[0]
      if (clubId != null) {
        params.scopeType = 2
        params.scopeId = clubId
      }
    } else if (level === LEVEL.DEPT_LEADER && userStore.primaryDepartmentId != null) {
      params.scopeType = 3
      params.scopeId = userStore.primaryDepartmentId
    }
    // 超管/院长不传，看全量

    const res = await sysClubApi.list(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
    const ids = tableData.value.map((r) => r.id!).filter(Boolean)
    if (ids.length) await fetchCounts(ids)
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchList()
}

function handleReset() {
  query.category = ''
  query.collegeId = undefined
  query.status = undefined
  query.keyword = ''
  query.page = 1
  fetchList()
}

function onKeywordInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    query.page = 1
    fetchList()
  }, 300)
}

function onTabChange(name: string | number) {
  activeTab.value = name as TabMode
  query.status = undefined
  query.page = 1
  invalidate()
  fetchList()
}

function categoryColor(code?: string) {
  return CATEGORY_COLOR[code || ''] || '#94a3b8'
}

function goDetail(row: SysClubItem) {
  if (!row.clubCode) return
  router.push({
    path: `/club/detail/${row.clubCode}`,
    query: { from: 'club-list' },
  })
}

function goCouncil(row: SysClubItem) {
  if (!row.id) return
  router.push({
    path: `/club/council/${row.id}`,
    query: { from: 'club-list' },
  })
}

async function handleDissolve(row: SysClubItem) {
  if (!row.clubCode || !row.clubName) return
  try {
    const { value } = await ElMessageBox.prompt(
      `请输入社团名称「${row.clubName}」以确认解散申请`,
      '解散申请确认',
      {
        confirmButtonText: '提交申请',
        cancelButtonText: '取消',
        inputPattern: new RegExp(`^${escapeRegExp(row.clubName)}$`),
        inputErrorMessage: '社团名称不匹配',
        type: 'warning',
        inputPlaceholder: '请输入社团名称',
      },
    )
    if (value !== row.clubName) return

    const reasonBox = await ElMessageBox.prompt('请填写解散原因', '解散原因', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputValidator: (v) => (!!v && v.trim().length >= 2) || '请至少填写 2 个字',
    })

    await clubApplicationApi.dissolve({
      clubCode: row.clubCode,
      dissolveReason: String(reasonBox.value || '').trim(),
    })
    ElMessage.success('解散申请已提交，等待审批')
    activeTab.value = 'dissolving'
    query.page = 1
    await fetchList()
  } catch (e: unknown) {
    if (e === 'cancel' || e === 'close') return
    const msg = e instanceof Error ? e.message : String(e)
    if (msg.includes('进行中的解散') || msg.includes('7207')) {
      ElMessageBox.alert('该社团已有进行中的解散流程，请勿重复操作', '提示', { type: 'warning' })
      fetchList()
    }
  }
}

function escapeRegExp(s: string) {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

// —— 创建社团（超管/院长） ——
const createVisible = ref(false)
const createSubmitting = ref(false)
const createForm = reactive({
  clubName: '',
  collegeId: undefined as number | undefined,
  category: '',
  proposedLeaderUsername: '',
  maxMembers: 50,
  description: '',
})

function openCreate() {
  createForm.clubName = ''
  createForm.collegeId = undefined
  createForm.category = ''
  createForm.proposedLeaderUsername = ''
  createForm.maxMembers = 50
  createForm.description = ''
  createVisible.value = true
}

async function submitCreate() {
  if (!createForm.clubName || !createForm.collegeId || !createForm.category || !createForm.proposedLeaderUsername) {
    ElMessage.warning('请完善必填项')
    return
  }
  createSubmitting.value = true
  try {
    const res = await clubApplicationApi.create({
      clubName: createForm.clubName,
      collegeId: createForm.collegeId,
      category: createForm.category,
      proposedLeaderUsername: createForm.proposedLeaderUsername,
      maxMembers: createForm.maxMembers,
      description: createForm.description || undefined,
    })
    ElMessage.success(`创建申请已提交，申请编号：${res.data || ''}`)
    createVisible.value = false
  } finally {
    createSubmitting.value = false
  }
}

onMounted(async () => {
  await loadDicts()
  await fetchList()
})

onActivated(() => {
  // keep-alive 保留筛选条件，仅按当前 query 静默刷新列表
  fetchList()
})
</script>

<template>
  <div class="page-container">
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="社团类别">
          <el-select v-model="query.category" placeholder="全部" clearable style="width: 150px">
            <el-option
              v-for="c in categories"
              :key="c.code"
              :label="c.label"
              :value="c.label"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属学院">
          <el-select
            v-model="query.collegeId"
            filterable
            remote
            clearable
            reserve-keyword
            placeholder="搜索学院"
            :remote-method="searchColleges"
            :loading="collegeLoading"
            style="width: 180px"
          >
            <el-option
              v-for="c in colleges"
              :key="c.id"
              :label="c.collegeName"
              :value="c.id!"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="activeTab === 'normal'" label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
            <el-option label="正常" :value="1" />
            <el-option label="已解散" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="query.keyword"
            clearable
            placeholder="请输入社团名称或社团编号"
            style="width: 240px"
            @input="onKeywordInput"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button v-if="canCreate" type="success" @click="openCreate">创建社团</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-alert
        v-if="isDeptLeader"
        type="info"
        :closable="true"
        show-icon
        class="dept-notice"
      >
        当前为只读视图。如需管理本部门成员，请前往「成员管理」；如需发起活动，请前往「活动管理」。
      </el-alert>
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="正常社团" name="normal" />
        <el-tab-pane v-if="!isDeptLeader" label="申请解散中" name="dissolving" />
        <el-tab-pane v-if="!isDeptLeader" label="合议中" name="council" />
      </el-tabs>

      <el-table v-loading="loading" :data="tableData" border stripe empty-text=" ">
        <el-table-column label="社团编号" width="140">
          <template #default="{ row }">
            <span class="club-code">{{ row.clubCode || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="社团名称" min-width="160">
          <template #default="{ row }">
            <div class="name-cell">
              <span class="cat-bar" :style="{ background: categoryColor(row.categoryCode) }" />
              <el-button link type="primary" @click="goDetail(row)">{{ row.clubName }}</el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="社团类别" width="120" />
        <el-table-column label="指导老师" width="120">
          <template #default="{ row }">
            <span v-if="row.advisorName">{{ row.advisorName }}</span>
            <span v-else class="muted">待指派</span>
          </template>
        </el-table-column>
        <el-table-column label="成员总数" width="110">
          <template #default="{ row }">
            <el-skeleton v-if="isLoading(row.id)" :rows="1" animated style="width: 48px" />
            <template v-else-if="isFailed(row.id)">
              <span class="muted">--</span>
              <el-button link type="primary" :icon="RefreshRight" @click="retryOne(row.id!)" />
            </template>
            <span v-else>{{ getCount(row.id) ?? 0 }}人</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span class="status-dot" :class="row.status === 1 ? 'ok' : 'off'" />
            {{ row.status === 1 ? '正常' : '已解散' }}
          </template>
        </el-table-column>
        <el-table-column v-if="canViewCreateTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="goDetail(row)">查看详情</el-button>
            <el-button
              v-if="row.canDissolve && activeTab === 'normal'"
              link
              type="danger"
              @click="handleDissolve(row)"
            >
              解散申请
            </el-button>
            <el-button
              v-if="row.canSignCouncil"
              link
              type="warning"
              @click="goCouncil(row)"
            >
              合议签字
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="当前暂无匹配社团">
            <template #default>
              <p class="empty-hint">请尝试调整筛选条件或清除搜索关键词</p>
            </template>
          </el-empty>
        </template>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.limit"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @current-change="fetchList"
          @size-change="() => { query.page = 1; fetchList() }"
        />
      </div>
    </el-card>

    <el-dialog v-model="createVisible" title="创建社团申请" width="520px" destroy-on-close>
      <el-form label-width="120px">
        <el-form-item label="社团名称" required>
          <el-input v-model="createForm.clubName" maxlength="40" />
        </el-form-item>
        <el-form-item label="挂靠学院" required>
          <el-select
            v-model="createForm.collegeId"
            filterable
            remote
            :remote-method="searchColleges"
            :loading="collegeLoading"
            style="width: 100%"
          >
            <el-option
              v-for="c in colleges"
              :key="c.id"
              :label="c.collegeName"
              :value="c.id!"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="社团类别" required>
          <el-select v-model="createForm.category" style="width: 100%">
            <el-option
              v-for="c in categories"
              :key="c.code"
              :label="c.label"
              :value="c.label"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="拟定社长账号" required>
          <el-input v-model="createForm.proposedLeaderUsername" placeholder="username" />
        </el-form-item>
        <el-form-item label="最大人数" required>
          <el-input-number v-model="createForm.maxMembers" :min="3" :max="300" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="createForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="createSubmitting" @click="submitCreate">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}
.club-code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  letter-spacing: 0.02em;
}
.name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.cat-bar {
  width: 2px;
  height: 16px;
  border-radius: 1px;
  flex-shrink: 0;
}
.muted {
  color: #909399;
}
.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;
}
.status-dot.ok {
  background: #22c55e;
}
.status-dot.off {
  background: #9ca3af;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.empty-hint {
  margin: 0;
  color: #909399;
  font-size: 13px;
}
.dept-notice {
  margin-bottom: 16px;
}
</style>
