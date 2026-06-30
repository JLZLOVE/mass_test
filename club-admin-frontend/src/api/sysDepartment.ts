import { createCrudApi } from './crudFactory'
import type { SysDepartment } from '@/types/generated'

export const sysDepartmentApi = createCrudApi<SysDepartment>('/sys-department', 'SysDepartment')
export default sysDepartmentApi
