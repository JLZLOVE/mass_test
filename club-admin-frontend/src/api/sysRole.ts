import { createCrudApi } from './crudFactory'
import type { SysRole } from '@/types/generated'

export const sysRoleApi = createCrudApi<SysRole>('/sys-role', 'SysRole')
export default sysRoleApi
