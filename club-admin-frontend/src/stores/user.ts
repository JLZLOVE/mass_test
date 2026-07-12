import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi } from '@/api/login'
import { sysUserApi } from '@/api/sysUser'
import { sysUserRoleApi } from '@/api/sysUserRole'
import { canViewBudget } from '@/utils/permission'
import type { SysUser } from '@/types/generated'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const userInfo = ref<SysUser | null>(null)
  const roleIds = ref<number[]>([])
  const clubScopeIds = ref<number[]>([])

  const isLoggedIn = computed(() => !!token.value)
  const showBudget = computed(() => canViewBudget(roleIds.value))

  async function login(name: string, password: string) {
    const res = await loginApi(name, password)
    token.value = (res.token as string) || ''
    username.value = (res.username as string) || name
    localStorage.setItem('token', token.value)
    localStorage.setItem('username', username.value)
    await loadUserProfile()
  }

  async function loadUserProfile() {
    const usersRes = await sysUserApi.query({ username: username.value })
    const users = usersRes.data || []
    userInfo.value = users[0] || null
    const rolesRes = await sysUserRoleApi.myRoles()
    const roles = rolesRes.data || []
    roleIds.value = roles.map((r) => r.roleId!).filter(Boolean)
    clubScopeIds.value = roles
      .filter((r) => r.scopeType === 2 && r.scopeId)
      .map((r) => r.scopeId!)
  }

  function logout() {
    token.value = ''
    username.value = ''
    userInfo.value = null
    roleIds.value = []
    clubScopeIds.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('username')
  }

  return {
    token,
    username,
    userInfo,
    roleIds,
    clubScopeIds,
    isLoggedIn,
    showBudget,
    login,
    loadUserProfile,
    logout,
  }
})
