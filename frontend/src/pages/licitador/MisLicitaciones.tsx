import { Link, useNavigate } from 'react-router-dom'
import { FilePlus2 } from 'lucide-react'
import { Button, Card, EmptyState, ErrorState, PageHeader, Spinner, StatusBadge, Table } from '@/components/ui'
import { licitacionesApi } from '@/api/services'
import { useAsync } from '@/hooks/useAsync'
import { fecha, presupuesto } from '@/utils/format'

export default function MisLicitaciones() {
  const nav = useNavigate()
  const { data, loading, error, reload } = useAsync(licitacionesApi.mias)
  return (
    <>
      <PageHeader eyebrow="Licitador" title="Mis licitaciones" subtitle="Publica sin costo, revisa postulantes y adjudica."
        actions={<Link to="/licitador/licitaciones/nueva"><Button icon={<FilePlus2 className="h-4 w-4" />}>Publicar licitación</Button></Link>} />
      <Card>
        {loading ? <Spinner /> : error ? <ErrorState message={error.mensaje} onRetry={reload} /> : (
          <Table rows={data ?? []} rowKey={(l) => l.id} onRowClick={(l) => nav(`/licitador/licitaciones/${l.id}`)}
            empty={<EmptyState title="Aún no publicas licitaciones" action={<Link to="/licitador/licitaciones/nueva"><Button>Publicar la primera</Button></Link>} />}
            columns={[
              { key: 't', header: 'Licitación', render: (l) => <div><p className="font-medium text-ink-900">{l.titulo}</p><p className="text-xs text-ink-500">{l.rubroNombre} · {l.regionNombre}</p></div> },
              { key: 'p', header: 'Presupuesto', render: (l) => presupuesto(l.presupuestoMin, l.presupuestoMax), hideOnMobile: true },
              { key: 'c', header: 'Cierre', render: (l) => fecha(l.fechaCierre) },
              { key: 'n', header: 'Postulaciones', render: (l) => l.maxPostulantes != null ? `${l.cantidadPostulaciones}/${l.maxPostulantes}` : l.cantidadPostulaciones },
              { key: 'e', header: 'Estado', render: (l) => <StatusBadge estado={l.estado} /> },
            ]} />
        )}
      </Card>
    </>
  )
}
