import { useState } from 'react'
import { Ban } from 'lucide-react'
import { Button, Card, ConfirmDialog, ErrorState, PageHeader, Pagination, Select, Spinner, StatusBadge, Table, Tabs } from '@/components/ui'
import { adminVentasApi } from '@/api/services'
import { apiError } from '@/api/client'
import { useAsync } from '@/hooks/useAsync'
import { toast } from '@/store/toast'
import type { SuscripcionAdmin } from '@/types'
import { clp, fecha, num } from '@/utils/format'

/** PPT: el Administrador ve ventas y suscripciones. */
export default function AdminVentas() {
  const [tab, setTab] = useState<'ventas' | 'suscripciones'>('ventas')
  const resumen = useAsync(adminVentasApi.resumen)
  return (
    <>
      <PageHeader eyebrow="Administración" title="Ventas y suscripciones" />
      {resumen.data && (
        <div className="mb-6 grid gap-3 sm:grid-cols-3 lg:grid-cols-6">
          {[['Recaudado', clp(resumen.data.totalRecaudado)], ['Pagos aprobados', num(resumen.data.pagosAprobados)], ['Pagos rechazados', num(resumen.data.pagosRechazados)],
            ['Pendientes de pago', num(resumen.data.ventasPendientes)], ['Premium activas', num(resumen.data.premiumActivas)], ['Estándar activas', num(resumen.data.estandarActivas)]].map(([k, v]) => (
            <Card key={k} className="p-4"><p className="text-xs text-ink-500">{k}</p><p className="mt-1 text-lg font-bold text-ink-900">{v}</p></Card>
          ))}
        </div>
      )}
      <Tabs value={tab} onChange={setTab} tabs={[{ value: 'ventas', label: 'Ventas' }, { value: 'suscripciones', label: 'Suscripciones' }]} />
      <div className="mt-4">{tab === 'ventas' ? <Ventas /> : <Suscripciones onCambio={resumen.reload} />}</div>
    </>
  )
}

function Ventas() {
  const [page, setPage] = useState(0)
  const { data, loading, error, reload } = useAsync(() => adminVentasApi.ventas(page, 20), [page])
  return (
    <Card>
      {loading ? <Spinner /> : error ? <ErrorState message={error.mensaje} onRetry={reload} /> : (
        <>
          <Table rows={data?.content ?? []} rowKey={(v) => v.id} columns={[
            { key: 'c', header: 'Pyme', render: (v) => <div><p className="font-medium text-ink-900">{v.clienteNombre ?? `Usuario #${v.usuarioId}`}</p><p className="text-xs text-ink-500">{v.clienteEmail}</p></div> },
            { key: 'p', header: 'Plan', render: (v) => v.plan },
            { key: 'm', header: 'Monto', render: (v) => clp(v.monto) },
            { key: 'f', header: 'Fecha', render: (v) => fecha(v.fecha) },
            { key: 'mp', header: 'Método', render: (v) => v.metodoPago ?? '—', hideOnMobile: true },
            { key: 'e', header: 'Pago', render: (v) => <StatusBadge estado={v.estadoPago} /> },
          ]} />
          {data && <Pagination page={data.page} totalPages={data.totalPages} totalElements={data.totalElements} onChange={setPage} />}
        </>
      )}
    </Card>
  )
}

function Suscripciones({ onCambio }: { onCambio: () => void }) {
  const [f, setF] = useState({ plan: '', estado: '', page: 0 })
  const { data, loading, error, reload } = useAsync(() => adminVentasApi.suscripciones({ ...f, size: 20 }), [JSON.stringify(f)])
  const [cancelar, setCancelar] = useState<SuscripcionAdmin | null>(null)
  const [procesando, setProcesando] = useState(false)
  const ejecutar = async () => {
    if (!cancelar) return
    setProcesando(true)
    try {
      await adminVentasApi.cancelar(cancelar.id)
      toast.success('Premium cancelado: la Pyme vuelve al plan Estándar')
      reload()
      onCambio()
    } catch (e) { toast.error(apiError(e).mensaje) } finally { setProcesando(false); setCancelar(null) }
  }
  return (
    <>
      <Card className="mb-4 grid gap-3 p-4 sm:grid-cols-2">
        <Select aria-label="Plan" value={f.plan} placeholder="Todos los planes" options={['Estándar', 'Premium'].map((p) => ({ value: p, label: p }))} onChange={(e) => setF({ ...f, plan: e.target.value, page: 0 })} />
        <Select aria-label="Estado" value={f.estado} placeholder="Todos los estados" options={['Activa', 'Vencida', 'Cancelada'].map((p) => ({ value: p, label: p }))} onChange={(e) => setF({ ...f, estado: e.target.value, page: 0 })} />
      </Card>
      <Card>
        {loading ? <Spinner /> : error ? <ErrorState message={error.mensaje} onRetry={reload} /> : (
          <>
            <Table rows={data?.content ?? []} rowKey={(s) => s.id} columns={[
              { key: 'c', header: 'Pyme', render: (s) => <div><p className="font-medium text-ink-900">{s.clienteNombre ?? `Usuario #${s.usuarioId}`}</p><p className="text-xs text-ink-500">{s.clienteEmail}</p></div> },
              { key: 'p', header: 'Plan', render: (s) => <StatusBadge estado={s.plan} /> },
              { key: 'e', header: 'Estado', render: (s) => <StatusBadge estado={s.estadoVisible} /> },
              { key: 'v', header: 'Vigencia', render: (s) => s.fechaInicio ? `${fecha(s.fechaInicio)} – ${fecha(s.fechaVencimiento)}` : '—', hideOnMobile: true },
              { key: 'a', header: '', render: (s) => s.plan === 'Premium' && s.estado === 'Activa'
                ? <Button size="sm" variant="ghost" icon={<Ban className="h-4 w-4 text-red-600" />} onClick={() => setCancelar(s)}>Cancelar</Button> : null },
            ]} />
            {data && <Pagination page={data.page} totalPages={data.totalPages} totalElements={data.totalElements} onChange={(page) => setF({ ...f, page })} />}
          </>
        )}
      </Card>
      <ConfirmDialog open={!!cancelar} danger loading={procesando} title="Cancelar Premium" onConfirm={ejecutar} onClose={() => setCancelar(null)}
        message={`${cancelar?.clienteNombre ?? 'La Pyme'} volverá al plan Estándar.`} />
    </>
  )
}
