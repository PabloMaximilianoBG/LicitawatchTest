import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Eye, Lock, LockOpen, Pencil, Trash2 } from 'lucide-react'
import { Button, Card, ConfirmDialog, ErrorState, Modal, PageHeader, Pagination, PremiumBadge, SearchBar, Select, Spinner, StatusBadge, Table } from '@/components/ui'
import { adminLicitacionesApi } from '@/api/services'
import { apiError } from '@/api/client'
import { useAsync } from '@/hooks/useAsync'
import { useCatalogos } from '@/hooks/useCatalogos'
import { toast } from '@/store/toast'
import type { Licitacion } from '@/types'
import { fecha } from '@/utils/format'

/** PPT: el Administrador modera licitaciones (ver, editar, cerrar/reabrir, eliminar). */
export default function AdminLicitaciones() {
  const { rubros } = useCatalogos()
  const [f, setF] = useState({ q: '', estado: '', rubroId: '' as number | '', page: 0 })
  const { data, loading, error, reload } = useAsync(() => adminLicitacionesApi.listar({ ...f, size: 15 }), [JSON.stringify(f)])
  const [accion, setAccion] = useState<{ tipo: 'cerrar' | 'reabrir' | 'eliminar'; l: Licitacion } | null>(null)
  const [ver, setVer] = useState<Licitacion | null>(null)
  const [procesando, setProcesando] = useState(false)
  const ejecutar = async () => {
    if (!accion) return
    setProcesando(true)
    try {
      if (accion.tipo === 'eliminar') await adminLicitacionesApi.eliminar(accion.l.id)
      else await adminLicitacionesApi.estado(accion.l.id, accion.tipo === 'cerrar' ? 'Cerrada' : 'Abierta')
      toast.success('Listo')
      reload()
    } catch (e) { toast.error(apiError(e).mensaje) } finally { setProcesando(false); setAccion(null) }
  }
  return (
    <>
      <PageHeader eyebrow="Administración" title="Licitaciones" subtitle="Moderación de todas las licitaciones publicadas." />
      <Card className="mb-4 grid gap-3 p-4 md:grid-cols-[2fr_1fr_1fr]">
        <SearchBar value={f.q} onSearch={(q) => setF({ ...f, q, page: 0 })} placeholder="Buscar por título…" />
        <Select aria-label="Estado" value={f.estado} placeholder="Todos los estados" options={['Abierta', 'Cerrada', 'Adjudicada'].map((e) => ({ value: e, label: e }))}
          onChange={(e) => setF({ ...f, estado: e.target.value, page: 0 })} />
        <Select aria-label="Rubro" value={String(f.rubroId)} placeholder="Todos los rubros" options={rubros.map((r) => ({ value: String(r.id), label: r.nombre }))}
          onChange={(e) => setF({ ...f, rubroId: e.target.value ? Number(e.target.value) : '', page: 0 })} />
      </Card>
      <Card>
        {loading ? <Spinner /> : error ? <ErrorState message={error.mensaje} onRetry={reload} /> : (
          <>
            <Table rows={data?.content ?? []} rowKey={(l) => l.id} columns={[
              { key: 't', header: 'Licitación', render: (l) => <div><p className="font-medium text-ink-900">#{l.id} {l.titulo}</p><p className="text-xs text-ink-500">{l.licitadorNombre} · {l.rubroNombre}</p></div> },
              { key: 'c', header: 'Cierre', render: (l) => fecha(l.fechaCierre) },
              { key: 'n', header: 'Postulaciones', render: (l) => l.cantidadPostulaciones },
              { key: 'e', header: 'Estado', render: (l) => <StatusBadge estado={l.estado} /> },
              { key: 'a', header: '', render: (l) => (
                <div className="flex justify-end gap-1">
                  <Button size="sm" variant="ghost" icon={<Eye className="h-4 w-4" />} onClick={() => setVer(l)} aria-label="Ver postulaciones" />
                  {l.estado === 'Abierta' && <Link to={`/admin/licitaciones/${l.id}/editar`}><Button size="sm" variant="ghost" icon={<Pencil className="h-4 w-4" />} aria-label="Editar" /></Link>}
                  {l.estado === 'Abierta' && <Button size="sm" variant="ghost" icon={<Lock className="h-4 w-4" />} onClick={() => setAccion({ tipo: 'cerrar', l })} aria-label="Cerrar" />}
                  {l.estado === 'Cerrada' && <Button size="sm" variant="ghost" icon={<LockOpen className="h-4 w-4" />} onClick={() => setAccion({ tipo: 'reabrir', l })} aria-label="Reabrir" />}
                  <Button size="sm" variant="ghost" icon={<Trash2 className="h-4 w-4 text-red-600" />} onClick={() => setAccion({ tipo: 'eliminar', l })} aria-label="Eliminar" />
                </div>) },
            ]} />
            {data && <Pagination page={data.page} totalPages={data.totalPages} totalElements={data.totalElements} onChange={(p) => setF({ ...f, page: p })} />}
          </>
        )}
      </Card>
      <ConfirmDialog open={!!accion} loading={procesando} danger={accion?.tipo === 'eliminar'} onConfirm={ejecutar} onClose={() => setAccion(null)}
        title={accion?.tipo === 'eliminar' ? 'Eliminar licitación' : accion?.tipo === 'cerrar' ? 'Cerrar licitación' : 'Reabrir licitación'}
        message={accion?.tipo === 'eliminar' ? 'Se eliminará con sus postulaciones, archivos y chats.' : accion?.tipo === 'reabrir'
          ? 'Volverá a recibir postulaciones (requiere que la fecha de cierre siga vigente).' : 'Dejará de recibir postulaciones.'} />
      {ver && <PostulacionesModal l={ver} onClose={() => setVer(null)} />}
    </>
  )
}

function PostulacionesModal({ l, onClose }: { l: Licitacion; onClose: () => void }) {
  const { data, loading } = useAsync(() => adminLicitacionesApi.postulaciones(l.id), [l.id])
  return (
    <Modal open onClose={onClose} title={`Postulaciones · #${l.id}`} description={l.titulo} size="lg">
      {loading ? <Spinner /> : (
        <Table rows={data ?? []} rowKey={(p) => p.id} empty={<p className="py-6 text-center text-sm text-ink-500">Sin postulaciones</p>} columns={[
          { key: 'p', header: 'Pyme', render: (p) => <span className="flex items-center gap-2">{p.pymeRazonSocial}{p.pymePremium && <PremiumBadge />}</span> },
          { key: 'f', header: 'Fecha', render: (p) => fecha(p.fechaPostulacion) },
          { key: 'e', header: 'Estado', render: (p) => <StatusBadge estado={p.estado} /> },
        ]} />
      )}
    </Modal>
  )
}
