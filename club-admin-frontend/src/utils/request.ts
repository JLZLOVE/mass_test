import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { R } from '@/types/api'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/Mass_Test'

const service: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
})

service.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
    config.headers.Token = token
  }
  return config
})

service.interceptors.response.use(
  (response: AxiosResponse<R>) => {
    const res = response.data
    if (res.code !== 0 && res.code !== undefined) {
      ElMessage.error(res.msg || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        window.location.hash = '#/login'
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return response
  },
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  },
)

/** GET 请求，返回后端 R 包装体 */
export async function httpGet<T>(url: string, config?: AxiosRequestConfig): Promise<R<T>> {
  const response: AxiosResponse<R<T>> = await service.get(url, config)
  return response.data
}

/** POST 请求，返回后端 R 包装体 */
export async function httpPost<T>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig,
): Promise<R<T>> {
  const response: AxiosResponse<R<T>> = await service.post(url, data, config)
  return response.data
}

/** PUT 请求，返回后端 R 包装体 */
export async function httpPut<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<R<T>> {
  const response: AxiosResponse<R<T>> = await service.put(url, data, config)
  return response.data
}

/** DELETE 请求，返回后端 R 包装体 */
export async function httpDelete<T>(url: string, config?: AxiosRequestConfig): Promise<R<T>> {
  const response: AxiosResponse<R<T>> = await service.delete(url, config)
  return response.data
}

export default service
