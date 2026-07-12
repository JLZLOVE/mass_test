<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { activitySignApi, type SignRecord } from '@/api/activitySign'
import { activityApplyApi } from '@/api/activityApply'
import { sysUserApi } from '@/api/sysUser'
import { formatDateTime } from '@/utils/format'
import type { ActivityApply, SysUser } from '@/types/generated'

const route = useRoute()
const router = useRouter()

const activityId = computed(() => Number(route.params.activityId))
const loading = ref(false)
const tableData = ref<SignRecord[]>([])
const total = ref(0)
const activityInfo = ref<ActivityApply | null>(null)

const query = reactive({
  page: 1,
  limit: 10,
})

const userList = ref<SysUser[]>([])

const signTypeMap: Record<number, string> = {
  1: '扫码签到',
  2: '定位签到',
  3: '手动补签',
}

const makeupVisible = ref(false)
const makeupFormRef = ref<FormInstance>()
const makeupForm = reactive({
  username: '',
})

const makeupRules: FormRules = {
  username: [{ required: true, message: '请选择用户', trigger: 'change' }],
}

async function loadUsers() {
  const res = await sysUserApi.listF({ page: 1, limit: 500, status: 1 })
  userList.value = res.data?.records || []
}

async function loadActivity() {
  if (!activityId.value) return
  const res = await activityApplyApi.detailF(activityId.value)
  activityInfo.value = res.data || null
}

async function fetchList() {
  if (!activityId.value) return
  loading.value = true
  try {
    const res = await activitySignApi.listRecords(activityId.value, {
      page: query.page,
      limit: query.limit,
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function openMakeup() {
  makeupForm.username = ''
  makeupVisible.value = true
}

async function submitMakeup() {
  const valid = await makeupFormRef.value?.validate().catch(() => false)
  if (!valid) return
  await activitySignApi.adminSign(activityId.value, { username: makeupForm.username })
  ElMessage.success('补签成功')
  makeupVisible.value = false
  fetchList()
}

function goBack() {
  router.push({ path: '/activity/apply' })
}

onMounted(async () => {
  await loadUsers()
  await loadActivity()
  await fetchList()
})
</script>

<template>
  <div class="page-container">
    <el-page-header @back="goBack">
      <template #content>
        <span class="page-header-title">
          签到记录 — {{ activityInfo?.activityName || `活动#${activityId}` }}
        </span>
      </template>
    </el-page-header>

    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" @click="openMakeup">手动补签</el-button>
      </div>
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column label="签到时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.signTime) }}</template>
        </el-table-column>
        <el-table-column label="签到方式" width="110">
          <template #default="{ row }">
            {{ signTypeMap[row.signType!] || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="迟到" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isLate === 1" type="warning" size="small">是</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="签到地址" min-width="160" show-overflow-tooltip />
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

    <el-dialog v-model="makeupVisible" title="手动补签" width="440px" destroy-on-close>
      <el-form ref="makeupFormRef" :model="makeupForm" :rules="makeupRules" label-width="80px">
        <el-form-item label="选择用户" prop="username">
          <el-select
            v-model="makeupForm.username"
            placeholder="请选择用户"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="u in userList"
              :key="u.username"
              :label="`${u.realName || u.username} (${u.username})`"
              :value="u.username!"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="makeupVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMakeup">确认补签</el-button>
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

.toolbar {
  margin-bottom: 12px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
