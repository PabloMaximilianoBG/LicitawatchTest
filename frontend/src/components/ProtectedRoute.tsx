import { Navigate, Outlet, useLocation } from 'react-router-dom'
import type { Rol } from '@/types'
import { etiquetaRol } from '@/utils/format'
import { rutaInicio, useAuth } from '@/store/auth'
import { ForbiddenState } from './ui'

/** Rutas por rol. El api-gateway y los microservicios vuelven a validar todo. */
export function ProtectedRoute({ roles }: { roles?: Rol[] }) {
  const { isAuthenticated, rol } = useAuth()
  const location = useLocation()
  if (!isAuthenticated()) {
    return <Navigate to={`/login?redirect=${encodeURIComponent(location.pathname + location.search)}`} replace />
  }
  if (roles && rol && !roles.includes(rol)) {
    return <div className="p-6"><ForbiddenState message={`Esta sección es para ${roles.map((r) => etiquetaRol[r]).join(' / ')}.`} /></div>
  }
  return <Outlet />
}

export function RedirectInicio() {
  const rol = useAuth((s) => s.rol)
  return <Navigate to={rutaInicio(rol)} replace />
}
