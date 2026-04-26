import http from './http'

export interface RagSource {
  content: string
  metadata: Record<string, unknown>
}

export interface RagResult {
  answer: string
  sources: RagSource[]
}

interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export function ragQuery(query: string, topK: number = 5) {
  return http.post<ApiResult<RagResult>>('/rag', { query, topK })
}
