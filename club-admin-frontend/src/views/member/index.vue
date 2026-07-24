<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { sysUserApi } from '@/api/sysUser'
import { sysUserRoleApi, type SysUserRoleItem } from '@/api/sysUserRole'
import { sysRoleApi, type SysRole } from '@/api/sysRole'
import { sysClubApi } from '@/api/sysClub'
import { sysCollegeApi } from '@/api/sysCollege'
import { portalApi } from '@/api/portal'
import { useUserStore } from '@/stores/user'
import { LEVEL } from '@/utils/level'
import { formatDateTime } from '@/utils/format'
import type { SysClubItem, SysCollege, SysDepartment, SysUser } from '@/types/generated'

/** 与后端 UserRoleScopeHelper 一致：0全部 1本学院 2本社团 3本部门 4仅自己 */
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

/** role_code → 默认 data_scope */
const ROLE_CODE_DATA_SCOPE: Record<string, number> = {
  SUPER_ADMIN: DATA_SCOPE_ALL,
  ADMIN: DATA_SCOPE_COLLEGE,
  ADVISOR: DATA_SCOPE_CLUB,
  CLUB_PRESIDENT: DATA_SCOPE_CLUB,
  CLUB_MINISTER: DATA_SCOPE_DEPARTMENT,
  MEMBER: DATA_SCOPE_SELF,
}

/** role_code → 允许分配的 userType（1=学生 2=教师），null=无限制 */
const ROLE_USER_TYPE_RESTRICTION: Record<string, number | null> = {
  SUPER_ADMIN: 2,
  ADMIN: 2,
  ADVISOR: 2,
  CLUB_PRESIDENT: 1,
  CLUB_MINISTER: 1,
  MEMBER: null,
}

const userStore = useUserStore()

/** 管理员及以上可管理用户（新增/删除） */
const canManageUsers = computed(() => userStore.effectiveLevel <= LEVEL.ADMIN)

/** 管理员及以上可分配角色（Level 4 学生不可见；社长/部长也不开放分配入口） */
const canManageRoles = computed(() => userStore.effectiveLevel <= LEVEL.ADMIN)

/** 创建时间仅 Level 0/1 可见 */
const canViewCreateTime = computed(() => userStore.effectiveLevel <= LEVEL.ADMIN)

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
  scopeId: undefined as number | undefined,
  scopeClubId: undefined as number | undefined,
})

// —— 新增用户 ——
const createUserVisible = ref(false)
const createUserFormRef = ref<FormInstance>()
const createUserSubmitting = ref(false)
const createUserForm = reactive({
  username: '',
  realName: '',
  password: '',
  userType: 1 as number,
  gender: 1 as number,
  phone: '',
  email: '',
})

function resolveDataScope(role?: SysRole | null): number | undefined {
  if (!role) return undefined
  const code = (role.roleCode || '').toUpperCase().trim()
  if (code && ROLE_CODE_DATA_SCOPE[code] != null) {
    return ROLE_CODE_DATA_SCOPE[code]
  }
  const name = role.roleName || ''
  if (name.includes('超级') || name.includes('超管')) return DATA_SCOPE_ALL
  if (name.includes('指导')) return DATA_SCOPE_CLUB
  if (name.includes('社长')) return DATA_SCOPE_CLUB
  if (name.includes('部长')) return DATA_SCOPE_DEPARTMENT
  if (name.includes('学院') || name.includes('管理员')) return DATA_SCOPE_COLLEGE
  if (name.includes('成员') || name.includes('学生')) return DATA_SCOPE_SELF
  if (typeof role.dataScope === 'number' && role.dataScope >= 0 && role.dataScope <= 4) {
    return role.dataScope
  }
  return undefined
}

const selectedRole = computed(() =>
  roles.value.find((r) => Number(r.id) === Number(addRoleForm.roleId)),
)
const selectedDataScope = computed(() => resolveDataScope(selectedRole.value))
const needsCollege = computed(() => selectedDataScope.value === DATA_SCOPE_COLLEGE)
const needsClub = computed(() => selectedDataScope.value === DATA_SCOPE_CLUB)
const needsDepartment = computed(() => selectedDataScope.value === DATA_SCOPE_DEPARTMENT)
const isGlobalScope = computed(
  () => selectedDataScope.value === DATA_SCOPE_ALL || selectedDataScope.value === DATA_SCOPE_SELF,
)
const dataScopeLabel = computed(() => {
  const ds = selectedDataScope.value
  return ds == null ? '未配置' : DATA_SCOPE_LABEL[ds] || '未配置'
})

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
    const club = clubs.value.find((c) => Number(c.id) === Number(clubId))
    if (!club?.clubCode) return
    try {
      const res = await sysClubApi.departments(club.clubCode)
      departments.value = res.data || []
    } catch {
      departments.value = []
    }
  },
)

async function loadClubs() {
  try {
    const clubRes = await sysClubApi.list({ page: 1, limit: 200, tabMode: 'normal' })
    const list = (clubRes.data?.records || []).filter((c) => c.id != null)
    if (list.length) {
      clubs.value = list
      return
    }
  } catch {
    // 管理端列表不可用时回退门户
  }
  try {
    const portalRes = await portalApi.clubs({})
    const raw = portalRes.data
    const list = Array.isArray(raw) ? raw : raw?.records || []
    clubs.value = list
      .filter((c) => c.id != null)
      .map((c) => ({ id: c.id, clubName: c.clubName, category: c.category }))
  } catch {
    clubs.value = []
  }
}

async function loadDicts() {
  const [, collegeRes, roleRes] = await Promise.all([
    loadClubs(),
    sysCollegeApi.list({}).catch(() => ({ data: [] as SysCollege[] })),
    sysRoleApi.list({ page: 1, limit: 100 }),
  ])
  colleges.value = Array.isArray(collegeRes.data) ? collegeRes.data : []
  // 列表不过滤死 role_code：名称可能已中文化，只要启用即可
  roles.value = (roleRes.data?.records || []).filter((r) => r.status !== 0)
  // 规范化 dataScope，保证下拉后能识别范围
  roles.value = roles.value.map((r) => ({
    ...r,
    dataScope: resolveDataScope(r) ?? r.dataScope,
  }))
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
  if (!currentUser.value?.username || addRoleForm.roleId == null) {
    ElMessage.warning('请选择角色')
    return
  }
  const role = selectedRole.value
  if (!role) {
    ElMessage.warning('角色不存在或未加载')
    return
  }
  const code = (role.roleCode || '').toUpperCase()
  const name = role.roleName || ''

  // 检查被分配人的身份类型是否匹配角色要求
  const targetUserType = currentUser.value.userType
  let requiredUserType = ROLE_USER_TYPE_RESTRICTION[code]
  if (requiredUserType === undefined) {
    if (name.includes('社长') || name.includes('部长')) requiredUserType = 1
    else if (name.includes('指导') || name.includes('管理员')) requiredUserType = 2
    else requiredUserType = null
  }
  if (requiredUserType != null && targetUserType !== requiredUserType) {
    const requiredLabel = requiredUserType === 1 ? '学生' : '教师'
    const actualLabel = targetUserType === 1 ? '学生' : targetUserType === 2 ? '教师' : '其他'
    ElMessage.warning(`「${role.roleName}」角色只能分配给${requiredLabel}，当前用户身份为${actualLabel}`)
    return
  }

  const ds = resolveDataScope(role)
  if (ds == null) {
    ElMessage.warning(`无法解析数据范围（roleCode=${role.roleCode || '空'}, name=${role.roleName || ''}）`)
    return
  }

  const payload: {
    username: string
    roleId: number
    scopeType?: number | null
    scopeId?: number | null
  } = {
    username: currentUser.value.username,
    roleId: Number(addRoleForm.roleId),
  }

  if (ds === DATA_SCOPE_ALL || ds === DATA_SCOPE_SELF) {
    payload.scopeType = null
    payload.scopeId = null
  } else if (ds === DATA_SCOPE_COLLEGE) {
    if (addRoleForm.scopeId == null) {
      ElMessage.warning('请选择所属学院')
      return
    }
    payload.scopeType = DATA_SCOPE_COLLEGE
    payload.scopeId = Number(addRoleForm.scopeId)
  } else if (ds === DATA_SCOPE_CLUB) {
    if (addRoleForm.scopeId == null) {
      ElMessage.warning('请选择所属社团')
      return
    }
    payload.scopeType = DATA_SCOPE_CLUB
    payload.scopeId = Number(addRoleForm.scopeId)
  } else if (ds === DATA_SCOPE_DEPARTMENT) {
    if (addRoleForm.scopeId == null) {
      ElMessage.warning('请选择所属部门')
      return
    }
    payload.scopeType = DATA_SCOPE_DEPARTMENT
    payload.scopeId = Number(addRoleForm.scopeId)
  } else {
    ElMessage.warning('角色数据范围配置无效')
    return
  }

  await sysUserRoleApi.assign(payload)
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

// —— 新增用户 ——
function openCreateUser() {
  createUserForm.username = ''
  createUserForm.realName = ''
  createUserForm.password = ''
  createUserForm.userType = 1
  createUserForm.gender = 1
  createUserForm.phone = ''
  createUserForm.email = ''
  createUserVisible.value = true
}

async function submitCreateUser() {
  if (!createUserForm.username || !createUserForm.realName || !createUserForm.password) {
    ElMessage.warning('请填写用户名、真实姓名和密码')
    return
  }
  createUserSubmitting.value = true
  try {
    await sysUserApi.add({
      username: createUserForm.username.trim(),
      realName: createUserForm.realName.trim(),
      password: createUserForm.password,
      userType: createUserForm.userType,
      gender: createUserForm.gender,
      phone: createUserForm.phone || undefined,
      email: createUserForm.email || undefined,
    })
    ElMessage.success('用户创建成功')
    createUserVisible.value = false
    await fetchList()
  } finally {
    createUserSubmitting.value = false
  }
}

// —— 删除用户 ——
async function handleDeleteUser(row: SysUser) {
  if (!row.username) return
  try {
    const { value } = await ElMessageBox.prompt(
      `请输入用户名「${row.username}」以确认删除`,
      '删除用户确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        inputPattern: new RegExp(`^${escapeRegExp(row.username)}$`),
        inputErrorMessage: '用户名不匹配',
        type: 'error',
        inputPlaceholder: '请输入用户名',
      },
    )
    if (value !== row.username) return
    await sysUserApi.delete(row.username)
    ElMessage.success('用户已删除')
    await fetchList()
  } catch (e: unknown) {
    if (e === 'cancel' || e === 'close') return
  }
}

function escapeRegExp(s: string) {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

function scopeLabel(row: SysUserRoleItem): string {
  if (row.scopeType == null && row.scopeId == null) return '全部 / 仅自己'
  if (row.scopeType === DATA_SCOPE_COLLEGE) {
    const college = colleges.value.find((c) => Number(c.id) === Number(row.scopeId))
    return college ? `学院: ${college.collegeName}` : `学院ID: ${row.scopeId}`
  }
  if (row.scopeType === DATA_SCOPE_CLUB) {
    const club = clubs.value.find((c) => Number(c.id) === Number(row.scopeId))
    return club ? `社团: ${club.clubName}` : `社团ID: ${row.scopeId}`
  }
  if (row.scopeType === DATA_SCOPE_DEPARTMENT) {
    return `部门ID: ${row.scopeId}`
  }
  return row.scopeId != null ? `范围: ${row.scopeId}` : '—'
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
          <el-button v-if="canManageUsers" type="success" @click="openCreateUser">新增用户</el-button>
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
        <el-table-column v-if="canViewCreateTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column v-if="canManageRoles || canManageUsers" label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canManageRoles" link type="primary" @click="openRoleDialog(row)">角色分配</el-button>
            <el-button v-if="canManageUsers" link type="danger" @click="handleDeleteUser(row)">删除</el-button>
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
      width="720px"
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
        <el-form ref="addRoleFormRef" :model="addRoleForm" :inline="true" class="add-role-form">
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
            <el-tag type="info">{{ dataScopeLabel }}</el-tag>
          </el-form-item>

          <el-form-item v-if="isGlobalScope">
            <span class="scope-hint">无需指定学院/社团</span>
          </el-form-item>

          <el-form-item v-if="needsCollege" label="所属学院">
            <el-select
              v-model="addRoleForm.scopeId"
              placeholder="选择学院"
              filterable
              clearable
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
              clearable
              style="width: 200px"
            >
              <el-option
                v-for="c in clubs"
                :key="c.id!"
                :label="c.clubName || c.clubCode"
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
                clearable
                style="width: 180px"
              >
                <el-option
                  v-for="c in clubs"
                  :key="c.id!"
                  :label="c.clubName || c.clubCode"
                  :value="c.id!"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="所属部门">
              <el-select
                v-model="addRoleForm.scopeId"
                placeholder="选择部门"
                filterable
                clearable
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

        <p v-if="needsClub && clubs.length === 0" class="scope-warn">
          暂无可用社团，请确认账号有社团管理权限且存在正常社团。
        </p>
      </div>
    </el-dialog>

    <!-- 新增用户对话框 -->
    <el-dialog v-model="createUserVisible" title="新增用户" width="500px" destroy-on-close>
      <el-form ref="createUserFormRef" :model="createUserForm" label-width="100px">
        <el-form-item label="用户名" required>
          <el-input v-model="createUserForm.username" maxlength="30" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="真实姓名" required>
          <el-input v-model="createUserForm.realName" maxlength="20" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input v-model="createUserForm.password" type="password" show-password maxlength="30" />
        </el-form-item>
        <el-form-item label="身份类型" required>
          <el-select v-model="createUserForm.userType" style="width: 100%">
            <el-option label="学生" :value="1" />
            <el-option label="教师" :value="2" />
            <el-option label="管理员" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="createUserForm.gender" style="width: 100%">
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
            <el-option label="未知" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="createUserForm.phone" maxlength="20" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="createUserForm.email" maxlength="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createUserVisible = false">取消</el-button>
        <el-button type="primary" :loading="createUserSubmitting" @click="submitCreateUser">提交</el-button>
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

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.role-table {
  margin-bottom: 8px;
}

.add-role-form {
  flex-wrap: wrap;
}

.scope-hint {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.scope-warn {
  margin: 0;
  color: var(--el-color-warning);
  font-size: 13px;
}
</style>
