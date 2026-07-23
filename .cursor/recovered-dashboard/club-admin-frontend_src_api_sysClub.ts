import { createCrudApi } from './crudFactory'
import type { SysClub } from '@/types/generated'

export const sysClubApi = createCrudApi<SysClub>('/sys-club', 'SysClub')
export default sysClubApi
