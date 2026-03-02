import httpClient from '../../../shared/api/httpClient'
import type { GasReading } from '../types'

export const readingsApi = {
  findAll: () => httpClient.get<GasReading[]>('/readings').then(r => r.data),
  findById: (id: number) => httpClient.get<GasReading>(`/readings/${id}`).then(r => r.data),
  create: (data: Omit<GasReading, 'id'>) => httpClient.post<GasReading>('/readings', data).then(r => r.data),
  update: (id: number, data: GasReading) => httpClient.put<GasReading>(`/readings/${id}`, data).then(r => r.data),
  remove: (id: number) => httpClient.delete(`/readings/${id}`),
}
