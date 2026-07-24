<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { sysUserApi } from '@/api/sysUser'
import { sysUserRoleApi, type SysUserRoleItem } from '@/api/sysUserRole'
import { sysRoleApi, type SysRole } from '@/api/sysRole'
import { sysClubApi } from '@/api/sysClub'
import { sysCollegeApi } from '@/api/sysCollege'
import { formatDateTime } from '@/utils/format'
import type { SysClubItem, SysCollege, SysDepartment, SysUser } from '@/types/generated'

/** 与后端 UserRoleScopeHelper / SysRole.dataScope 一致：0全部 1本学院 2本社团 3本部门 4仅自己 */
const DATA_SCOPE_ALL = 0
const DATA_SCOPE_COLLEGE = 1
const DATA_SCOPE_CLUB = 2
const DATA_SCOPE_DEPARTMENT = 3
const DATA_SCOPE_SELF = 4

const DATA_SCOPE_LABEL: Record<number, string> = {
  [DATA_SCOPE_ALL]: '全部',
  [DATA_SCOPE_COLLEGE]: '本学院',
  [DATA_SCOPE_CLUB]: '本社团',
  [DATA_SCOPE_DEPARTMENT]: '本部门',
  [DATA_SCOPE_SELF]: '仅自己',
}

const BASE_ROLE_CODES = ['SUPER_ADMIN', 'MEMBER', 'ADVISOR', 'ADMIN', 'CLUB_PRESIDENT', 'CLUB_MINISTER']

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

const clubs = ref<SysClubItem[]>([])
const colleges = ref<SysCollege[]>([])
const departments = ref<SysDepartment[]>([])
const roles = ref<SysRole[]>([])
const roleMap = ref<Record<number, string>>({})

const roleDialogVisible = ref(false)
const currentUser = ref<SysUser | null>(null)
const userRoles = ref<SysUserRoleItem[]>([])
const roleLoading = ref(false)

const addRoleFormRef = ref<FormInstance>()
const addRoleForm = reactive({
  roleId: undefined as number | undefined,
  /** 学院/社团/部门最终 scopeId */
  scopeId: undefined as number | undefined,
  /** 部长分配时先选社团再选部门 */
  scopeClubId: undefined as number | undefined,
})

const selectedRole = computed(() => roles.value.find((r) => r.id === addRoleForm.roleId))
const selectedDataScope = computed(() => selectedRole.value?.dataScope)
const needsCollege = computed(() => selectedDataScope.value === DATA_SCOPE_COLLEGE)
const needsClub = computed(() => selectedDataScope.value === DATA_SCOPE_CLUB)
const needsDepartment = computed(() => selectedDataScope.value === DATA_SCOPE_DEPARTMENT)
const isGlobalScope = computed(
  () => selectedDataScope.value === DATA_SCOPE_ALL || selectedDataScope.value === DATA_SCOPE_SELF,
)

watch(
  () => addRoleForm.roleId,
  () => {
    addRoleForm.scopeId = undefined
    addRoleForm.scopeClubId = undefined
    departments.value = []
  },
)

watch(
  () => addRoleForm.scopeClubId,
  async (clubId) => {
    addRoleForm.scopeId = undefined
    departments.value = []
    if (!clubId || !needsDepartment.value) return
    const club = clubs.value.find((c) => c.id === clubId)
    if (!club?.clubCode) return
    const res = await sysClubApi.departments(club.clubCode)
    departments.value = res.data || []
  },
)

async function loadDicts() {
  const [clubRes, collegeRes, roleRes] = await Promise.all([
    sysClubApi.list({ page: 1, limit: 200, tabMode: 'normal' }),
    sysCollegeApi.list({}),
    sysRoleApi.list({ page: 1, limit: 100 }),
  ])
  clubs.value = (clubRes.data?.records || []).filter((c) => c.id != null)
  colleges.value = Array.isArray(collegeRes.data) ? collegeRes.data : []
  // 仅展示基础角色模板，不展示 ADVISOR_类别_缩写 等具体常量角色（若已 seed）
  roles.value = (roleRes.data?.records || []).filter(
    (r) => r.status !== 0 && BASE_ROLE_CODES.includes((r.roleCode || '').toUpperCase()),
  )
  roleMap.value = Object.fromEntries(roles.value.map((r) => [r.id!, r.roleName || '']))
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
    addRoleForm.scopeId = undefined
    addRoleForm.scopeClubId = undefined
    departments.value = []
  } finally {
    roleLoading.value = false
  }
}

async function handleAddRole() {
  if (!currentUser.value?.username || !addRoleForm.roleId) {
    ElMessage.warning('请选择角色')
    return
  }
  const role = selectedRole.value
  if (!role || role.dataScope == null) {
    ElMessage.warning('角色未配置数据范围')
    return
  }

  const ds = role.dataScope
  let scopeType: number | undefined
  let scopeId: number | undefined

  if (ds === DATA_SCOPE_ALL || ds === DATA_SCOPE_SELF) {
    scopeType = undefined
    scopeId = undefined
  } else if (ds === DATA_SCOPE_COLLEGE) {
    if (!addRoleForm.scopeId) {
      ElMessage.warning('请选择所属学院')
      return
    }
    scopeType = DATA_SCOPE_COLLEGE
    scopeId = addRoleForm.scopeId
  } else if (ds === DATA_SCOPE_CLUB) {
    if (!addRoleForm.scopeId) {
      ElMessage.warning('请选择所属社团')
      return
    }
    scopeType = DATA_SCOPE_CLUB
    scopeId = addRoleForm.scopeId
  } else if (ds === DATA_SCOPE_DEPARTMENT) {
    if (!addRoleForm.scopeId) {
      ElMessage.warning('请选择所属部门')
      return
    }
    scopeType = DATA_SCOPE_DEPARTMENT
    scopeId = addRoleForm.scopeId
  } else {
    ElMessage.warning('角色数据范围配置无效')
    return
  }

  await sysUserRoleApi.assign({
    username: currentUser.value.username,
    roleId: addRoleForm.roleId,
    scopeType,
    scopeId,
  })
  ElMessage.success('角色分配成功')
  const res = await sysUserRoleApi.rolesByUsername(currentUser.value.username)
  userRoles.value = res.data || []
  addRoleForm.roleId = undefined
  addRoleForm.scopeId = undefined
  addRoleForm.scopeClubId = undefined
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
  if (row.scopeType == null && row.scopeId == null) return '全部 / 仅自己'
  if (row.scopeType === DATA_SCOPE_COLLEGE) {
    const college = colleges.value.find((c) => c.id === row.scopeId)
    return college ? `学院: ${college.collegeName}` : `学院ID: ${row.scopeId}`
  }
  if (row.scopeType === DATA_SCOPE_CLUB) {
    const club = clubs.value.find((c) => c.id === row.scopeId)
    return club ? `社团: ${club.clubName}` : `社团ID: ${row.scopeId}`
  }
  if (row.scopeType === DATA_SCOPE_DEPARTMENT) {
    return `部门ID: ${row.scopeId}`
  }
  return row.scopeId ? `范围: ${row.scopeId}` : '—'
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
      width="680px"
      destroy-on-close
    >
      <div v-loading="roleLoading">
        <el-table :data="userRoles" border size="small" class="role-table">
          <el-table-column label="角色" width="140">
            <template #default="{ row }">{{ roleMap[row.roleId!] || row.roleName || row.roleId }}</template>
          </el-table-column>
          <el-table-column label="数据范围" min-width="180">
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
            <el-select v-model="addRoleForm.roleId" placeholder="选择角色" style="width: 160px" clearable>
              <el-option
                v-for="r in roles"
                :key="r.id!"
                :label="r.roleName"
                :value="r.id!"
              />
            </el-select>
          </el-form-item>

          <el-form-item v-if="selectedRole" label="数据范围">
            <el-tag type="info">{{ DATA_SCOPE_LABEL[selectedDataScope!] || '未配置' }}</el-tag>
          </el-form-item>

          <el-form-item v-if="isGlobalScope">
            <span class="scope-hint">无需指定学院/社团，scope 为空</span>
          </el-form-item>

          <el-form-item v-if="needsCollege" label="所属学院">
            <el-select
              v-model="addRoleForm.scopeId"
              placeholder="选择学院"
              filterable
              style="width: 200px"
            >
              <el-option
                v-for="c in colleges"
                :key="c.id!"
                :label="c.collegeName"
                :value="c.id!"
              />
            </el-select>
          </el-form-item>

          <el-form-item v-if="needsClub" label="所属社团">
            <el-select
              v-model="addRoleForm.scopeId"
              placeholder="选择社团"
              filterable
              style="width: 200px"
            >
              <el-option
                v-for="c in clubs"
                :key="c.id!"
                :label="c.clubName"
                :value="c.id!"
              />
            </el-select>
          </el-form-item>

          <template v-if="needsDepartment">
            <el-form-item label="所属社团">
              <el-select
                v-model="addRoleForm.scopeClubId"
                placeholder="先选社团"
                filterable
                style="width: 180px"
              >
                <el-option
                  v-for="c in clubs"
                  :key="c.id!"
                  :label="c.clubName"
                  :value="c.id!"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="所属部门">
              <el-select
                v-model="addRoleForm.scopeId"
                placeholder="选择部门"
                filterable
                :disabled="!addRoleForm.scopeClubId"
                style="width: 180px"
              >
                <el-option
                  v-for="d in departments"
                  :key="d.id!"
                  :label="d.deptName"
                  :value="d.id!"
                />
              </el-select>
            </el-form-item>
          </template>

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

.scope-hint {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
