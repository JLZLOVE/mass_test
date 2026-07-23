import { httpGet } from '@/utils/request'
import type { R } from '@/types/api'
import type { PageResult } from '@/types/generated'

export interface SysRole {
  id?: number
  roleName?: string
  roleCode?: string
  roleLevel?: number
  dataScope?: number
  status?: number
}

/** 角色 API */
export const sysRoleApi = {
  list(params: Record<string, unknown> = {}): Promise<R<PageResult<SysRole>>> {
    return httpGet('/sys-role/listSysRole', { params }) as Promise<R<PageResult<SysRole>>>
  },
}
