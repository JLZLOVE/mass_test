import { httpPost } from '@/utils/request'
import type { R } from '@/types/api'

export interface LoginResult {
  token: string
  username: string
}

export interface RegisterDTO {
  username?: string
  realName: string
  password: string
  gender?: number
  userType?: number
}

/** 登录 POST /login/allocation?name=&password= */
export function login(name: string, password: string): Promise<R & LoginResult> {
  return httpPost('/login/allocation', null, { params: { name, password } }) as Promise<R & LoginResult>
}

/** 注册 POST /register/single */
export function register(data: RegisterDTO): Promise<R> {
  return httpPost('/register/single', data)
}
