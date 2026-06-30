import { createCrudApi } from './crudFactory'
import type { ActivityCategory } from '@/types/generated'

export const activityCategoryApi = createCrudApi<ActivityCategory>(
  '/activity-category',
  'ActivityCategory',
)
export default activityCategoryApi
