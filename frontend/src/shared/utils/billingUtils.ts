/**
 * Pure billing UI helper: format monetary values
 */
export function formatEur(value: number | string): string {
  return `${Number(value).toFixed(2)} €`
}

/**
 * Validate YYYY-MM period format
 */
export function isValidPeriod(period: string): boolean {
  return /^\d{4}-\d{2}$/.test(period)
}

/**
 * Compute IVA from base and rate (for display purposes)
 */
export function computeIva(base: number, rate: number): number {
  return Math.round(base * rate * 100) / 100
}

/**
 * Compute total from base and iva
 */
export function computeTotal(base: number, iva: number): number {
  return Math.round((base + iva) * 100) / 100
}
