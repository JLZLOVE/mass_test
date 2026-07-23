import { httpGet } from '@/utils/request'
import type { R } from '@/types/api'
import type { PageResult, SysUser } from '@/types/generated'

/** 用户管理 API */
export const sysUserApi = {
  /** 分页查询用户（按权限自动过滤） */
  query(params: Record<string, unknown>): Promise<R<PageResult<SysUser>>> {
    return httpGet('/sys-user/listSysUser', { params }) as Promise<R<PageResult<SysUser>>>
  },
  detail(username: string): Promise<R<SysUser>> {
    return httpGet(`/sys-user/detailSysUser/${encodeURIComponent(username)}`) as Promise<R<SysUser>>
  },
}
