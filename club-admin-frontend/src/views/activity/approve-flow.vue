<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { activityApplyApi } from '@/api/activityApply'
import { activityApproveFlowApi } from '@/api/activityApproveFlow'
import { sysRoleApi } from '@/api/sysRole'
import { sysUserApi } from '@/api/sysUser'
import { useUserStore } from '@/stores/user'
import { formatDateTime } from '@/utils/format'
import type { ActivityApply, ActivityApproveFlow, SysRole, SysUser } from '@/types/generated'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const applyId = computed(() => Number(route.params.applyId))
const loading = ref(false)
const applyDetail = ref<ActivityApply | null>(null)
const flowList = ref<ActivityApproveFlow[]>([])

const roleMap = ref<Record<number, string>>({})
const userMap = ref<Record<number, string>>({})

const approveDialogVisible = ref(false)
const approveFormRef = ref<FormInstance>()
const approveAction = ref<1 | 2>(1)
const currentFlow = ref<ActivityApproveFlow | null>(null)
const approveForm = ref({
  approveOpinion: '',
})

const resultMap: Record<number, string> = {
  0: '待审批',
  1: '已通过',
  2: '已驳回',
}

const statusMap: Record<number, string> = {
  1: '草稿',
  2: '待审批',
  3: '审批中',
  4: '已通过',
  5: '已驳回',
  6: '已取消',
}

const activeStep = computed(() => {
  if (!applyDetail.value) return 0
  const current = applyDetail.value.currentApproveStep || 0
  const pendingIdx = flowList.value.findIndex((f) => !f.approveResult)
  if (pendingIdx >= 0) return pendingIdx
  return Math.min(current, flowList.value.length - 1)
})

const canApprove = computed(() => {
  const pending = flowList.value.find((f) => !f.approveResult || f.approveResult === 0)
  if (!pending) return false
  return pending.approveUsername === userStore.username
})

async function loadDicts() {
  const [roleRes, userRes] = await Promise.all([sysRoleApi.list(), sysUserApi.list()])
  roleMap.value = Object.fromEntries(
    (roleRes.data || []).map((r: SysRole) => [r.id!, r.roleName || '']),
  )
  userMap.value = Object.fromEntries(
    (userRes.data || []).map((u: SysUser) => [u.id!, u.realName || u.username || '']),
  )
}

async function loadData() {
  if (!applyId.value) return
  loading.value = true
  try {
    const detailRes = await activityApplyApi.detailF(applyId.value)
    applyDetail.value = detailRes.data || null
    const activityId = applyDetail.value?.id
    if (!activityId) return
    const flowRes = await activityApproveFlowApi.listF({
      page: 1,
      limit: 100,
      activityId,
    })
    const records = flowRes.data?.records || []
    flowList.value = records.sort((a, b) => (a.step || 0) - (b.step || 0))
  } finally {
    loading.value = false
  }
}

function openApprove(action: 1 | 2, flow: ActivityApproveFlow) {
  approveAction.value = action
  currentFlow.value = flow
  approveForm.value.approveOpinion = ''
  approveDialogVisible.value = true
}

async function submitApprove() {
  if (!currentFlow.value?.id) return
  await activityApproveFlowApi.updateF({
    id: currentFlow.value.id,
    approveResult: approveAction.value,
    approveOpinion: approveForm.value.approveOpinion,
  })
  ElMessage.success(approveAction.value === 1 ? '审批通过' : '已驳回')
  approveDialogVisible.value = false
  await loadData()
}

function goBack() {
  router.push({ path: '/activity/apply' })
}

onMounted(async () => {
  await loadDicts()
  await loadData()
})
</script>

<template>
  <div v-loading="loading" class="page-container">
    <el-page-header @back="goBack">
      <template #content>
        <span class="page-header-title">活动审批流程</span>
      </template>
    </el-page-header>

    <el-card v-if="applyDetail" shadow="never" class="detail-card">
      <template #header>
        <span>申请信息</span>
        <el-tag class="status-tag">
          {{ statusMap[applyDetail.approveStatus!] || '未知' }}
        </el-tag>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="活动编号">{{ applyDetail.activityNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="活动名称">{{ applyDetail.activityName }}</el-descriptions-item>
        <el-descriptions-item label="活动地点">{{ applyDetail.location || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">
          {{ formatDateTime(applyDetail.startTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="结束时间">
          {{ formatDateTime(applyDetail.endTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ formatDateTime(applyDetail.applyTime || applyDetail.createTime) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never">
      <template #header>审批步骤</template>
      <el-steps
        v-if="flowList.length"
        :active="activeStep"
        finish-status="success"
        align-center
        class="flow-steps"
      >
        <el-step
          v-for="flow in flowList"
          :key="flow.id"
          :title="`第${flow.step}步`"
          :description="roleMap[flow.approveRoleId!] || '审批'"
          :status="
            flow.approveResult === 2
              ? 'error'
              : flow.approveResult === 1
                ? 'success'
                : undefined
          "
        />
      </el-steps>
      <el-empty v-else description="暂无审批记录" />

      <el-table :data="flowList" border stripe class="flow-table">
        <el-table-column prop="step" label="步骤" width="70" />
        <el-table-column label="审批角色" width="120">
          <template #default="{ row }">{{ roleMap[row.approveRoleId] || '-' }}</template>
        </el-table-column>
        <el-table-column label="审批人" width="120">
          <template #default="{ row }">{{ row.approveUsername || userMap[row.approveUserId] || '-' }}</template>
        </el-table-column>
        <el-table-column label="审批结果" width="100">
          <template #default="{ row }">
            <el-tag
              :type="
                row.approveResult === 1
                  ? 'success'
                  : row.approveResult === 2
                    ? 'danger'
                    : 'info'
              "
            >
              {{ resultMap[row.approveResult ?? 0] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approveOpinion" label="审批意见" min-width="160" show-overflow-tooltip />
        <el-table-column label="审批时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.approveTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="(!row.approveResult || row.approveResult === 0) && row.approveUsername === userStore.username">
              <el-button link type="success" @click="openApprove(1, row)">同意</el-button>
              <el-button link type="danger" @click="openApprove(2, row)">驳回</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="canApprove" class="approve-actions">
        <el-alert type="info" show-icon :closable="false" title="您有待处理的审批任务，请在上方表格中操作" />
      </div>
    </el-card>

    <el-dialog
      v-model="approveDialogVisible"
      :title="approveAction === 1 ? '审批通过' : '审批驳回'"
      width="480px"
      destroy-on-close
    >
      <el-form ref="approveFormRef" :model="approveForm" label-width="80px">
        <el-form-item label="审批意见">
          <el-input
            v-model="approveForm.approveOpinion"
            type="textarea"
            :rows="3"
            :placeholder="approveAction === 2 ? '请填写驳回原因' : '可选'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button :type="approveAction === 1 ? 'success' : 'danger'" @click="submitApprove">
          确定
        </el-button>
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

.page-header-title {
  font-size: 16px;
  font-weight: 500;
}

.status-tag {
  margin-left: 12px;
}

.flow-steps {
  margin-bottom: 24px;
}

.flow-table {
  margin-top: 16px;
}

.approve-actions {
  margin-top: 16px;
}
</style>
