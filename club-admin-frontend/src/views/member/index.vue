<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { sysUserApi } from '@/api/sysUser'
import { sysUserRoleApi, type SysUserRoleItem } from '@/api/sysUserRole'
import { sysRoleApi, type SysRole } from '@/api/sysRole'
import { portalApi } from '@/api/portal'
import { useUserStore } from '@/stores/user'
import { formatDateTime } from '@/utils/format'
import type { PortalClub, SysUser } from '@/types/generated'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref<SysUser[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  limit: 10,
  username: '',
  realName: '',
  status: undefined as number | undefined,
})

const genderMap: Record<number, string> = { 1: '男', 2: '女', 0: '未知' }
const userTypeMap: Record<number, string> = { 1: '学生', 2: '教师', 3: '管理员' }

const clubs = ref<PortalClub[]>([])
const roles = ref<SysRole[]>([])
const roleMap = ref<Record<number, string>>({})

const roleDialogVisible = ref(false)
const currentUser = ref<SysUser | null>(null)
const userRoles = ref<SysUserRoleItem[]>([])
const roleLoading = ref(false)

const addRoleFormRef = ref<FormInstance>()
const addRoleForm = reactive({
  roleId: undefined as number | undefined,
  scopeType: 2,
  scopeId: undefined as number | undefined,
})

async function loadDicts() {
  const [clubRes, roleRes] = await Promise.all([
    portalApi.clubs({}),
    sysRoleApi.list({ page: 1, limit: 100 }),
  ])
  const clubData = clubRes.data
  clubs.value = Array.isArray(clubData) ? clubData : clubData?.records || []
  roles.value = (roleRes.data?.records || []).filter((r) => r.status !== 0)
  roleMap.value = Object.fromEntries(roles.value.map((r) => [r.id!, r.roleName || '']))
  if (!addRoleForm.scopeId && userStore.clubScopeIds.length) {
    addRoleForm.scopeId = userStore.clubScopeIds[0]
  }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await sysUserApi.query({
      page: query.page,
      limit: query.limit,
      username: query.username || undefined,
      realName: query.realName || undefined,
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
  query.username = ''
  query.realName = ''
  query.status = undefined
  query.page = 1
  fetchList()
}

async function openRoleDialog(row: SysUser) {
  currentUser.value = row
  roleDialogVisible.value = true
  roleLoading.value = true
  try {
    if (!row.username) return
    const res = await sysUserRoleApi.rolesByUsername(row.username)
    userRoles.value = res.data || []
    addRoleForm.roleId = undefined
    addRoleForm.scopeType = 2
    addRoleForm.scopeId = userStore.clubScopeIds[0] || clubs.value[0]?.id
  } finally {
    roleLoading.value = false
  }
}

async function handleAddRole() {
  if (!currentUser.value?.username || !addRoleForm.roleId) {
    ElMessage.warning('请选择角色')
    return
  }
  if (addRoleForm.scopeType === 2 && !addRoleForm.scopeId) {
    ElMessage.warning('请选择所属社团')
    return
  }
  await sysUserRoleApi.assign({
    username: currentUser.value.username,
    roleId: addRoleForm.roleId,
    scopeType: addRoleForm.scopeType,
    scopeId: addRoleForm.scopeId,
  })
  ElMessage.success('角色分配成功')
  const res = await sysUserRoleApi.rolesByUsername(currentUser.value.username)
  userRoles.value = res.data || []
  addRoleForm.roleId = undefined
}

async function handleRemoveRole(row: SysUserRoleItem) {
  await ElMessageBox.confirm('确定移除该角色吗？', '提示', { type: 'warning' })
  await sysUserRoleApi.revoke(row.id!)
  ElMessage.success('已移除')
  if (currentUser.value?.username) {
    const res = await sysUserRoleApi.rolesByUsername(currentUser.value.username)
    userRoles.value = res.data || []
  }
}

function scopeLabel(row: SysUserRoleItem): string {
  if (row.scopeType === 2) {
    const club = clubs.value.find((c) => c.id === row.scopeId)
    return club ? `社团: ${club.clubName}` : `社团ID: ${row.scopeId}`
  }
  if (row.scopeType === 1) return '全局'
  return row.scopeId ? `范围: ${row.scopeId}` : '全局'
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
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="query.realName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column label="性别" width="70">
          <template #default="{ row }">{{ genderMap[row.gender ?? 0] }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="手机" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column label="用户类型" width="90">
          <template #default="{ row }">{{ userTypeMap[row.userType!] || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openRoleDialog(row)">角色分配</el-button>
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

    <el-dialog
      v-model="roleDialogVisible"
      :title="`角色分配 - ${currentUser?.realName || currentUser?.username}`"
      width="620px"
      destroy-on-close
    >
      <div v-loading="roleLoading">
        <el-table :data="userRoles" border size="small" class="role-table">
          <el-table-column label="角色" width="140">
            <template #default="{ row }">{{ roleMap[row.roleId!] || row.roleName || row.roleId }}</template>
          </el-table-column>
          <el-table-column label="数据范围" min-width="160">
            <template #default="{ row }">{{ scopeLabel(row) }}</template>
          </el-table-column>
          <el-table-column label="分配时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button link type="danger" @click="handleRemoveRole(row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-divider>新增角色</el-divider>
        <el-form ref="addRoleFormRef" :model="addRoleForm" :inline="true">
          <el-form-item label="角色">
            <el-select v-model="addRoleForm.roleId" placeholder="选择角色" style="width: 160px">
              <el-option
                v-for="r in roles"
                :key="r.id!"
                :label="r.roleName"
                :value="r.id!"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="范围类型">
            <el-select v-model="addRoleForm.scopeType" style="width: 120px">
              <el-option label="全局" :value="1" />
              <el-option label="社团" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="addRoleForm.scopeType === 2" label="所属社团">
            <el-select v-model="addRoleForm.scopeId" placeholder="选择社团" style="width: 180px">
              <el-option
                v-for="c in clubs"
                :key="c.id!"
                :label="c.clubName"
                :value="c.id!"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleAddRole">添加</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.role-table {
  margin-bottom: 8px;
}
</style>
