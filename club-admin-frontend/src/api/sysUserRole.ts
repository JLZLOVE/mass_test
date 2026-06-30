import { createCrudApi } from './crudFactory'
import type { SysUserRole } from '@/types/generated'

export const sysUserRoleApi = createCrudApi<SysUserRole>('/sys-user-role', 'SysUserRole')
export default sysUserRoleApi
