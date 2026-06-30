import { createCrudApi } from './crudFactory'
import type { ActivityApproveFlow } from '@/types/generated'

export const activityApproveFlowApi = createCrudApi<ActivityApproveFlow>(
  '/activity-approve-flow',
  'ActivityApproveFlow',
)
export default activityApproveFlowApi
