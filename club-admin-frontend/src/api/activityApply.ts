import { createCrudApi } from './crudFactory'
import type { ActivityApply } from '@/types/generated'

export const activityApplyApi = createCrudApi<ActivityApply>('/activity-apply', 'ActivityApply')
export default activityApplyApi
