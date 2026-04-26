import http from './http'

export interface DocumentItem {
  id: number
  filename: string
  fileSize: number
  mimeType: string
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED'
  chunkCount: number
  createdAt: string
  updatedAt: string
}

interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export function listDocuments() {
  return http.get<ApiResult<DocumentItem[]>>('/documents')
}

export function uploadDocument(file: File) {
  const form = new FormData()
  form.append('file', file)
  return http.post<ApiResult<DocumentItem>>('/documents', form)
}

export function deleteDocument(id: number) {
  return http.delete<ApiResult<void>>(`/documents/${id}`)
}
