import { httpGet } from '@/utils/request'
import type { R } from '@/types/api'
import type { MenuTreeResult } from '@/types/generated'

/** 菜单 API */
export const sysMenuApi = {
  /** 获取当前用户可见菜单树 */
  list(): Promise<R<MenuTreeResult>> {
    return httpGet('/sys-menu/tree') as Promise<R<MenuTreeResult>>
  },
}