import { httpGet } from '@/utils/request'
import type { R } from '@/types/api'
import type { SysUserRole } from '@/types/generated'

/** 用户角色关联 API */
export const sysUserRoleApi = {
  /** 查询当前登录用户的角色列表 */
  myRoles(): Promise<R<SysUserRole[]>> {
    return httpGet('/sys-user-role/my-roles') as Promise<R<SysUserRole[]>>
  },
}