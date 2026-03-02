import { describe, it, expect } from 'vitest'
import { formatEur, isValidPeriod, computeIva, computeTotal } from '../billingUtils'

describe('billingUtils', () => {
  describe('formatEur', () => {
    it('formats number with 2 decimals and EUR symbol', () => {
      expect(formatEur(12.5)).toBe('12.50 €')
      expect(formatEur(0)).toBe('0.00 €')
      expect(formatEur('99.999')).toBe('100.00 €')
    })
  })

  describe('isValidPeriod', () => {
    it('accepts valid YYYY-MM', () => {
      expect(isValidPeriod('2026-01')).toBe(true)
      expect(isValidPeriod('2025-12')).toBe(true)
    })

    it('rejects invalid formats', () => {
      expect(isValidPeriod('')).toBe(false)
      expect(isValidPeriod('2026')).toBe(false)
      expect(isValidPeriod('26-01')).toBe(false)
      expect(isValidPeriod('2026-1')).toBe(false)
    })
  })

  describe('computeIva', () => {
    it('computes IVA rounded to 2 decimals', () => {
      expect(computeIva(100, 0.21)).toBe(21)
      expect(computeIva(10.55, 0.21)).toBe(2.22)
    })
  })

  describe('computeTotal', () => {
    it('sums base and iva', () => {
      expect(computeTotal(100, 21)).toBe(121)
      expect(computeTotal(10.55, 2.22)).toBe(12.77)
    })
  })
})
