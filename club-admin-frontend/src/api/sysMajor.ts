import { createCrudApi } from './crudFactory'
import type { SysMajor } from '@/types/generated'

export const sysMajorApi = createCrudApi<SysMajor>('/sys-major', 'SysMajor')
export default sysMajorApi
