export interface GasConversionFactor {
  id?: number
  zona: string
  mes: string             // YYYY-MM
  coefConv: number | string
  pcsKwhM3: number | string
}
