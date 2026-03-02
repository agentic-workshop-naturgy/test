import axios, { AxiosError } from 'axios'

const httpClient = axios.create({ baseURL: '/api/gas' })

export interface ApiError {
  message: string
  status: number
}

export function mapError(err: unknown): ApiError {
  if (err instanceof AxiosError) {
    const status = err.response?.status ?? 0
    let message = 'Error inesperado'
    if (status === 400) {
      message = err.response?.data?.message ?? err.response?.data ?? 'Solicitud inválida'
    } else if (status === 404) {
      message = 'Recurso no encontrado'
    } else if (status === 409) {
      message = err.response?.data?.message ?? 'Conflicto: el recurso ya existe'
    } else if (status >= 500) {
      message = 'Error interno del servidor'
    } else if (err.message) {
      message = err.message
    }
    return { message: String(message), status }
  }
  return { message: String(err), status: 0 }
}

export default httpClient
