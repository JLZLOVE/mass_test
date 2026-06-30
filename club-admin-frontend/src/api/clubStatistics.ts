import { createCrudApi } from './crudFactory'
import type { ClubStatistics } from '@/types/generated'

export const clubStatisticsApi = createCrudApi<ClubStatistics>('/club-statistics', 'ClubStatistics')
export default clubStatisticsApi
