import { httpGet } from '@/utils/request'
import type { R } from '@/types/api'
import type { SysCollege } from '@/types/generated'

/** 学院 API */
export const sysCollegeApi = {
  list(params: { keyword?: string } = {}): Promise<R<SysCollege[]>> {
    return httpGet('/sys-college/list', { params }) as Promise<R<SysCollege[]>>
  },
}
