import { httpGet, httpPost, httpDelete } from '@/utils/request'
import type { R } from '@/types/api'
import type { PageResult, SysUserRole } from '@/types/generated'

export type SysUserRoleItem = SysUserRole

/** 用户角色关联 API */
export const sysUserRoleApi = {
  myRoles(): Promise<R<SysUserRole[]>> {
    return httpGet('/sys-user-role/my-roles') as Promise<R<SysUserRole[]>>
  },
  rolesByUsername(username: string): Promise<R<SysUserRole[]>> {
    return httpGet(`/sys-user-role/roles/${encodeURIComponent(username)}`) as Promise<R<SysUserRole[]>>
  },
  list(params: Record<string, unknown> = {}): Promise<R<PageResult<SysUserRole>>> {
    return httpGet('/sys-user-role/list', { params }) as Promise<R<PageResult<SysUserRole>>>
  },
  assign(data: {
    username: string
    roleId: number
    scopeType?: number | null
    scopeId?: number | null
  }): Promise<R> {
    return httpPost('/sys-user-role/assign', data)
  },
  revoke(id: number): Promise<R> {
    return httpDelete(`/sys-user-role/revoke/${id}`)
  },
}
