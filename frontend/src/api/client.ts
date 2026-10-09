import axios, { AxiosError } from 'axios'
import type { ApiError } from '@/types'
import { useAuth } from '@/store/auth'
import { toast } from '@/store/toast'

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

/** El frontend habla SOLO con el api-gateway (PPT diap. 11). */
export const api = axios.create({ baseURL: API_BASE_URL, timeout: 120_000 })

api.interceptors.request.use((config) => {
  const token = useAuth.getState().token
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (r) => r,
  (error: AxiosError<ApiError>) => {
    const status = error.response?.status
    const url = error.config?.url ?? ''
    if (status === 401 && !url.includes('/api/auth/')) {
      useAuth.getState().logout()
      toast.error('Tu sesión expiró o no es válida. Inicia sesión nuevamente.')
      if (!window.location.pathname.startsWith('/login')) {
        window.location.assign(`/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`)
      }
    } else if (status === 429) {
      toast.error(error.response?.data?.mensaje ?? 'Demasiadas solicitudes. Intenta en unos segundos.')
    } else if (!error.response) {
      toast.error('No se pudo conectar con LicitaWatch. Verifica que el api-gateway esté en ejecución.')
    }
    return Promise.reject(error)
  },
)

/** Normaliza cualquier error al formato de error de la API ({codigo, mensaje, errores}). */
export function apiError(e: unknown): ApiError {
  const err = e as AxiosError<ApiError>
  const data = err?.response?.data
  if (data && typeof data === 'object' && 'mensaje' in data) {
    return { status: err.response!.status, codigo: data.codigo, mensaje: data.mensaje, errores: data.errores }
  }
  if (err?.response) return { status: err.response.status, mensaje: 'Ocurrió un error inesperado.' }
  return { status: 0, mensaje: 'No se pudo conectar con el servidor.' }
}
