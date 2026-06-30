<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { sysClubApi } from '@/api/sysClub'
import { sysCollegeApi } from '@/api/sysCollege'
import { sysUserApi } from '@/api/sysUser'
import { formatDateTime } from '@/utils/format'
import type { SysClub, SysCollege, SysUser } from '@/types/generated'

const loading = ref(false)
const tableData = ref<SysClub[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  limit: 10,
  clubName: '',
  status: undefined as number | undefined,
})

const collegeMap = ref<Record<number, string>>({})
const advisorMap = ref<Record<number, string>>({})

const categoryOptions = [
  { label: '学术科技', value: '学术科技' },
  { label: '文化体育', value: '文化体育' },
  { label: '志愿服务', value: '志愿服务' },
  { label: '创新创业', value: '创新创业' },
  { label: '其他', value: '其他' },
]

const statusOptions = [
  { label: '正常', value: 1 },
  { label: '已解散', value: 0 },
]

const dialogVisible = ref(false)
const dialogTitle = ref('新增社团')
const formRef = ref<FormInstance>()
const form = reactive<Partial<SysClub>>({
  id: undefined,
  clubName: '',
  clubCode: '',
  category: '',
  collegeId: undefined,
  advisorId: undefined,
  description: '',
  logo: '',
  status: 1,
})

const rules: FormRules = {
  clubName: [{ required: true, message: '请输入社团名称', trigger: 'blur' }],
  clubCode: [{ required: true, message: '请输入社团编码', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  collegeId: [{ required: true, message: '请选择所属学院', trigger: 'change' }],
}

const colleges = ref<SysCollege[]>([])
const advisors = ref<SysUser[]>([])

async function loadDicts() {
  const [collegeRes, userRes] = await Promise.all([
    sysCollegeApi.list(),
    sysUserApi.list(),
  ])
  colleges.value = collegeRes.data || []
  advisors.value = (userRes.data || []).filter(
    (u) => u.userType === 2 || u.teacherNo,
  )
  collegeMap.value = Object.fromEntries(
    colleges.value.map((c) => [c.id!, c.collegeName || '']),
  )
  advisorMap.value = Object.fromEntries(
    advisors.value.map((u) => [u.id!, u.realName || u.username || '']),
  )
}

async function fetchList() {
  loading.value = true
  try {
    const res = await sysClubApi.listF({
      page: query.page,
      limit: query.limit,
      clubName: query.clubName || undefined,
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
  query.clubName = ''
  query.status = undefined
  query.page = 1
  fetchList()
}

function openDialog(row?: SysClub) {
  if (row) {
    dialogTitle.value = '编辑社团'
    Object.assign(form, { ...row })
  } else {
    dialogTitle.value = '新增社团'
    Object.assign(form, {
      id: undefined,
      clubName: '',
      clubCode: '',
      category: '',
      collegeId: undefined,
      advisorId: undefined,
      description: '',
      logo: '',
      status: 1,
    })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.id) {
    await sysClubApi.updateF(form)
    ElMessage.success('更新成功')
  } else {
    await sysClubApi.addF(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row: SysClub) {
  await ElMessageBox.confirm(`确定解散社团「${row.clubName}」吗？`, '提示', {
    type: 'warning',
  })
  await sysClubApi.deleteF(row.id!)
  ElMessage.success('已解散')
  fetchList()
}

onMounted(async () => {
  await loadDicts()
  await fetchList()
})
</script>

<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="社团名称">
          <el-input v-model="query.clubName" placeholder="请输入" clearable />
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

    <el-card shadow="never" class="table-card">
      <div class="toolbar">
        <el-button type="primary" @click="openDialog()">新增社团</el-button>
      </div>
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="clubName" label="社团名称" min-width="140" />
        <el-table-column prop="clubCode" label="社团编码" width="120" />
        <el-table-column prop="category" label="分类" width="110" />
        <el-table-column label="所属学院" width="140">
          <template #default="{ row }">
            {{ collegeMap[row.collegeId] || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="指导老师" width="120">
          <template #default="{ row }">
            {{ advisorMap[row.advisorId] || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '正常' : '已解散' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button
              v-if="row.status === 1"
              link
              type="danger"
              @click="handleDelete(row)"
            >
              解散
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="社团名称" prop="clubName">
          <el-input v-model="form.clubName" placeholder="请输入社团名称" />
        </el-form-item>
        <el-form-item label="社团编码" prop="clubCode">
          <el-input v-model="form.clubCode" placeholder="请输入社团编码" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择" style="width: 100%">
            <el-option
              v-for="item in categoryOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属学院" prop="collegeId">
          <el-select v-model="form.collegeId" placeholder="请选择" style="width: 100%">
            <el-option
              v-for="c in colleges"
              :key="c.id"
              :label="c.collegeName"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="指导老师">
          <el-select
            v-model="form.advisorId"
            placeholder="请选择"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="u in advisors"
              :key="u.id"
              :label="u.realName || u.username"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="Logo">
          <el-input v-model="form.logo" placeholder="Logo URL（可选）" />
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
