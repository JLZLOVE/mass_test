import { createCrudApi } from './crudFactory'
import type { ActivitySign } from '@/types/generated'

export const activitySignApi = createCrudApi<ActivitySign>('/activity-sign', 'ActivitySign')
export default activitySignApi
