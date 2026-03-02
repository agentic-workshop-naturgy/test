import httpClient from '../../../shared/api/httpClient'
import type { Invoice, BillingResultDto } from '../types'

export const billingApi = {
  runBilling: (period: string) =>
    httpClient.post<BillingResultDto>(`/billing/${encodeURIComponent(period)}`).then(r => r.data),

  findInvoices: (params?: { cups?: string; period?: string }) =>
    httpClient.get<Invoice[]>('/invoices', { params }).then(r => r.data),

  findInvoiceById: (id: number) =>
    httpClient.get<Invoice>(`/invoices/${id}`).then(r => r.data),

  getPdfUrl: (id: number) => `/api/gas/invoices/${id}/pdf`,
}
