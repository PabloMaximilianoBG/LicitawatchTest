import { Link } from 'react-router-dom'
import { CalendarClock, MapPin, Tag, Users } from 'lucide-react'
import { StatusBadge } from '@/components/ui'
import type { Licitacion } from '@/types'
import { fecha, presupuesto } from '@/utils/format'

export function LicitacionCard({ l, to }: { l: Licitacion; to: string }) {
  return (
    <Link to={to} className="card group flex flex-col overflow-hidden transition hover:-translate-y-0.5 hover:shadow-glow">
      {l.imagenUrl
        ? <img src={l.imagenUrl} alt="" className="h-36 w-full object-cover" loading="lazy" />
        : <div className="h-2 w-full bg-brand-gradient" />}
      <div className="flex flex-1 flex-col p-5">
        <div className="flex items-start justify-between gap-2">
          <h3 className="line-clamp-2 font-semibold text-ink-900 group-hover:text-brand-700">{l.titulo}</h3>
          {l.miPostulacionEstado ? <StatusBadge estado={l.miPostulacionEstado} /> : <StatusBadge estado={l.estado} />}
        </div>
        <p className="mt-1 text-xs text-ink-500">#{l.id} · {l.licitadorNombre ?? 'Licitador'}</p>
        <p className="mt-3 line-clamp-2 text-sm text-ink-600">{l.descripcion}</p>
        <div className="mt-auto space-y-1.5 pt-4 text-xs text-ink-600">
          <p className="flex items-center gap-1.5"><Tag className="h-3.5 w-3.5 text-ink-400" />{l.rubroNombre}</p>
          <p className="flex items-center gap-1.5"><MapPin className="h-3.5 w-3.5 text-ink-400" />{l.regionNombre}</p>
          <p className="flex items-center gap-1.5"><CalendarClock className="h-3.5 w-3.5 text-ink-400" />Cierra el {fecha(l.fechaCierre)}
            {l.estado === 'Abierta' && l.diasParaCierre <= 7 && <span className="font-semibold text-amber-600">· {l.diasParaCierre === 0 ? 'hoy' : `en ${l.diasParaCierre} d`}</span>}</p>
          {l.maxPostulantes != null && <p className="flex items-center gap-1.5"><Users className="h-3.5 w-3.5 text-ink-400" />{l.cuposDisponibles} de {l.maxPostulantes} cupos disponibles</p>}
        </div>
        <p className="mt-3 border-t border-ink-100 pt-3 text-sm font-semibold text-ink-900">{presupuesto(l.presupuestoMin, l.presupuestoMax)}</p>
      </div>
    </Link>
  )
}
