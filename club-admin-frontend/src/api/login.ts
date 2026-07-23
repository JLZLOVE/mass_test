import { httpPost, httpGet } from '@/utils/request'
import type { R } from '@/types/api'

/**
 * 登录
 * POST /login/allocation?name=xxx&password=xxx
 * 返回 { token, username }
 */
export async function login(name: string, password: string): Promise<{ token: string; username: string }> {
  const res = await httpPost<Record<string, unknown>>(
    '/login/allocation',
    null,
    { params: { name, password } },
  )
  return (res as unknown as { token: string; username: string }) || { token: '', username: '' }
}

/**
 * 注册
 * POST /register/single
 */
export async function register(data: {
  username: string
  realName: string
  password: string
  userType: number
  gender: number
}): Promise<R> {
  return httpPost('/register/single', data)
}

/**
 * 注销
 * POST /login/logout
 */
export async function logout(): Promise<R> {
  return httpPost('/login/logout')
}