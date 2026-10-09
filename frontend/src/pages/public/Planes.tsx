import { Link } from 'react-router-dom'
import { CheckCircle2, Crown } from 'lucide-react'
import { Button, ErrorState, Logo, Spinner } from '@/components/ui'
import { ventasApi } from '@/api/services'
import { useAsync } from '@/hooks/useAsync'
import { useAuth } from '@/store/auth'
import { clp, cn } from '@/utils/format'

/** PPT diap. 7: dos planes para las Pymes. El Licitador no paga. */
export default function Planes() {
  const { data, loading, error, reload } = useAsync(ventasApi.planes)
  const { isAuthenticated, rol } = useAuth()
  return (
    <div className="min-h-screen bg-ink-50">
      <header className="border-b border-ink-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-4"><Link to="/"><Logo /></Link>
          {!isAuthenticated() && <Link to="/login"><Button size="sm" variant="secondary">Iniciar sesión</Button></Link>}</div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-10">
        <h1 className="text-center text-3xl font-bold">Planes para Pymes</h1>
        <p className="mt-2 text-center text-ink-500">El Licitador no paga: publica sus licitaciones sin costo.</p>
        {loading && <Spinner />}
        {error && <ErrorState message={error.mensaje} onRetry={reload} />}
        <div className="mt-8 grid gap-5 md:grid-cols-2">
          {data?.map((p) => {
            const premium = p.nombre === 'Premium'
            return (
              <div key={p.id} className={cn('rounded-2xl p-7', premium ? 'bg-ink-950 text-white shadow-glow' : 'card')}>
                <p className={cn('flex items-center gap-2 text-sm font-semibold uppercase tracking-widest', premium ? 'text-violet-300' : 'text-ink-500')}>
                  {premium && <Crown className="h-4 w-4" />}{p.nombre} · {premium ? 'de pago' : 'gratis'}
                </p>
                <p className="mt-3 text-3xl font-bold">{premium ? clp(p.precio) : 'Gratis'}{premium && p.vigenciaDias && <span className="text-sm font-medium text-ink-300"> / {p.vigenciaDias} días</span>}</p>
                <ul className="mt-6 space-y-2.5 text-sm">
                  {p.beneficios.map((b) => <li key={b} className="flex gap-2"><CheckCircle2 className={cn('h-5 w-5 shrink-0', premium ? 'text-violet-300' : 'text-emerald-600')} />{b}</li>)}
                </ul>
                {premium && (
                  <Link to={isAuthenticated() && rol === 'PYME' ? '/pyme/suscripcion' : '/registro/pyme'} className="mt-7 block">
                    <Button full size="lg">{isAuthenticated() && rol === 'PYME' ? 'Contratar Premium' : 'Crear cuenta de Pyme'}</Button>
                  </Link>
                )}
              </div>
            )
          })}
        </div>
      </main>
    </div>
  )
}
