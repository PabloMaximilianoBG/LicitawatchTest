import { useState } from 'react'
import { Card, ErrorState, PageHeader, Pagination, Select, Spinner, Table } from '@/components/ui'
import { notificacionesApi } from '@/api/services'
import { useAsync } from '@/hooks/useAsync'
import { fecha } from '@/utils/format'

const TIPOS = ['Licitación publicada', 'Postulación recibida', 'Postulación aprobada', 'Postulación rechazada', 'Pago confirmado', 'Mensaje nuevo']

export default function AdminNotificaciones() {
  const [f, setF] = useState({ tipo: '', page: 0 })
  const { data, loading, error, reload } = useAsync(() => notificacionesApi.admin({ ...f, size: 20 }), [JSON.stringify(f)])
  return (
    <>
      <PageHeader eyebrow="Administración" title="Notificaciones" subtitle="Un registro por cada evento importante enviado por correo." />
      <Card className="mb-4 max-w-sm p-4">
        <Select aria-label="Tipo" value={f.tipo} placeholder="Todos los tipos" options={TIPOS.map((t) => ({ value: t, label: t }))} onChange={(e) => setF({ tipo: e.target.value, page: 0 })} />
      </Card>
      <Card>
        {loading ? <Spinner /> : error ? <ErrorState message={error.mensaje} onRetry={reload} /> : (
          <>
            <Table rows={data?.content ?? []} rowKey={(n) => n.id} columns={[
              { key: 'u', header: 'Usuario', render: (n) => n.usuarioEmail ?? `#${n.usuarioId}` },
              { key: 't', header: 'Evento', render: (n) => <span className="font-medium text-ink-900">{n.tipo}</span> },
              { key: 'c', header: 'Canal', render: (n) => n.canal, hideOnMobile: true },
              { key: 'f', header: 'Fecha', render: (n) => fecha(n.createdAt, true) },
            ]} />
            {data && <Pagination page={data.page} totalPages={data.totalPages} totalElements={data.totalElements} onChange={(page) => setF({ ...f, page })} />}
          </>
        )}
      </Card>
    </>
  )
}
