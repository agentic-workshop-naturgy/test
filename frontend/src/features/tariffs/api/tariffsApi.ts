import httpClient from '../../../shared/api/httpClient'
import type { GasTariff } from '../types'

export const tariffsApi = {
  findAll: () => httpClient.get<GasTariff[]>('/tariffs').then(r => r.data),
  findById: (tarifa: string) => httpClient.get<GasTariff>(`/tariffs/${encodeURIComponent(tarifa)}`).then(r => r.data),
  create: (data: GasTariff) => httpClient.post<GasTariff>('/tariffs', data).then(r => r.data),
  update: (tarifa: string, data: GasTariff) => httpClient.put<GasTariff>(`/tariffs/${encodeURIComponent(tarifa)}`, data).then(r => r.data),
  remove: (tarifa: string) => httpClient.delete(`/tariffs/${encodeURIComponent(tarifa)}`),
}
