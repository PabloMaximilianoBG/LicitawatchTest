import { useState, type FormEvent } from 'react'
import { LifeBuoy } from 'lucide-react'
import { Button, Card, Input, PageHeader, Textarea } from '@/components/ui'
import { notificacionesApi } from '@/api/services'
import { apiError } from '@/api/client'
import { useAuth } from '@/store/auth'
import { toast } from '@/store/toast'

/** PPT diap. 7: soporte estándar (Estándar y Licitador) y soporte prioritario (Premium). */
export default function Soporte() {
  const premium = useAuth((s) => s.usuario?.premium)
  const [asunto, setAsunto] = useState('')
  const [mensaje, setMensaje] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [respuesta, setRespuesta] = useState<string | null>(null)
  const enviar = async (e: FormEvent) => {
    e.preventDefault()
    if (!asunto.trim() || !mensaje.trim()) { toast.error('Completa el asunto y el mensaje'); return }
    setEnviando(true)
    try {
      const r = await notificacionesApi.soporte(asunto.trim(), mensaje.trim())
      setRespuesta(r.mensaje)
      setAsunto('')
      setMensaje('')
    } catch (x) {
      toast.error(apiError(x).mensaje)
    } finally {
      setEnviando(false)
    }
  }
  return (
    <>
      <PageHeader title="Soporte" subtitle={premium ? 'Tu plan Premium incluye soporte prioritario.' : 'Soporte estándar: te respondemos a tu correo.'} />
      <Card className="max-w-2xl p-6">
        {respuesta ? (
          <div className="space-y-4 text-center">
            <LifeBuoy className="mx-auto h-10 w-10 text-brand-600" />
            <p className="text-sm text-ink-700">{respuesta}</p>
            <Button variant="secondary" onClick={() => setRespuesta(null)}>Enviar otra consulta</Button>
          </div>
        ) : (
          <form onSubmit={enviar} className="space-y-4">
            <Input label="Asunto" required maxLength={150} value={asunto} onChange={(e) => setAsunto(e.target.value)} />
            <Textarea label="¿En qué te ayudamos?" required rows={6} maxLength={4000} value={mensaje} onChange={(e) => setMensaje(e.target.value)} />
            <Button type="submit" loading={enviando}>Enviar a soporte</Button>
          </form>
        )}
      </Card>
    </>
  )
}
