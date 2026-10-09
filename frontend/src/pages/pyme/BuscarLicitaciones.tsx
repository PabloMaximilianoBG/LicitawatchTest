import { useState } from 'react'
import { Crown, SlidersHorizontal } from 'lucide-react'
import { Link } from 'react-router-dom'
import { Button, Card, EmptyState, ErrorState, Input, LoadingCards, PageHeader, Pagination, SearchBar, Select } from '@/components/ui'
import { LicitacionCard } from '@/components/LicitacionCard'
import { licitacionesApi, postulacionesApi, type FiltrosBusqueda } from '@/api/services'
import { useAsync } from '@/hooks/useAsync'
import { useCatalogos } from '@/hooks/useCatalogos'
import { useAuth } from '@/store/auth'

/** PPT: búsqueda y filtros de licitaciones sin límite (ambos planes). */
export default function BuscarLicitaciones() {
  const usuario = useAuth((s) => s.usuario)
  const { rubros, regiones } = useCatalogos()
  const [f, setF] = useState<FiltrosBusqueda>({ q: '', rubroId: '', regionId: '', presupuestoMin: '', presupuestoMax: '', orden: 'cierre', page: 0, size: 12 })
  const { data, loading, error, reload } = useAsync(() => licitacionesApi.buscar(f), [JSON.stringify(f)])
  const uso = useAsync(postulacionesApi.uso)
  const set = (c: Partial<FiltrosBusqueda>) => setF((x) => ({ ...x, ...c, page: c.page ?? 0 }))
  return (
    <>
      <PageHeader eyebrow="Pyme" title="Buscar licitaciones" subtitle="Licitaciones abiertas con cupos disponibles." />
      {uso.data && (
        <Card className="mb-5 flex flex-col gap-3 p-4 sm:flex-row sm:items-center sm:justify-between">
          <p className="text-sm text-ink-700">Plan <b>{uso.data.plan}</b>: usaste <b>{uso.data.postulacionesMes}</b> de <b>{uso.data.limitePostulacionesMes}</b> postulaciones este mes.</p>
          {!uso.data.premium && <Link to="/pyme/suscripcion"><Button size="sm" variant="secondary" icon={<Crown className="h-4 w-4" />}>Hasta 7 al mes con Premium</Button></Link>}
        </Card>
      )}
      <Card className="mb-6 p-4">
        <div className="grid gap-3 md:grid-cols-[2fr_1fr_1fr]">
          <SearchBar value={f.q ?? ''} onSearch={(q) => set({ q })} placeholder="Buscar por título o descripción…" />
          <Select aria-label="Rubro" value={String(f.rubroId)} placeholder="Todos los rubros" options={rubros.map((r) => ({ value: String(r.id), label: r.nombre }))}
            onChange={(e) => set({ rubroId: e.target.value ? Number(e.target.value) : '' })} />
          <Select aria-label="Región" value={String(f.regionId)} placeholder="Todas las regiones" options={regiones.map((r) => ({ value: String(r.id), label: r.nombre }))}
            onChange={(e) => set({ regionId: e.target.value ? Number(e.target.value) : '' })} />
        </div>
        <div className="mt-3 grid gap-3 sm:grid-cols-[1fr_1fr_1fr_auto]">
          <Input type="number" min={0} placeholder="Presupuesto desde" aria-label="Presupuesto desde" value={f.presupuestoMin}
            onChange={(e) => set({ presupuestoMin: e.target.value ? Number(e.target.value) : '' })} />
          <Input type="number" min={0} placeholder="Presupuesto hasta" aria-label="Presupuesto hasta" value={f.presupuestoMax}
            onChange={(e) => set({ presupuestoMax: e.target.value ? Number(e.target.value) : '' })} />
          <Select aria-label="Orden" value={f.orden} options={[{ value: 'cierre', label: 'Cierran primero' }, { value: 'recientes', label: 'Más recientes' }]}
            onChange={(e) => set({ orden: e.target.value })} />
          <Button variant="ghost" icon={<SlidersHorizontal className="h-4 w-4" />} onClick={() => set({ q: '', rubroId: '', regionId: '', presupuestoMin: '', presupuestoMax: '' })}>Limpiar</Button>
        </div>
        {usuario?.rubroId && f.rubroId !== usuario.rubroId && (
          <button onClick={() => set({ rubroId: usuario.rubroId! })} className="mt-3 text-xs font-semibold text-brand-600 hover:underline">Ver solo mi rubro ({usuario.rubroNombre})</button>
        )}
      </Card>
      {loading ? <LoadingCards n={6} /> : error ? <ErrorState message={error.mensaje} onRetry={reload} /> : !data?.content.length
        ? <EmptyState title="No hay licitaciones con esos filtros" message="Prueba con otro rubro o región." />
        : <>
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
            {data.content.map((l) => <LicitacionCard key={l.id} l={l} to={`/pyme/licitaciones/${l.id}`} />)}
          </div>
          <Pagination page={data.page} totalPages={data.totalPages} totalElements={data.totalElements} onChange={(p) => set({ page: p })} />
        </>}
    </>
  )
}
