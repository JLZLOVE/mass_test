/** 格式化日期时间 */
export function formatDateTime(value?: string | null): string {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

/** 格式化日期 */
export function formatDate(value?: string | null): string {
  if (!value) return '-'
  return value.slice(0, 10)
}

/** 构建树形结构 */
export function buildTree<T extends { id?: number; parentId?: number; children?: T[] }>(
  list: T[],
  parentId = 0,
): T[] {
  return list
    .filter((item) => (item.parentId ?? 0) === parentId)
    .sort((a, b) => ((a as { sort?: number }).sort ?? 0) - ((b as { sort?: number }).sort ?? 0))
    .map((item) => ({
      ...item,
      children: buildTree(list, item.id),
    }))
}
