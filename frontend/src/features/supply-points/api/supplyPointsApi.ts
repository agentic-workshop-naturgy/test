import httpClient from '../../../shared/api/httpClient'
import type { SupplyPoint } from '../types'

export const supplyPointsApi = {
  findAll: () => httpClient.get<SupplyPoint[]>('/supply-points').then(r => r.data),
  findById: (cups: string) => httpClient.get<SupplyPoint>(`/supply-points/${encodeURIComponent(cups)}`).then(r => r.data),
  create: (data: SupplyPoint) => httpClient.post<SupplyPoint>('/supply-points', data).then(r => r.data),
  update: (cups: string, data: SupplyPoint) => httpClient.put<SupplyPoint>(`/supply-points/${encodeURIComponent(cups)}`, data).then(r => r.data),
  remove: (cups: string) => httpClient.delete(`/supply-points/${encodeURIComponent(cups)}`),
}
