import { useState } from 'react'
import { Mail } from 'lucide-react'
import { Card, EmptyState, ErrorState, PageHeader, Pagination, Spinner, Table } from '@/components/ui'
import { notificacionesApi } from '@/api/services'
import { useAsync } from '@/hooks/useAsync'
import { fecha } from '@/utils/format'

/** Registro de los avisos automáticos que se te enviaron por correo (tabla notificacion del ER). */
export default function Avisos() {
  const [page, setPage] = useState(0)
  const { data, loading, error, reload } = useAsync(() => notificacionesApi.mias(page, 20), [page])
  return (
    <>
      <PageHeader title="Avisos por correo" subtitle="Notificaciones automáticas que LicitaWatch te envió por email." />
      <Card>
        {loading ? <Spinner /> : error ? <ErrorState message={error.mensaje} onRetry={reload} /> : (
          <>
            <Table rows={data?.content ?? []} rowKey={(n) => n.id} empty={<EmptyState icon={<Mail className="h-6 w-6" />} title="Aún no tienes avisos" />}
              columns={[
                { key: 't', header: 'Evento', render: (n) => <span className="font-medium text-ink-900">{n.tipo}</span> },
                { key: 'c', header: 'Canal', render: (n) => n.canal },
                { key: 'f', header: 'Fecha', render: (n) => fecha(n.createdAt, true) },
              ]} />
            {data && <Pagination page={data.page} totalPages={data.totalPages} totalElements={data.totalElements} onChange={setPage} />}
          </>
        )}
      </Card>
    </>
  )
}
