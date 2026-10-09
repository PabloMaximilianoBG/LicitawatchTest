import type { ReactNode } from 'react'
import { Crown } from 'lucide-react'
import { cn, etiquetaCuenta, etiquetaRol } from '@/utils/format'

type Tono = 'gray' | 'blue' | 'green' | 'amber' | 'red' | 'violet' | 'indigo'

const tonos: Record<Tono, string> = {
  gray: 'bg-ink-100 text-ink-700 ring-ink-200',
  blue: 'bg-brand-50 text-brand-700 ring-brand-200',
  green: 'bg-emerald-50 text-emerald-700 ring-emerald-200',
  amber: 'bg-amber-50 text-amber-700 ring-amber-200',
  red: 'bg-red-50 text-red-700 ring-red-200',
  violet: 'bg-violet-50 text-violet-700 ring-violet-200',
  indigo: 'bg-indigo-50 text-indigo-700 ring-indigo-200',
}

export function Badge({ tono = 'gray', children, dot, className }: { tono?: Tono; children: ReactNode; dot?: boolean; className?: string }) {
  return (
    <span className={cn('inline-flex items-center gap-1.5 whitespace-nowrap rounded-full px-2.5 py-0.5 text-xs font-semibold ring-1 ring-inset', tonos[tono], className)}>
      {dot && <span className="h-1.5 w-1.5 rounded-full bg-current" aria-hidden />}
      {children}
    </span>
  )
}

/** Valores de los catálogos del ER (estado_licitacion, estado_postulacion, estado_pago, estado_suscripcion) y estados de cuenta. */
const tonoEstado: Record<string, Tono> = {
  Abierta: 'green', Cerrada: 'amber', Adjudicada: 'indigo',
  Pendiente: 'amber', Aprobada: 'green', Rechazada: 'red',
  Aprobado: 'green', Rechazado: 'red', Activa: 'green', Vencida: 'gray', Cancelada: 'gray', 'Pendiente de pago': 'amber',
  ACTIVA: 'green', PENDIENTE_CONFIRMACION: 'amber', DESACTIVADA: 'red',
  PYME: 'blue', LICITADOR: 'indigo', ADMINISTRADOR: 'violet', Estándar: 'gray', Premium: 'violet',
}

export function StatusBadge({ estado }: { estado: string }) {
  return <Badge tono={tonoEstado[estado] ?? 'gray'} dot>{etiquetaRol[estado] ?? etiquetaCuenta[estado] ?? estado}</Badge>
}

/** Insignia Premium visible en el perfil de la Pyme (PPT diap. 7). */
export function PremiumBadge({ className }: { className?: string }) {
  return (
    <span className={cn('inline-flex items-center gap-1 rounded-full bg-gradient-to-r from-violet-600 to-brand-600 px-2.5 py-0.5 text-xs font-semibold text-white shadow-sm', className)}>
      <Crown className="h-3 w-3" aria-hidden />Premium
    </span>
  )
}
