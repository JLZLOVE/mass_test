import { httpGet } from '@/utils/request'
import type { R } from '@/types/api'
import type { NoticeInfo, PageResult } from '@/types/generated'

/** 通知 API */
export const noticeInfoApi = {
  /** 收件箱（分页） */
  inbox(params: Record<string, unknown>): Promise<R<PageResult<NoticeInfo>>> {
    return httpGet('/notice-info/inbox', { params }) as Promise<R<PageResult<NoticeInfo>>>
  },
}