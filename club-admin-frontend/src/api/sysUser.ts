import { httpGet } from '@/utils/request'
import type { R } from '@/types/api'
import type { SysUser } from '@/types/generated'

/** 用户管理 API */
export const sysUserApi = {
  /** 分页查询用户（按权限自动过滤） */
  query(params: Record<string, unknown>): Promise<R<{ records: SysUser[]; total: number }>> {
    return httpGet('/sys-user/listSysUser', { params }) as Promise<R<{ records: SysUser[]; total: number }>>
  },
}