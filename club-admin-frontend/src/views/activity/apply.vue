<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { activityApplyApi } from '@/api/activityApply'
import { activityCategoryApi } from '@/api/activityCategory'
import { sysClubApi } from '@/api/sysClub'
import { useUserStore } from '@/stores/user'
import { formatDateTime } from '@/utils/format'
import type { ActivityApply, ActivityCategory, SysClub } from '@/types/generated'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const tableData = ref<ActivityApply[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  limit: 10,
  activityName: '',
  approveStatus: undefined as number | undefined,
})

const statusMap: Record<number, { label: string; type: '' | 'success' | 'warning' | 'info' | 'danger' }> = {
  1: { label: '草稿', type: 'info' },
  2: { label: '待审批', type: 'warning' },
  3: { label: '审批中', type: 'warning' },
  4: { label: '已通过', type: 'success' },
  5: { label: '已驳回', type: 'danger' },
  6: { label: '已取消', type: 'info' },
}

const statusOptions = Object.entries(statusMap).map(([k, v]) => ({
  value: Number(k),
  label: v.label,
}))

const activityTypeOptions = [
  { label: '常规活动', value: 1 },
  { label: '大型活动', value: 2 },
  { label: '外出活动', value: 3 },
]

const clubMap = ref<Record<number, string>>({})
const categoryMap = ref<Record<number, string>>({})
const clubs = ref<SysClub[]>([])
const categories = ref<ActivityCategory[]>([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增活动申请')
const formRef = ref<FormInstance>()
const form = reactive<Partial<ActivityApply>>({
  id: undefined,
  clubId: undefined,
  activityName: '',
  categoryId: undefined,
  activityType: 1,
  startTime: '',
  endTime: '',
  location: '',
  locationDetail: '',
  expectedPeople: undefined,
  budget: undefined,
  activityContent: '',
  safetyPlan: '',
  approveStatus: 1,
})

const rules: FormRules = {
  activityName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  clubId: [{ required: true, message: '请选择社团', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择活动分类', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
}

const showBudget = computed(() => userStore.showBudget)

async function loadDicts() {
  const [clubRes, catRes] = await Promise.all([
    sysClubApi.list(),
    activityCategoryApi.list(),
  ])
  clubs.value = (clubRes.data || []).filter((c) => c.status === 1)
  categories.value = catRes.data || []
  clubMap.value = Object.fromEntries(clubs.value.map((c) => [c.id!, c.clubName || '']))
  categoryMap.value = Object.fromEntries(
    categories.value.map((c) => [c.id!, c.categoryName || '']),
  )
}

async function fetchList() {
  loading.value = true
  try {
    const res = await activityApplyApi.listF({
      page: query.page,
      limit: query.limit,
      activityName: query.activityName || undefined,
      approveStatus: query.approveStatus,
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchList()
}

function handleReset() {
  query.activityName = ''
  query.approveStatus = undefined
  query.page = 1
  fetchList()
}

function openDialog(row?: ActivityApply) {
  if (row) {
    if (row.approveStatus !== 1) {
      ElMessage.warning('仅草稿状态可编辑')
      return
    }
    dialogTitle.value = '编辑活动申请'
    Object.assign(form, { ...row })
  } else {
    dialogTitle.value = '新增活动申请'
    Object.assign(form, {
      id: undefined,
      clubId: userStore.clubScopeIds[0] || undefined,
      activityName: '',
      categoryId: undefined,
      activityType: 1,
      startTime: '',
      endTime: '',
      location: '',
      locationDetail: '',
      expectedPeople: undefined,
      budget: undefined,
      activityContent: '',
      safetyPlan: '',
      approveStatus: 1,
    })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  const payload = { ...form }
  if (!showBudget.value) {
    delete payload.budget
  }
  if (form.id) {
    await activityApplyApi.updateF(payload)
    ElMessage.success('更新成功')
  } else {
    await activityApplyApi.addF(payload)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row: ActivityApply) {
  await ElMessageBox.confirm(`确定撤销活动「${row.activityName}」吗？`, '提示', {
    type: 'warning',
  })
  await activityApplyApi.deleteF(row.id!)
  ElMessage.success('已撤销')
  fetchList()
}

async function handleSubmitApprove(row: ActivityApply) {
  if (row.approveStatus !== 1) return
  await ElMessageBox.confirm('确定提交审批吗？提交后不可再编辑。', '提示')
  await activityApplyApi.updateF({ id: row.id, approveStatus: 2 })
  ElMessage.success('已提交审批')
  fetchList()
}

function goApproveFlow(row: ActivityApply) {
  router.push({ name: 'activity-approve-flow', params: { applyId: String(row.id) } })
}

function goSign(row: ActivityApply) {
  router.push({ name: 'activity-sign', params: { activityId: String(row.id) } })
}

onMounted(async () => {
  await loadDicts()
  await fetchList()
})
</script>

<template>
  <div class="page-container">
    <el-card shadow="never">
      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="活动名称">
          <el-input v-model="query.activityName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.approveStatus" placeholder="全部" clearable style="width: 130px">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" @click="openDialog()">新增申请</el-button>
      </div>
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="activityNo" label="活动编号" width="150" />
        <el-table-column prop="activityName" label="活动名称" min-width="140" />
        <el-table-column label="社团" width="120">
          <template #default="{ row }">{{ clubMap[row.clubId] || '-' }}</template>
        </el-table-column>
        <el-table-column label="分类" width="110">
          <template #default="{ row }">{{ categoryMap[row.categoryId] || '-' }}</template>
        </el-table-column>
        <el-table-column label="开始时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column v-if="showBudget" prop="budget" label="预算(元)" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.approveStatus!]?.type || 'info'">
              {{ statusMap[row.approveStatus!]?.label || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.applyTime || row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.approveStatus === 1"
              link
              type="primary"
              @click="openDialog(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.approveStatus === 1"
              link
              type="success"
              @click="handleSubmitApprove(row)"
            >
              提交审批
            </el-button>
            <el-button link type="primary" @click="goApproveFlow(row)">审批流程</el-button>
            <el-button
              v-if="row.approveStatus === 4"
              link
              type="primary"
              @click="goSign(row)"
            >
              签到
            </el-button>
            <el-button
              v-if="row.approveStatus === 1 || row.approveStatus === 2"
              link
              type="danger"
              @click="handleDelete(row)"
            >
              撤销
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.limit"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="活动名称" prop="activityName">
          <el-input v-model="form.activityName" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="所属社团" prop="clubId">
          <el-select v-model="form.clubId" placeholder="请选择" style="width: 100%">
            <el-option
              v-for="c in clubs"
              :key="c.id"
              :label="c.clubName"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="活动分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择" style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.categoryName"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="活动类型">
          <el-select v-model="form.activityType" style="width: 100%">
            <el-option
              v-for="t in activityTypeOptions"
              :key="t.value"
              :label="t.label"
              :value="t.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="活动地点">
          <el-input v-model="form.location" placeholder="活动地点" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.locationDetail" placeholder="详细地址" />
        </el-form-item>
        <el-form-item label="预计人数">
          <el-input-number v-model="form.expectedPeople" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="showBudget" label="预算金额">
          <el-input-number v-model="form.budget" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="活动内容">
          <el-input v-model="form.activityContent" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="安全预案">
          <el-input v-model="form.safetyPlan" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar {
  margin-bottom: 12px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
