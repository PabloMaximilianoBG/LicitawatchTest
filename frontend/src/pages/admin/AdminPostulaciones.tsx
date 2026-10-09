import { useState } from 'react'
import { Card, ErrorState, PageHeader, Pagination, PremiumBadge, Select, Spinner, StatusBadge, Table } from '@/components/ui'
import { adminLicitacionesApi } from '@/api/services'
import { useAsync } from '@/hooks/useAsync'
import { fecha } from '@/utils/format'

export default function AdminPostulaciones() {
  const [f, setF] = useState({ estado: '', page: 0 })
  const { data, loading, error, reload } = useAsync(() => adminLicitacionesApi.todasLasPostulaciones({ ...f, size: 20 }), [JSON.stringify(f)])
  return (
    <>
      <PageHeader eyebrow="Administración" title="Postulaciones" />
      <Card className="mb-4 max-w-xs p-4">
        <Select aria-label="Estado" value={f.estado} placeholder="Todos los estados" options={['Pendiente', 'Aprobada', 'Rechazada'].map((e) => ({ value: e, label: e }))}
          onChange={(e) => setF({ estado: e.target.value, page: 0 })} />
      </Card>
      <Card>
        {loading ? <Spinner /> : error ? <ErrorState message={error.mensaje} onRetry={reload} /> : (
          <>
            <Table rows={data?.content ?? []} rowKey={(p) => p.id} columns={[
              { key: 'l', header: 'Licitación', render: (p) => <span className="font-medium text-ink-900">{p.licitacionTitulo}</span> },
              { key: 'li', header: 'Licitador', render: (p) => p.licitadorNombre ?? '—', hideOnMobile: true },
              { key: 'p', header: 'Pyme', render: (p) => <span className="flex items-center gap-2">{p.pymeRazonSocial}{p.pymePremium && <PremiumBadge />}</span> },
              { key: 'f', header: 'Fecha', render: (p) => fecha(p.fechaPostulacion) },
              { key: 'e', header: 'Estado', render: (p) => <StatusBadge estado={p.estado} /> },
            ]} />
            {data && <Pagination page={data.page} totalPages={data.totalPages} totalElements={data.totalElements} onChange={(page) => setF({ ...f, page })} />}
          </>
        )}
      </Card>
    </>
  )
}
