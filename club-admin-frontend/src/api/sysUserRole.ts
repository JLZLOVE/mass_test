import { httpGet, httpPost, httpDelete } from '@/utils/request'

export interface SysUserRoleItem {
  id?: number
  username?: string
  realName?: string
  roleId?: number
  roleName?: string
  roleCode?: string
  scopeType?: number
  scopeId?: number
  createTime?: string
}

export const sysUserRoleApi = {
  assign(data: { username: string; roleId: number; scopeType?: number; scopeId?: number }) {
    return httpPost('/sys-user-role/assign', data)
  },

  revoke(id: number) {
    return httpDelete(`/sys-user-role/revoke/${id}`)
  },

  myRoles() {
    return httpGet<SysUserRoleItem[]>('/sys-user-role/my-roles')
  },

  rolesByUsername(username: string) {
    return httpGet<SysUserRoleItem[]>(`/sys-user-role/roles/${username}`)
  },
}

export default sysUserRoleApi
