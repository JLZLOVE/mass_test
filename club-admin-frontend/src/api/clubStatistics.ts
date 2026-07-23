import { httpGet } from '@/utils/request'
import type { R } from '@/types/api'
import type { ClubMemberCount } from '@/types/generated'

/** 社团统计 / 成员数聚合 */
export const clubStatisticsApi = {
  list(clubIds: number[]): Promise<R<ClubMemberCount[]>> {
    return httpGet('/club-statistics/list', {
      params: { clubIds: clubIds.join(',') },
    }) as Promise<R<ClubMemberCount[]>>
  },
}
