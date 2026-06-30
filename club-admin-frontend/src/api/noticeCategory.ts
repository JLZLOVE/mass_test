import { createCrudApi } from './crudFactory'
import type { NoticeCategory } from '@/types/generated'

export const noticeCategoryApi = createCrudApi<NoticeCategory>('/notice-category', 'NoticeCategory')
export default noticeCategoryApi
