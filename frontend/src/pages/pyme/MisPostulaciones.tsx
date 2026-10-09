import { useNavigate } from 'react-router-dom'
import { MessagesSquare } from 'lucide-react'
import { Button, Card, EmptyState, ErrorState, PageHeader, Spinner, StatusBadge, Table } from '@/components/ui'
import { chatApi, postulacionesApi } from '@/api/services'
import { apiError } from '@/api/client'
import { useAsync } from '@/hooks/useAsync'
import { toast } from '@/store/toast'
import type { Postulacion } from '@/types'
import { fecha } from '@/utils/format'

/** PPT: la Pyme ve el estado de su postulación (Pendiente, Aprobada, Rechazada). */
export default function MisPostulaciones() {
  const nav = useNavigate()
  const { data, loading, error, reload } = useAsync(postulacionesApi.mias)
  const irAlChat = async (p: Postulacion) => {
    try {
      const c = await chatApi.porPostulacion(p.id)
      nav(`/chat/${c.id}`)
    } catch (e) {
      const a = apiError(e)
      toast.info(a.status === 404 ? 'El Licitador aún no abre el chat. Te avisaremos por correo cuando te escriba.' : a.mensaje)
    }
  }
  return (
    <>
      <PageHeader eyebrow="Pyme" title="Mis postulaciones" subtitle="Estado de cada postulación. Si te adjudican, se habilita el chat privado con el Licitador." />
      <Card>
        {loading ? <Spinner /> : error ? <ErrorState message={error.mensaje} onRetry={reload} /> : (
          <Table rows={data ?? []} rowKey={(p) => p.id} onRowClick={(p) => nav(`/pyme/licitaciones/${p.licitacionId}`)}
            empty={<EmptyState title="Aún no postulas" message="Busca licitaciones abiertas y postula." />}
            columns={[
              { key: 't', header: 'Licitación', render: (p) => <span className="font-medium text-ink-900">{p.licitacionTitulo}</span> },
              { key: 'l', header: 'Licitador', render: (p) => p.licitadorNombre ?? '—', hideOnMobile: true },
              { key: 'f', header: 'Postulaste', render: (p) => fecha(p.fechaPostulacion) },
              { key: 'c', header: 'Cierre', render: (p) => fecha(p.fechaCierre), hideOnMobile: true },
              { key: 'e', header: 'Estado', render: (p) => <StatusBadge estado={p.estado} /> },
              { key: 'a', header: '', render: (p) => p.chatDisponible
                ? <Button size="sm" variant="secondary" icon={<MessagesSquare className="h-4 w-4" />} onClick={(e) => { e.stopPropagation(); irAlChat(p) }}>Chat</Button> : null },
            ]} />
        )}
      </Card>
    </>
  )
}
