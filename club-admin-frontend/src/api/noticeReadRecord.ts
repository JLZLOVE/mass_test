import { createCrudApi } from './crudFactory'
import type { NoticeReadRecord } from '@/types/generated'

export const noticeReadRecordApi = createCrudApi<NoticeReadRecord>(
  '/notice-read-record',
  'NoticeReadRecord',
)
export default noticeReadRecordApi
