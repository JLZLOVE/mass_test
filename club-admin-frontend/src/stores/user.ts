import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi } from '@/api/login'
import { sysUserApi } from '@/api/sysUser'
import { sysUserRoleApi } from '@/api/sysUserRole'
import { canViewBudget } from '@/utils/permission'
import {
  levelLabel,
  resolveClubScopes,
  resolveEffectiveLevel,
  resolvePrimaryDepartmentId,
  resolvePrimaryRoleName,
} from '@/utils/level'
import type { SysUser, SysUserRole } from '@/types/generated'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const userInfo = ref<SysUser | null>(null)
  const roles = ref<SysUserRole[]>([])
  const roleIds = ref<number[]>([])
  const clubScopeIds = ref<number[]>([])
  const effectiveLevel = ref(Number(localStorage.getItem('effectiveLevel') ?? 4))
  const primaryClubId = ref<number | null>(
    localStorage.getItem('primaryClubId') ? Number(localStorage.getItem('primaryClubId')) : null,
  )
  const primaryDepartmentId = ref<number | null>(null)
  const unreadCount = ref(0)

  const isLoggedIn = computed(() => !!token.value)
  const showBudget = computed(() => canViewBudget(roleIds.value, effectiveLevel.value))
  const displayName = computed(
    () => userInfo.value?.realName || username.value || '未命名用户',
  )
  const roleName = computed(() =>
    resolvePrimaryRoleName(roles.value, effectiveLevel.value),
  )
  const levelText = computed(() => levelLabel(effectiveLevel.value))
  const hasGoldAvatarRing = computed(
    () => effectiveLevel.value === 0 || effectiveLevel.value === 2,
  )
  const canUseFab = computed(() => effectiveLevel.value <= 2)
  const canPublishNotice = computed(() => effectiveLevel.value <= 1)

  async function login(name: string, password: string) {
    const res = await loginApi(name, password)
    token.value = (res.token as string) || ''
    username.value = (res.username as string) || name
    localStorage.setItem('token', token.value)
    localStorage.setItem('username', username.value)
    await loadUserProfile()
  }

  async function loadUserProfile() {
    try {
      const detailRes = await sysUserApi.detail(username.value)
      userInfo.value = detailRes.data || null
    } catch {
      const usersRes = await sysUserApi.query({ username: username.value, page: 1, limit: 1 })
      userInfo.value = usersRes.data?.records?.[0] || null
    }

    const rolesRes = await sysUserRoleApi.myRoles()
    roles.value = rolesRes.data || []
    roleIds.value = roles.value.map((r) => r.roleId!).filter(Boolean)
    clubScopeIds.value = resolveClubScopes(roles.value)
    effectiveLevel.value = resolveEffectiveLevel(roles.value)
    primaryClubId.value = clubScopeIds.value[0] ?? null
    primaryDepartmentId.value = resolvePrimaryDepartmentId(roles.value)

    localStorage.setItem('effectiveLevel', String(effectiveLevel.value))
    if (primaryClubId.value != null) {
      localStorage.setItem('primaryClubId', String(primaryClubId.value))
    } else {
      localStorage.removeItem('primaryClubId')
    }
  }

  function switchPrimaryClub(clubId: number) {
    if (!clubScopeIds.value.includes(clubId)) return
    primaryClubId.value = clubId
    localStorage.setItem('primaryClubId', String(clubId))
  }

  function setUnreadCount(n: number) {
    unreadCount.value = n
  }

  async function logout() {
    try {
      await logoutApi()
    } catch {
      // 即使后端注销失败也清除本地状态
    }
    token.value = ''
    username.value = ''
    userInfo.value = null
    roles.value = []
    roleIds.value = []
    clubScopeIds.value = []
    effectiveLevel.value = 4
    primaryClubId.value = null
    primaryDepartmentId.value = null
    unreadCount.value = 0
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('effectiveLevel')
    localStorage.removeItem('primaryClubId')
  }

  return {
    token,
    username,
    userInfo,
    roles,
    roleIds,
    clubScopeIds,
    effectiveLevel,
    primaryClubId,
    primaryDepartmentId,
    unreadCount,
    isLoggedIn,
    showBudget,
    displayName,
    roleName,
    levelText,
    hasGoldAvatarRing,
    canUseFab,
    canPublishNotice,
    login,
    loadUserProfile,
    switchPrimaryClub,
    setUnreadCount,
    logout,
  }
})
