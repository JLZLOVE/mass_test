import { createCrudApi } from './crudFactory'
import type { SysCollege } from '@/types/generated'

export const sysCollegeApi = createCrudApi<SysCollege>('/sys-college', 'SysCollege')
export default sysCollegeApi
