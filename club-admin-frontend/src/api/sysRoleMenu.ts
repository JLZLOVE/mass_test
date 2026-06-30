import { createCrudApi } from './crudFactory'
import type { SysRoleMenu } from '@/types/generated'

export const sysRoleMenuApi = createCrudApi<SysRoleMenu>('/sys-role-menu', 'SysRoleMenu')
export default sysRoleMenuApi
