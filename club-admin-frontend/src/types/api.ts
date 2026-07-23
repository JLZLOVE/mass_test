/** 后端统一响应包装 */
export interface R<T = unknown> {
  code: number
  msg?: string
  data?: T
  [key: string]: unknown
}