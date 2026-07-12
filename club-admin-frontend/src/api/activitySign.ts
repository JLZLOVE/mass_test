import { httpGet, httpPost } from '@/utils/request'
import type { PageParams, PageResult } from '@/types/api'

export interface SignRecord {
  id?: number
  username?: string
  realName?: string
  signType?: number
  signTime?: string
  address?: string
  isLate?: number
  isEarlyLeave?: number
  checkoutTime?: string
}

export const activitySignApi = {
  listRecords(activityId: number, params?: PageParams) {
    return httpGet<PageResult<SignRecord>>(`/activity-sign/list/${activityId}`, { params })
  },

  adminSign(activityId: number, data: { username: string; address?: string }) {
    return httpPost(`/activity-sign/admin/sign/${activityId}`, data)
  },

  applyMakeup(
    activityId: number,
    data: { username: string; reasonType: number; reasonDetail: string; attachment?: string },
  ) {
    return httpPost(`/activity-sign/apply/${activityId}`, data)
  },
}

export default activitySignApi
