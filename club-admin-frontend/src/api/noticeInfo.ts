import { createCrudApi } from './crudFactory'
import type { NoticeInfo } from '@/types/generated'

export const noticeInfoApi = createCrudApi<NoticeInfo>('/notice-info', 'NoticeInfo')
export default noticeInfoApi
