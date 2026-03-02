import httpClient from '../../../shared/api/httpClient'
import type { GasConversionFactor } from '../types'

export const factorsApi = {
  findAll: () => httpClient.get<GasConversionFactor[]>('/factors').then(r => r.data),
  findById: (id: number) => httpClient.get<GasConversionFactor>(`/factors/${id}`).then(r => r.data),
  create: (data: Omit<GasConversionFactor, 'id'>) => httpClient.post<GasConversionFactor>('/factors', data).then(r => r.data),
  update: (id: number, data: GasConversionFactor) => httpClient.put<GasConversionFactor>(`/factors/${id}`, data).then(r => r.data),
  remove: (id: number) => httpClient.delete(`/factors/${id}`),
}
