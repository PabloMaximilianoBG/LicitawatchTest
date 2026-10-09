import { CalendarClock, Download, FileText, MapPin, Tag, Users, Wallet } from 'lucide-react'
import { Card, StatusBadge } from '@/components/ui'
import type { Licitacion } from '@/types'
import { fecha, presupuesto } from '@/utils/format'

/** Detalle de una licitación (columnas del ER con catálogos resueltos). */
export function LicitacionResumen({ l }: { l: Licitacion }) {
  return (
    <Card className="overflow-hidden">
      {l.imagenUrl && <img src={l.imagenUrl} alt="" className="max-h-72 w-full object-cover" />}
      <div className="p-6">
        <div className="flex flex-wrap items-center gap-2"><StatusBadge estado={l.estado} /><span className="text-xs text-ink-500">#{l.id} · publicada por {l.licitadorNombre}</span></div>
        <h2 className="mt-3 text-xl font-bold text-ink-900">{l.titulo}</h2>
        <p className="mt-3 whitespace-pre-line text-sm leading-relaxed text-ink-700">{l.descripcion}</p>
        <dl className="mt-6 grid gap-3 text-sm sm:grid-cols-2">
          <Dato icon={<Tag />} k="Rubro" v={l.rubroNombre} />
          <Dato icon={<MapPin />} k="Región" v={l.regionNombre} />
          <Dato icon={<Wallet />} k="Presupuesto" v={presupuesto(l.presupuestoMin, l.presupuestoMax)} />
          <Dato icon={<CalendarClock />} k="Fecha de cierre" v={fecha(l.fechaCierre)} />
          <Dato icon={<Users />} k="Postulaciones" v={l.maxPostulantes != null ? `${l.cantidadPostulaciones} de ${l.maxPostulantes} (máximo)` : String(l.cantidadPostulaciones)} />
          <Dato icon={<CalendarClock />} k="Publicada" v={fecha(l.createdAt, true)} />
        </dl>
        {l.archivoUrl && (
          <a href={l.archivoUrl} target="_blank" rel="noreferrer" className="mt-6 flex items-center gap-3 rounded-xl border border-ink-200 p-3 text-sm hover:border-brand-300 hover:bg-brand-50/40">
            <FileText className="h-5 w-5 text-brand-600" /><span className="min-w-0 flex-1 truncate font-medium">{l.archivoNombre}</span>
            <span className="text-xs text-ink-500">{l.tipoArchivo}</span><Download className="h-4 w-4 text-ink-400" />
          </a>
        )}
      </div>
    </Card>
  )
}

function Dato({ icon, k, v }: { icon: JSX.Element; k: string; v?: string | null }) {
  return (
    <div className="flex items-start gap-2.5 rounded-xl bg-ink-50 p-3">
      <span className="mt-0.5 text-ink-400 [&>svg]:h-4 [&>svg]:w-4">{icon}</span>
      <div><dt className="text-xs text-ink-500">{k}</dt><dd className="font-medium text-ink-900">{v ?? '—'}</dd></div>
    </div>
  )
}
