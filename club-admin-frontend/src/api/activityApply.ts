import { httpGet } from '@/utils/request'
import type { R } from '@/types/api'
import type { ActivityApply, PageResult } from '@/types/generated'

/** 活动申请 / 审批 API（对接当前 /activity-apply） */
export const activityApplyApi = {
  list(params: Record<string, unknown> = {}): Promise<R<PageResult<ActivityApply>>> {
    return httpGet('/activity-apply/list', { params }) as Promise<R<PageResult<ActivityApply>>>
  },
  detail(id: number): Promise<R<unknown>> {
    return httpGet(`/activity-apply/detail/${id}`)
  },
}
