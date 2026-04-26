import http from './http'

export interface AuthResponse {
  token: string
  username: string
}

interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export function register(username: string, password: string) {
  return http.post<ApiResult<AuthResponse>>('/auth/register', { username, password })
}

export function login(username: string, password: string) {
  return http.post<ApiResult<AuthResponse>>('/auth/login', { username, password })
}
