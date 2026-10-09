import { Link, useSearchParams } from 'react-router-dom'
import { CheckCircle2, Clock, XCircle } from 'lucide-react'
import { Button, Logo, Spinner } from '@/components/ui'
import { ventasApi } from '@/api/services'
import { useAsync } from '@/hooks/useAsync'
import { useAuth } from '@/store/auth'
import { clp, fecha } from '@/utils/format'

/** Vuelta desde Webpay: el pago ya fue confirmado (o no) en el backend; aquí solo se muestra el resultado. */
export default function PagoResultado() {
  const [params] = useSearchParams()
  const estado = params.get('estado')
  const ventaId = Number(params.get('venta'))
  const autenticado = useAuth((s) => s.isAuthenticated())
  const venta = useAsync(() => (ventaId && autenticado ? ventasApi.venta(ventaId) : Promise.resolve(null)), [ventaId])
  const textos: Record<string, { icono: JSX.Element; titulo: string; texto: string }> = {
    aprobado: { icono: <CheckCircle2 className="mx-auto h-12 w-12 text-emerald-600" />, titulo: '¡Pago aprobado!', texto: 'Tu plan Premium ya está activo. Te enviamos la confirmación por correo.' },
    rechazado: { icono: <XCircle className="mx-auto h-12 w-12 text-red-500" />, titulo: 'Pago rechazado', texto: 'La transacción no fue autorizada. Puedes intentarlo nuevamente.' },
    anulado: { icono: <Clock className="mx-auto h-12 w-12 text-amber-500" />, titulo: 'Pago no completado', texto: 'Cancelaste el pago o expiró el tiempo. Tu compra quedó pendiente y puedes retomarla.' },
    error: { icono: <XCircle className="mx-auto h-12 w-12 text-red-500" />, titulo: 'No pudimos confirmar el pago', texto: 'Revisa el estado de tu plan en unos minutos.' },
  }
  const t = textos[estado ?? 'error'] ?? textos.error
  return (
    <div className="grid min-h-screen place-items-center bg-ink-50 px-4">
      <div className="card w-full max-w-md p-8 text-center">
        <Logo className="mb-6" />
        {t.icono}
        <h1 className="mt-4 text-2xl font-bold">{t.titulo}</h1>
        <p className="mt-2 text-sm text-ink-600">{t.texto}</p>
        {venta.loading ? <Spinner /> : venta.data && (
          <dl className="mt-6 space-y-1.5 rounded-xl bg-ink-50 p-4 text-left text-sm">
            <div className="flex justify-between"><dt className="text-ink-500">Plan</dt><dd className="font-semibold">{venta.data.plan}</dd></div>
            <div className="flex justify-between"><dt className="text-ink-500">Monto</dt><dd className="font-semibold">{clp(venta.data.monto)}</dd></div>
            <div className="flex justify-between"><dt className="text-ink-500">Fecha</dt><dd>{fecha(venta.data.fecha)}</dd></div>
            <div className="flex justify-between"><dt className="text-ink-500">Estado del pago</dt><dd>{venta.data.estadoPago}</dd></div>
            {venta.data.metodoPago && <div className="flex justify-between"><dt className="text-ink-500">Método</dt><dd>{venta.data.metodoPago}</dd></div>}
          </dl>
        )}
        <Link to={autenticado ? '/pyme/suscripcion' : '/login'} className="mt-6 block"><Button full>Ver mi plan</Button></Link>
        <p className="mt-4 text-xs text-ink-400">Webpay Plus en ambiente de pruebas (sandbox): no se realizan cobros reales.</p>
      </div>
    </div>
  )
}
