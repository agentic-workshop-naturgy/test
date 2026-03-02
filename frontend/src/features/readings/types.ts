export type TipoLectura = 'REAL' | 'ESTIMADA'

export interface GasReading {
  id?: number
  cups: string
  fecha: string          // YYYY-MM-DD
  lecturaM3: number | string
  tipo: TipoLectura
}
