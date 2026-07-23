import { httpGet } from '@/utils/request'
import type { R } from '@/types/api'
import type { PortalClub } from '@/types/generated'

/** 公开门户 API（社团列表等） */
export const portalApi = {
  clubs(params: Record<string, unknown> = {}): Promise<R<PortalClub[] | { records: PortalClub[]; total: number }>> {
    return httpGet('/portal/clubs', { params }) as Promise<
      R<PortalClub[] | { records: PortalClub[]; total: number }>
    >
  },
}
