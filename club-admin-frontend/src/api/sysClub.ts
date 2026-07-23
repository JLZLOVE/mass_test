import { httpGet, httpPost } from '@/utils/request'
import type { R } from '@/types/api'
import type { PageResult, SysClubItem, SysDepartment, SysUserRole } from '@/types/generated'

/** 管理端社团 API */
export const sysClubApi = {
  list(params: Record<string, unknown> = {}): Promise<R<PageResult<SysClubItem>>> {
    return httpGet('/sys-club/list', { params }) as Promise<R<PageResult<SysClubItem>>>
  },
  detail(clubCode: string): Promise<R<SysClubItem>> {
    return httpGet(`/sys-club/detail/${encodeURIComponent(clubCode)}`) as Promise<R<SysClubItem>>
  },
  departments(clubCode: string): Promise<R<SysDepartment[]>> {
    return httpGet(`/sys-club/departments/${encodeURIComponent(clubCode)}`) as Promise<R<SysDepartment[]>>
  },
  members(clubCode: string, params: Record<string, unknown> = {}): Promise<R<PageResult<SysUserRole>>> {
    return httpGet(`/sys-club/members/${encodeURIComponent(clubCode)}`, { params }) as Promise<
      R<PageResult<SysUserRole>>
    >
  },
}
