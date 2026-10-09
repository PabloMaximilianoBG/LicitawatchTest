import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, Bot, Send } from 'lucide-react'
import { Button, Card, ErrorState, PageHeader, Spinner, StatusBadge, Textarea } from '@/components/ui'
import { LicitacionResumen } from '@/components/LicitacionResumen'
import { licitacionesApi, postulacionesApi } from '@/api/services'
import { apiError } from '@/api/client'
import { useAsync } from '@/hooks/useAsync'
import { useAuth } from '@/store/auth'
import { toast } from '@/store/toast'

export default function LicitacionDetallePyme() {
  const id = Number(useParams().id)
  const premium = useAuth((s) => s.usuario?.premium)
  const { data: l, loading, error, reload } = useAsync(() => licitacionesApi.obtener(id), [id])
  const uso = useAsync(postulacionesApi.uso)
  const [mensaje, setMensaje] = useState('')
  const [enviando, setEnviando] = useState(false)

  const postular = async () => {
    setEnviando(true)
    try {
      await licitacionesApi.postular(id, mensaje.trim())
      toast.success('Postulación enviada. Quedó en estado Pendiente.')
      setMensaje('')
      reload()
      uso.reload()
    } catch (e) {
      toast.error(apiError(e).mensaje)
    } finally {
      setEnviando(false)
    }
  }

  if (loading) return <Spinner />
  if (error || !l) return <ErrorState message={error?.mensaje} onRetry={reload} />
  const sinCupoMensual = uso.data && uso.data.restantes === 0
  return (
    <>
      <Link to="/pyme/licitaciones" className="mb-4 inline-flex items-center gap-1 text-sm text-ink-500 hover:text-ink-900"><ArrowLeft className="h-4 w-4" />Volver</Link>
      <PageHeader title="Detalle de la licitación" actions={premium && <Link to={`/asistente?licitacion=${l.id}`}><Button variant="secondary" icon={<Bot className="h-4 w-4" />}>Preguntar a LicitAsist</Button></Link>} />
      <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
        <LicitacionResumen l={l} />
        <Card className="h-fit p-5">
          <h3 className="font-semibold text-ink-900">Postular</h3>
          {l.miPostulacionEstado ? (
            <div className="mt-3 space-y-2 text-sm text-ink-700">
              <p>Ya postulaste. Estado: <StatusBadge estado={l.miPostulacionEstado} /></p>
              <Link to="/pyme/postulaciones" className="font-semibold text-brand-600 hover:underline">Ver mis postulaciones</Link>
            </div>
          ) : !l.disponibleParaPostular ? (
            <p className="mt-3 text-sm text-ink-600">Esta licitación ya no recibe postulaciones.</p>
          ) : (
            <div className="mt-3 space-y-3">
              <Textarea label="Mensaje para el Licitador (opcional)" rows={5} maxLength={2000} value={mensaje} onChange={(e) => setMensaje(e.target.value)}
                hint={`${mensaje.length}/2000`} />
              {uso.data && <p className="text-xs text-ink-500">Te quedan {uso.data.restantes} de {uso.data.limitePostulacionesMes} postulaciones este mes (plan {uso.data.plan}).</p>}
              <Button full icon={<Send className="h-4 w-4" />} loading={enviando} disabled={!!sinCupoMensual} onClick={postular}>Enviar postulación</Button>
              {sinCupoMensual && <p className="text-xs text-red-600">Alcanzaste el límite mensual de tu plan.{!uso.data?.premium && <> <Link to="/pyme/suscripcion" className="font-semibold underline">Pásate a Premium</Link>.</>}</p>}
            </div>
          )}
        </Card>
      </div>
    </>
  )
}
