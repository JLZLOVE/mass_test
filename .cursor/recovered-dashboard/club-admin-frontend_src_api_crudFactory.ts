import { httpDelete, httpGet, httpPost, httpPut } from '@/utils/request'
import type { PageParams, PageResult, R } from '@/types/api'

type Entity = Record<string, unknown>

export interface CrudApi<T extends Entity> {
  list: () => Promise<R<T[]>>
  listF: (params?: PageParams & Partial<T>) => Promise<R<PageResult<T>>>
  listB: (params?: PageParams & Partial<T>) => Promise<R<PageResult<T>>>
  query: (entity?: Partial<T>) => Promise<R<T[]>>
  detailF: (id: number) => Promise<R<T>>
  detailB: (id: number) => Promise<R<T>>
  addF: (data: Partial<T>) => Promise<R<T>>
  addB: (data: Partial<T>) => Promise<R<T>>
  updateF: (data: Partial<T>) => Promise<R>
  updateB: (data: Partial<T>[]) => Promise<R>
  deleteF: (id: number) => Promise<R>
  deleteB: (ids: number[]) => Promise<R>
}

/** 根据 OpenAPI 规范生成标准 CRUD API */
export function createCrudApi<T extends Entity>(basePath: string, resourceName: string): CrudApi<T> {
  return {
    list: () => httpGet<T[]>(`${basePath}/list${resourceName}`),
    listF: (params = {}) => httpGet<PageResult<T>>(`${basePath}/list${resourceName}_F`, { params }),
    listB: (params = {}) => httpGet<PageResult<T>>(`${basePath}/list${resourceName}_B`, { params }),
    query: (entity = {}) => httpGet<T[]>(`${basePath}/query`, { params: entity }),
    detailF: (id) => httpGet<T>(`${basePath}/detail${resourceName}_F/${id}`),
    detailB: (id) => httpGet<T>(`${basePath}/detail${resourceName}_B/${id}`),
    addF: (data) => httpPost<T>(`${basePath}/add_F`, data),
    addB: (data) => httpPost<T>(`${basePath}/add_B`, data),
    updateF: (data) => httpPut(`${basePath}/update${resourceName}_F`, data),
    updateB: (data) => httpPut(`${basePath}/update${resourceName}_B`, data),
    deleteF: (id) => httpDelete(`${basePath}/delete${resourceName}_F/${id}`),
    deleteB: (ids) => httpDelete(`${basePath}/delete${resourceName}_B`, { data: ids }),
  }
}
