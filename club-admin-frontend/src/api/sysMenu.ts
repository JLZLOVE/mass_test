import { createCrudApi } from './crudFactory'
import type { SysMenu } from '@/types/generated'

export const sysMenuApi = createCrudApi<SysMenu>('/sys-menu', 'SysMenu')
export default sysMenuApi
