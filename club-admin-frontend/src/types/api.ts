/** 后端统一响应 R */
export interface R<T = unknown> {
  code: number
  msg?: string
  data?: T
  token?: string
  username?: string
  [key: string]: unknown
}

/** MyBatis-Plus 分页结果 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/** 分页查询参数 */
export interface PageParams {
  page?: number
  limit?: number
  sidx?: string
  order?: 'asc' | 'desc'
}
