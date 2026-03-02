import httpClient from '../../../shared/api/httpClient'
import type { TaxConfig } from '../types'

export const taxesApi = {
  findAll: () => httpClient.get<TaxConfig[]>('/taxes').then(r => r.data),
  findById: (code: string) => httpClient.get<TaxConfig>(`/taxes/${encodeURIComponent(code)}`).then(r => r.data),
  create: (data: TaxConfig) => httpClient.post<TaxConfig>('/taxes', data).then(r => r.data),
  update: (code: string, data: TaxConfig) => httpClient.put<TaxConfig>(`/taxes/${encodeURIComponent(code)}`, data).then(r => r.data),
  remove: (code: string) => httpClient.delete(`/taxes/${encodeURIComponent(code)}`),
}
