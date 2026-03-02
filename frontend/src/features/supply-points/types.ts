export type EstadoSupplyPoint = 'ACTIVO' | 'INACTIVO'

export interface SupplyPoint {
  cups: string
  zona: string
  tarifa: string
  estado: EstadoSupplyPoint
}
