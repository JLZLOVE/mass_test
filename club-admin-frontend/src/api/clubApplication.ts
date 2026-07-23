import { httpGet, httpPost } from '@/utils/request'
import type { R } from '@/types/api'
import type { ClubCategoryItem, PageResult } from '@/types/generated'

export interface ClubCreateApplyBody {
  clubName: string
  collegeId: number
  category: string
  proposedLeaderUsername: string
  maxMembers: number
  description?: string
}

export interface ClubDissolveApplyBody {
  clubCode: string
  dissolveReason: string
}

/** 社团申请 API */
export const clubApplicationApi = {
  categories(): Promise<R<ClubCategoryItem[]>> {
    return httpGet('/club-application/categories') as Promise<R<ClubCategoryItem[]>>
  },
  create(body: ClubCreateApplyBody): Promise<R<string>> {
    return httpPost('/club-application/apply/create', body) as Promise<R<string>>
  },
  dissolve(body: ClubDissolveApplyBody): Promise<R<unknown>> {
    return httpPost('/club-application/apply/dissolve', body)
  },
  list(params: Record<string, unknown> = {}): Promise<R<PageResult<unknown>>> {
    return httpGet('/club-application/apply/list', { params }) as Promise<R<PageResult<unknown>>>
  },
  approveAdmin(body: {
    username?: string
    applicationNo?: string
    approved: boolean
    opinion?: string
  }): Promise<R<unknown>> {
    return httpPost('/club-application/approve/admin', body)
  },
}
