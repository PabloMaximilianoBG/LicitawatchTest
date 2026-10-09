import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { ChevronDown, LogOut, Menu, User } from 'lucide-react'
import { PremiumBadge } from '@/components/ui'
import { nombreVisible, useAuth } from '@/store/auth'
import { etiquetaRol } from '@/utils/format'

export function Topbar({ onMenu, onLogout }: { onMenu: () => void; onLogout: () => void }) {
  const { usuario, rol } = useAuth()
  const [abierto, setAbierto] = useState(false)
  const ref = useRef<HTMLDivElement>(null)
  useEffect(() => {
    const h = (e: MouseEvent) => { if (ref.current && !ref.current.contains(e.target as Node)) setAbierto(false) }
    document.addEventListener('mousedown', h)
    return () => document.removeEventListener('mousedown', h)
  }, [])
  const nombre = nombreVisible(usuario)
  const iniciales = nombre.split(' ').map((p) => p[0]).slice(0, 2).join('').toUpperCase()
  return (
    <header className="sticky top-0 z-20 flex h-16 items-center gap-3 border-b border-ink-200/70 bg-white/80 px-4 backdrop-blur-xl sm:px-6">
      <button onClick={onMenu} className="rounded-lg p-2 text-ink-600 hover:bg-ink-100 lg:hidden" aria-label="Abrir menú"><Menu className="h-5 w-5" /></button>
      <div className="flex-1" />
      <div className="relative" ref={ref}>
        <button onClick={() => setAbierto((v) => !v)} className="flex items-center gap-2.5 rounded-xl py-1.5 pl-1.5 pr-2.5 transition hover:bg-ink-100">
          <span className="grid h-8 w-8 place-items-center rounded-lg bg-brand-gradient text-xs font-bold text-white">{iniciales}</span>
          <span className="hidden max-w-[200px] text-left sm:block">
            <span className="block truncate text-sm font-semibold text-ink-900">{nombre}</span>
            <span className="block text-xs text-ink-500">{rol && etiquetaRol[rol]}</span>
          </span>
          {usuario?.premium && <PremiumBadge className="hidden sm:inline-flex" />}
          <ChevronDown className="h-4 w-4 text-ink-400" />
        </button>
        {abierto && (
          <div className="absolute right-0 mt-2 w-56 overflow-hidden rounded-2xl border border-ink-200 bg-white py-1 shadow-xl animate-fade-in">
            <p className="truncate px-4 py-2 text-xs text-ink-500">{usuario?.email}</p>
            <Link to="/perfil" onClick={() => setAbierto(false)} className="flex items-center gap-2 px-4 py-2 text-sm hover:bg-ink-50"><User className="h-4 w-4" />Mi perfil</Link>
            <button onClick={onLogout} className="flex w-full items-center gap-2 px-4 py-2 text-sm text-red-600 hover:bg-red-50"><LogOut className="h-4 w-4" />Cerrar sesión</button>
          </div>
        )}
      </div>
    </header>
  )
}
