import type { ReactNode } from 'react'
import { Bell, Bot, Building2, CreditCard, FilePlus2, FileSearch, Gavel, LifeBuoy, ListChecks, MessagesSquare, Send, Tags, User, Users, Wallet } from 'lucide-react'
import type { Rol } from '@/types'

export interface NavItem { to: string; label: string; icon: ReactNode; end?: boolean; premium?: boolean; chat?: boolean }
export interface NavGrupo { titulo: string; items: NavItem[] }

const i = (C: typeof Bell) => <C className="h-[18px] w-[18px]" />

/** Solo las funcionalidades de la PPT (diap. 6) para cada perfil. */
export const navegacion: Record<Rol, NavGrupo[]> = {
  PYME: [
    { titulo: 'Licitaciones', items: [
      { to: '/pyme/licitaciones', label: 'Buscar licitaciones', icon: i(FileSearch) },
      { to: '/pyme/postulaciones', label: 'Mis postulaciones', icon: i(Send) },
      { to: '/chat', label: 'Chat', icon: i(MessagesSquare), chat: true },
      { to: '/asistente', label: 'LicitAsist', icon: i(Bot), premium: true },
    ] },
    { titulo: 'Cuenta', items: [
      { to: '/pyme/suscripcion', label: 'Mi plan', icon: i(Wallet) },
      { to: '/perfil', label: 'Mi perfil', icon: i(User) },
      { to: '/notificaciones', label: 'Avisos por correo', icon: i(Bell) },
      { to: '/soporte', label: 'Soporte', icon: i(LifeBuoy) },
    ] },
  ],
  LICITADOR: [
    { titulo: 'Licitaciones', items: [
      { to: '/licitador/licitaciones', label: 'Mis licitaciones', icon: i(Gavel), end: true },
      { to: '/licitador/licitaciones/nueva', label: 'Publicar licitación', icon: i(FilePlus2) },
      { to: '/chat', label: 'Chat', icon: i(MessagesSquare), chat: true },
      { to: '/asistente', label: 'LicitAsist', icon: i(Bot) },
    ] },
    { titulo: 'Cuenta', items: [
      { to: '/perfil', label: 'Mi perfil', icon: i(User) },
      { to: '/notificaciones', label: 'Avisos por correo', icon: i(Bell) },
      { to: '/soporte', label: 'Soporte', icon: i(LifeBuoy) },
    ] },
  ],
  ADMINISTRADOR: [
    { titulo: 'Administración', items: [
      { to: '/admin/usuarios', label: 'Usuarios y roles', icon: i(Users) },
      { to: '/admin/licitaciones', label: 'Licitaciones', icon: i(Building2) },
      { to: '/admin/postulaciones', label: 'Postulaciones', icon: i(ListChecks) },
      { to: '/admin/ventas', label: 'Ventas y suscripciones', icon: i(CreditCard) },
      { to: '/admin/notificaciones', label: 'Notificaciones', icon: i(Bell) },
      { to: '/admin/catalogos', label: 'Rubros', icon: i(Tags) },
      { to: '/asistente', label: 'LicitAsist', icon: i(Bot) },
    ] },
    { titulo: 'Cuenta', items: [{ to: '/perfil', label: 'Mi perfil', icon: i(User) }] },
  ],
}
