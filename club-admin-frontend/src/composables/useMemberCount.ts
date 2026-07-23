import { ref, reactive } from 'vue'
import { clubStatisticsApi } from '@/api/clubStatistics'

const TTL_MS = 5000

interface CacheEntry {
  count: number | null
  /** null = 加载中；number = 成功；undefined 表示失败用 -- */
  failed?: boolean
  expireAt: number
}

const cache = reactive<Record<number, CacheEntry>>({})

/**
 * 成员数批量聚合（遵循设计 1A）：
 * - 一次携带当前页所有 clubId
 * - TTL 5 秒内存缓存
 * - 失败展示 --，支持单行重试
 */
export function useMemberCount() {
  const loadingIds = ref<Set<number>>(new Set())

  function getCount(clubId?: number): number | null | undefined {
    if (clubId == null) return undefined
    const entry = cache[clubId]
    if (!entry) return undefined
    if (entry.failed) return undefined
    return entry.count
  }

  function isLoading(clubId?: number): boolean {
    if (clubId == null) return false
    return loadingIds.value.has(clubId)
  }

  function isFailed(clubId?: number): boolean {
    if (clubId == null) return false
    return !!cache[clubId]?.failed
  }

  async function fetchCounts(clubIds: number[]) {
    const now = Date.now()
    const need = clubIds.filter((id) => {
      const entry = cache[id]
      return !entry || entry.expireAt < now || entry.failed
    })
    if (!need.length) return

    need.forEach((id) => {
      loadingIds.value.add(id)
      cache[id] = { count: null, expireAt: now + TTL_MS }
    })
    loadingIds.value = new Set(loadingIds.value)

    try {
      const res = await clubStatisticsApi.list(need)
      const list = res.data || []
      const map = new Map(list.map((i) => [i.clubId, i.memberCount]))
      const expireAt = Date.now() + TTL_MS
      for (const id of need) {
        cache[id] = {
          count: map.has(id) ? (map.get(id) ?? 0) : 0,
          expireAt,
          failed: false,
        }
        loadingIds.value.delete(id)
      }
    } catch {
      const expireAt = Date.now() + TTL_MS
      for (const id of need) {
        cache[id] = { count: null, expireAt, failed: true }
        loadingIds.value.delete(id)
      }
    }
    loadingIds.value = new Set(loadingIds.value)
  }

  async function retryOne(clubId: number) {
    delete cache[clubId]
    await fetchCounts([clubId])
  }

  function invalidate() {
    Object.keys(cache).forEach((k) => delete cache[Number(k)])
  }

  return { getCount, isLoading, isFailed, fetchCounts, retryOne, invalidate }
}
