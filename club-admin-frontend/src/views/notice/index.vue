<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { noticeInfoApi } from '@/api/noticeInfo'
import { noticeCategoryApi } from '@/api/noticeCategory'
import { noticeReadRecordApi } from '@/api/noticeReadRecord'
import { sysUserApi } from '@/api/sysUser'
import { formatDateTime } from '@/utils/format'
import type { NoticeInfo, NoticeCategory, NoticeReadRecord, SysUser } from '@/types/generated'

const loading = ref(false)
const tableData = ref<NoticeInfo[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  limit: 10,
  title: '',
  status: undefined as number | undefined,
})

const categoryMap = ref<Record<number, string>>({})
const categories = ref<NoticeCategory[]>([])
const userMap = ref<Record<number, string>>({})

const importanceOptions = [
  { label: '普通', value: 1 },
  { label: '重要', value: 2 },
  { label: '紧急', value: 3 },
]

const urgencyOptions = [
  { label: '一般', value: 1 },
  { label: '较急', value: 2 },
  { label: '特急', value: 3 },
]

const receiverTypeOptions = [
  { label: '全体', value: 1 },
  { label: '指定角色', value: 2 },
  { label: '指定社团', value: 3 },
  { label: '指定人员', value: 4 },
]

const statusOptions = [
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 },
  { label: '已撤回', value: 2 },
]

const dialogVisible = ref(false)
const dialogTitle = ref('发布通知')
const formRef = ref<FormInstance>()
const form = reactive<Partial<NoticeInfo>>({
  id: undefined,
  title: '',
  content: '',
  categoryId: undefined,
  importance: 1,
  urgency: 1,
  receiverType: 1,
  receiverValues: '',
  needConfirm: 0,
  expireTime: '',
  status: 1,
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
}

const readDialogVisible = ref(false)
const readLoading = ref(false)
const readRecords = ref<NoticeReadRecord[]>([])
const currentNotice = ref<NoticeInfo | null>(null)

async function loadDicts() {
  const [catRes, userRes] = await Promise.all([
    noticeCategoryApi.list(),
    sysUserApi.list(),
  ])
  categories.value = (catRes.data || []).filter((c) => c.status !== 0)
  categoryMap.value = Object.fromEntries(
    categories.value.map((c) => [c.id!, c.categoryName || '']),
  )
  userMap.value = Object.fromEntries(
    (userRes.data || []).map((u: SysUser) => [u.id!, u.realName || u.username || '']),
  )
}

async function fetchList() {
  loading.value = true
  try {
    const res = await noticeInfoApi.listF({
      page: query.page,
      limit: query.limit,
      title: query.title || undefined,
      status: query.status,
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
  query.title = ''
  query.status = undefined
  query.page = 1
  fetchList()
}

function openDialog(row?: NoticeInfo) {
  if (row) {
    dialogTitle.value = '编辑通知'
    Object.assign(form, { ...row })
  } else {
    dialogTitle.value = '发布通知'
    Object.assign(form, {
      id: undefined,
      title: '',
      content: '',
      categoryId: undefined,
      importance: 1,
      urgency: 1,
      receiverType: 1,
      receiverValues: '',
      needConfirm: 0,
      expireTime: '',
      status: 1,
    })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.id) {
    await noticeInfoApi.updateF(form)
    ElMessage.success('更新成功')
  } else {
    await noticeInfoApi.addF(form)
    ElMessage.success('发布成功')
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row: NoticeInfo) {
  await ElMessageBox.confirm(`确定撤回通知「${row.title}」吗？`, '提示', { type: 'warning' })
  await noticeInfoApi.deleteF(row.id!)
  ElMessage.success('已撤回')
  fetchList()
}

async function openReadRecords(row: NoticeInfo) {
  currentNotice.value = row
  readDialogVisible.value = true
  readLoading.value = true
  try {
    const res = await noticeReadRecordApi.listF({
      page: 1,
      limit: 200,
      noticeId: row.id,
    })
    readRecords.value = res.data?.records || []
  } finally {
    readLoading.value = false
  }
}

function importanceLabel(v?: number) {
  return importanceOptions.find((o) => o.value === v)?.label || '-'
}

function statusLabel(v?: number) {
  return statusOptions.find((o) => o.value === v)?.label || '-'
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
        <el-form-item label="标题">
          <el-input v-model="query.title" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
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
        <el-button type="primary" @click="openDialog()">发布通知</el-button>
      </div>
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column label="分类" width="110">
          <template #default="{ row }">{{ categoryMap[row.categoryId] || '-' }}</template>
        </el-table-column>
        <el-table-column label="重要程度" width="90">
          <template #default="{ row }">{{ importanceLabel(row.importance) }}</template>
        </el-table-column>
        <el-table-column label="紧急程度" width="90">
          <template #default="{ row }">
            {{ urgencyOptions.find((o) => o.value === row.urgency)?.label || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'info' : 'warning'">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="primary" @click="openReadRecords(row)">已读记录</el-button>
            <el-button link type="danger" @click="handleDelete(row)">撤回</el-button>
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
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="通知标题" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择" style="width: 100%">
            <el-option
              v-for="c in categories"
              :key="c.id"
              :label="c.categoryName"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="通知内容" />
        </el-form-item>
        <el-form-item label="重要程度">
          <el-radio-group v-model="form.importance">
            <el-radio
              v-for="o in importanceOptions"
              :key="o.value"
              :value="o.value"
            >
              {{ o.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="紧急程度">
          <el-radio-group v-model="form.urgency">
            <el-radio v-for="o in urgencyOptions" :key="o.value" :value="o.value">
              {{ o.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="接收范围">
          <el-select v-model="form.receiverType" style="width: 100%">
            <el-option
              v-for="o in receiverTypeOptions"
              :key="o.value"
              :label="o.label"
              :value="o.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="form.receiverType && form.receiverType > 1"
          label="接收对象"
        >
          <el-input
            v-model="form.receiverValues"
            placeholder="ID列表，逗号分隔"
          />
        </el-form-item>
        <el-form-item label="需确认阅读">
          <el-switch v-model="form.needConfirm" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker
            v-model="form.expireTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="可选"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="readDialogVisible"
      :title="`已读记录 - ${currentNotice?.title}`"
      width="700px"
      destroy-on-close
    >
      <el-table v-loading="readLoading" :data="readRecords" border stripe>
        <el-table-column label="用户" width="120">
          <template #default="{ row }">{{ userMap[row.userId] || row.userId }}</template>
        </el-table-column>
        <el-table-column label="阅读时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.readTime) }}</template>
        </el-table-column>
        <el-table-column label="确认时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.confirmTime) }}</template>
        </el-table-column>
        <el-table-column label="已确认" width="90">
          <template #default="{ row }">
            <el-tag :type="row.isConfirmed === 1 ? 'success' : 'info'">
              {{ row.isConfirmed === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
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
