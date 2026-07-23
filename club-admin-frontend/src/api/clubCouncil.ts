import { httpGet, httpPost } from '@/utils/request'
import type { R } from '@/types/api'
import type { ClubCouncilDetail, PageResult } from '@/types/generated'

/** 社团合议 API */
export const clubCouncilApi = {
  list(params: Record<string, unknown> = {}): Promise<R<PageResult<ClubCouncilDetail>>> {
    return httpGet('/club-council/list', { params }) as Promise<R<PageResult<ClubCouncilDetail>>>
  },
  detail(params: { id?: number; clubId?: number }): Promise<R<ClubCouncilDetail>> {
    return httpGet('/club-council/detail', { params }) as Promise<R<ClubCouncilDetail>>
  },
  initiate(body: { clubCode: string; reason: string }): Promise<R<unknown>> {
    return httpPost('/club-council/council/initiate', body)
  },
  sign(id: number): Promise<R<unknown>> {
    return httpPost(`/club-council/council/sign/${id}`)
  },
}
