import { useEffect, useState } from 'react'
import { Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '@/store/auth'
import { toast } from '@/store/toast'
import { chatApi, usuariosApi } from '@/api/services'
import { Sidebar } from './Sidebar'
import { Topbar } from './Topbar'

/** Sidebar + Topbar + contenido. Al entrar refresca el perfil (insignia Premium, datos editados) y los chats sin leer. */
export function DashboardLayout() {
  const [abierto, setAbierto] = useState(false)
  const [chatsSinLeer, setChatsSinLeer] = useState(0)
  const { logout, setUsuario, rol } = useAuth()
  const nav = useNavigate()

  useEffect(() => {
    usuariosApi.me().then(setUsuario).catch(() => undefined)
  }, [setUsuario])

  useEffect(() => {
    if (rol === 'ADMINISTRADOR') return
    const cargar = () => chatApi.mias().then((l) => setChatsSinLeer(l.reduce((s, c) => s + c.noLeidos, 0))).catch(() => undefined)
    cargar()
    const t = setInterval(cargar, 30_000)
    return () => clearInterval(t)
  }, [rol])

  const salir = () => { logout(); toast.info('Sesión cerrada'); nav('/login') }
  return (
    <div className="min-h-screen">
      <Sidebar abierto={abierto} onCerrar={() => setAbierto(false)} onLogout={salir} chatsSinLeer={chatsSinLeer} />
      <div className="lg:pl-72">
        <Topbar onMenu={() => setAbierto(true)} onLogout={salir} />
        <main className="mx-auto w-full max-w-7xl px-4 py-6 animate-fade-in sm:px-6 lg:px-8 lg:py-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
