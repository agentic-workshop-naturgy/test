export interface InvoiceLine {
  id?: number
  tipo: string
  descripcion: string
  cantidad: number | string
  precioUnitario: number | string
  importe: number | string
}

export interface Invoice {
  id: number
  numeroFactura: string
  cups: string
  periodoInicio: string   // YYYY-MM-DD
  periodoFin: string      // YYYY-MM-DD
  base: number | string
  impuestos: number | string
  total: number | string
  fechaEmision: string    // YYYY-MM-DD
  lines: InvoiceLine[]
}

export interface BillingSuccessEntry {
  cups: string
  invoiceId: number
  numeroFactura: string
}

export interface BillingErrorEntry {
  cups: string
  reason: string
}

export interface BillingResultDto {
  period: string
  invoiced: BillingSuccessEntry[]
  errors: BillingErrorEntry[]
}
