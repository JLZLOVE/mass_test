import { createCrudApi } from './crudFactory'
import type { SysUser } from '@/types/generated'

export const sysUserApi =
  createCrudApi<SysUser>('/sys-user', 'SysUser')

/** sys-user 新增仅后端接口 add_B */
export function addSysUser(data: Partial<SysUser>) {
  return sysUserApi.addB(data)
}

export default sysUserApi
