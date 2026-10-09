import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { Perfil, Rol } from '@/types'

interface AuthState {
  token: string | null
  expiraEn: string | null
  usuario: Perfil | null
  rol: Rol | null
  isAuthenticated: () => boolean
  login: (token: string, expiraEn: string, usuario: Perfil) => void
  setUsuario: (u: Perfil) => void
  logout: () => void
}

/** Sesión del usuario logueado, disponible en toda la app y persistida: se mantiene al recargar la página. */
export const useAuth = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      expiraEn: null,
      usuario: null,
      rol: null,
      isAuthenticated: () => {
        const { token, expiraEn } = get()
        return !!token && (!expiraEn || new Date(expiraEn).getTime() > Date.now())
      },
      login: (token, expiraEn, usuario) => set({ token, expiraEn, usuario, rol: usuario.rol }),
      setUsuario: (usuario) => set({ usuario, rol: usuario.rol }),
      logout: () => set({ token: null, expiraEn: null, usuario: null, rol: null }),
    }),
    { name: 'licitawatch-sesion' },
  ),
)

export const nombreVisible = (u: Perfil | null) => u?.razonSocial ?? u?.nombre ?? u?.email ?? ''

export const rutaInicio = (rol: Rol | null) =>
  rol === 'ADMINISTRADOR' ? '/admin/usuarios' : rol === 'LICITADOR' ? '/licitador/licitaciones' : '/pyme/licitaciones'
