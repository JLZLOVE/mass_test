import { createCrudApi } from './crudFactory'
import type { SysDataPermission } from '@/types/generated'

export const sysDataPermissionApi = createCrudApi<SysDataPermission>(
  '/sys-data-permission',
  'SysDataPermission',
)
export default sysDataPermissionApi
