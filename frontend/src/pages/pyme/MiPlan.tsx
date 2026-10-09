import { useState } from 'react'
import { CheckCircle2, Clock, Crown, CreditCard, ShieldCheck } from 'lucide-react'
import { Button, Card, CardHeader, ErrorState, PageHeader, PremiumBadge, Spinner, StatusBadge, Table } from '@/components/ui'
import { ventasApi } from '@/api/services'
import { apiError } from '@/api/client'
import { useAsync } from '@/hooks/useAsync'
import { toast } from '@/store/toast'
import { clp, fecha } from '@/utils/format'

/** PPT: la Pyme elige su plan y administra su suscripción. Premium se paga con Webpay Plus (sandbox). */
export default function MiPlan() {
  const { data: s, loading, error, reload } = useAsync(ventasApi.miSuscripcion)
  const ventas = useAsync(ventasApi.misVentas)
  const [pagando, setPagando] = useState(false)

  const pagar = async () => {
    setPagando(true)
    try {
      const t = await ventasApi.iniciarPremium()
      // Webpay Plus: se envía token_ws por POST al formulario de Transbank
      const form = document.createElement('form')
      form.method = 'POST'
      form.action = t.url
      const input = document.createElement('input')
      input.type = 'hidden'
      input.name = 'token_ws'
      input.value = t.token
      form.appendChild(input)
      document.body.appendChild(form)
      form.submit()
    } catch (e) {
      toast.error(apiError(e).mensaje)
      setPagando(false)
    }
  }

  if (loading) return <Spinner />
  if (error || !s) return <ErrorState message={error?.mensaje} onRetry={reload} />
  return (
    <>
      <PageHeader eyebrow="Pyme" title="Mi plan" subtitle="El plan Estándar es gratis y viene por defecto. Premium amplía tus postulaciones y beneficios." />
      <div className="grid gap-6 lg:grid-cols-[1fr_380px]">
        <Card className="p-6">
          <div className="flex flex-wrap items-center gap-3">
            <p className="text-2xl font-bold">Plan {s.plan}</p>{s.premium ? <PremiumBadge /> : <StatusBadge estado="Estándar" />}
          </div>
          {s.premium && s.fechaVencimiento && (
            <p className="mt-2 text-sm text-ink-600">Vigente desde el {fecha(s.fechaInicio)} hasta el {fecha(s.fechaVencimiento)} ({s.diasRestantes} días restantes).</p>
          )}
          <ul className="mt-5 grid gap-2 text-sm sm:grid-cols-2">
            {s.beneficios.map((b) => <li key={b} className="flex gap-2"><CheckCircle2 className="h-5 w-5 shrink-0 text-emerald-600" />{b}</li>)}
          </ul>
          <p className="mt-5 flex items-center gap-2 text-sm text-ink-600"><ShieldCheck className="h-4 w-4" />Soporte {s.soporte.toLowerCase()}</p>
        </Card>
        {!s.premium && (
          <Card className="overflow-hidden">
            <div className="bg-ink-950 p-6 text-white">
              <p className="flex items-center gap-2 text-sm font-semibold uppercase tracking-widest text-violet-300"><Crown className="h-4 w-4" />Premium</p>
              <p className="mt-2 text-3xl font-bold">{clp(s.precioPremium)}</p>
              <ul className="mt-4 space-y-1.5 text-sm text-ink-200">
                {['Hasta 7 postulaciones al mes', 'Acceso a LicitAsist', 'Prioridad de visibilidad', 'Alertas por correo de tu rubro', 'Insignia Premium', 'Soporte prioritario'].map((b) => <li key={b}>· {b}</li>)}
              </ul>
            </div>
            <div className="space-y-3 p-5">
              {s.ventaPendiente && (
                <p className="flex items-start gap-2 rounded-xl bg-amber-50 p-3 text-xs text-amber-800"><Clock className="h-4 w-4 shrink-0" />
                  Tienes una compra pendiente de pago ({clp(s.ventaPendiente.monto)} del {fecha(s.ventaPendiente.fecha)}). Puedes retomarla.</p>
              )}
              <Button full size="lg" icon={<CreditCard className="h-5 w-5" />} loading={pagando} onClick={pagar}>
                {s.ventaPendiente ? 'Retomar pago con Webpay' : 'Pagar con Webpay'}
              </Button>
              <p className="text-center text-[11px] text-ink-400">Webpay Plus en ambiente de pruebas (sandbox): no se realizan cobros reales.</p>
            </div>
          </Card>
        )}
      </div>
      <Card className="mt-6">
        <CardHeader title="Historial de compras" subtitle="Venta y pago de cada contratación del plan Premium." />
        {ventas.loading ? <Spinner /> : (
          <Table rows={ventas.data ?? []} rowKey={(v) => v.id}
            empty={<p className="px-5 py-8 text-center text-sm text-ink-500">Aún no tienes compras.</p>}
            columns={[
              { key: 'f', header: 'Fecha', render: (v) => fecha(v.fecha) },
              { key: 'p', header: 'Plan', render: (v) => v.plan },
              { key: 'm', header: 'Monto', render: (v) => clp(v.monto) },
              { key: 'mp', header: 'Método', render: (v) => v.metodoPago ?? '—', hideOnMobile: true },
              { key: 'e', header: 'Pago', render: (v) => <StatusBadge estado={v.estadoPago} /> },
            ]} />
        )}
      </Card>
    </>
  )
}
