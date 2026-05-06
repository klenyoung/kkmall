import axios from 'axios'
import { message } from 'ant-design-vue'
import { useAuthStore } from '../stores/auth'

export interface ApiResponse<T> {
  code: string | number
  message: string
  data: T
  traceId?: string
}

export interface PageResult<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
}

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  const token = auth.activeToken
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data as ApiResponse<unknown>
    if (payload.code !== 0) throw new Error(payload.message || String(payload.code))
    return payload.data as any
  },
  (error) => {
    const code = error.response?.data?.code
    if (code === 'AUTH_REQUIRED' || code === 'AUTH_FORBIDDEN') useAuthStore().logoutExpired()
    message.error(error.response?.data?.message || error.message || '请求失败')
    return Promise.reject(error)
  }
)
