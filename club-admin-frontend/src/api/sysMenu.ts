import { httpGet } from '@/utils/request'
import type { R } from '@/types/api'
import type { MenuTreeResult, SysMenu } from '@/types/generated'

/** 菜单 API */
export const sysMenuApi = {
  /** 获取当前用户可见菜单树 */
  list(): Promise<R<MenuTreeResult>> {
    return httpGet('/sys-menu/tree') as Promise<R<MenuTreeResult>>
  },
  /** 管理端平铺列表（可选） */
  page(params: Record<string, unknown> = {}): Promise<R<{ records: SysMenu[]; total: number }>> {
    return httpGet('/sys-menu/list', { params }) as Promise<R<{ records: SysMenu[]; total: number }>>
  },
}
