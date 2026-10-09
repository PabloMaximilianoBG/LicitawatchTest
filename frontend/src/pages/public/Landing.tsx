import { Link } from 'react-router-dom'
import { Bot, Briefcase, CheckCircle2, Crown, MessagesSquare, Search, ShieldCheck } from 'lucide-react'
import { Button, Logo } from '@/components/ui'
import { rutaInicio, useAuth } from '@/store/auth'

/** Portada: el contenido viene de la PPT (problema, solución, perfiles y planes). */
export default function Landing() {
  const { isAuthenticated, rol } = useAuth()
  const autenticado = isAuthenticated()
  return (
    <div className="min-h-screen bg-ink-950 text-white">
      <div className="relative overflow-hidden">
        <div className="absolute inset-0 bg-grid-dark [background-size:34px_34px]" />
        <div className="absolute -top-40 left-1/2 h-[520px] w-[min(900px,100%)] -translate-x-1/2 rounded-full bg-brand-600/25 blur-3xl" />
        <header className="relative mx-auto flex max-w-6xl items-center justify-between px-4 py-5 sm:px-6">
          <Logo light />
          <nav className="flex items-center gap-2">
            <Link to="/planes" className="hidden rounded-lg px-3 py-2 text-sm text-ink-300 hover:text-white sm:block">Planes</Link>
            {autenticado
              ? <Link to={rutaInicio(rol)}><Button size="sm">Ir a mi cuenta</Button></Link>
              : <>
                <Link to="/login"><Button size="sm" variant="outline-light">Iniciar sesión</Button></Link>
                <Link to="/registro"><Button size="sm">Crear cuenta</Button></Link>
              </>}
          </nav>
        </header>
        <section className="relative mx-auto max-w-6xl px-4 pb-20 pt-14 text-center sm:px-6 sm:pt-20">
          <p className="text-sm font-semibold uppercase tracking-widest text-brand-300">Plataforma privada de gestión de licitaciones</p>
          <h1 className="mx-auto mt-4 max-w-3xl text-4xl font-bold leading-tight sm:text-5xl">
            Licitadores publican. Pymes postulan. Todo en un solo lugar.
          </h1>
          <p className="mx-auto mt-5 max-w-2xl text-ink-300">
            Las licitaciones privadas se gestionan hoy con planillas, correos y contactos. LicitaWatch las centraliza:
            el Licitador publica sin costo y las Pymes buscan, filtran y postulan.
          </p>
          <div className="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
            <Link to="/registro/licitador"><Button size="lg" icon={<Briefcase className="h-5 w-5" />}>Soy Licitador</Button></Link>
            <Link to="/registro/pyme"><Button size="lg" variant="outline-light" icon={<Search className="h-5 w-5" />}>Soy Pyme</Button></Link>
          </div>
        </section>
      </div>

      <section className="mx-auto grid max-w-6xl gap-4 px-4 pb-16 sm:grid-cols-2 sm:px-6 lg:grid-cols-4">
        {[
          { icon: Briefcase, t: 'Licitadores publican', d: 'Publican y administran sus licitaciones de principio a fin, sin costo.' },
          { icon: Search, t: 'Pymes postulan', d: 'Buscan, filtran y postulan a las licitaciones abiertas.' },
          { icon: MessagesSquare, t: 'Chat privado', d: 'Al adjudicar, Licitador y Pyme conversan para resolver consultas.' },
          { icon: Bot, t: 'LicitAsist', d: 'Asistente con IA que responde con datos reales de la plataforma.' },
        ].map(({ icon: Icon, t, d }) => (
          <div key={t} className="glass rounded-2xl p-6">
            <Icon className="h-6 w-6 text-brand-300" />
            <h3 className="mt-4 font-semibold">{t}</h3>
            <p className="mt-1.5 text-sm text-ink-300">{d}</p>
          </div>
        ))}
      </section>

      <section className="mx-auto max-w-6xl px-4 pb-20 sm:px-6">
        <div className="grid gap-4 lg:grid-cols-2">
          <div className="rounded-2xl border border-white/10 bg-white/5 p-7">
            <p className="text-sm font-semibold uppercase tracking-widest text-ink-300">Estándar · gratis</p>
            <ul className="mt-5 space-y-2.5 text-sm text-ink-200">
              {['Búsqueda y filtros de licitaciones sin límite', 'Hasta 3 postulaciones al mes', 'Chat privado con el Licitador al adjudicarle una licitación', 'Soporte estándar'].map((b) => (
                <li key={b} className="flex gap-2"><CheckCircle2 className="h-5 w-5 shrink-0 text-emerald-400" />{b}</li>))}
            </ul>
          </div>
          <div className="rounded-2xl bg-gradient-to-br from-brand-600 to-violet-700 p-7 shadow-glow">
            <p className="flex items-center gap-2 text-sm font-semibold uppercase tracking-widest"><Crown className="h-4 w-4" />Premium · de pago</p>
            <ul className="mt-5 space-y-2.5 text-sm">
              {['Todo lo de Estándar', 'Hasta 7 postulaciones al mes', 'Acceso a LicitAsist, el asistente con IA', 'Prioridad de visibilidad en resultados',
                'Alertas por correo de licitaciones de su rubro', 'Insignia Premium visible en su perfil', 'Soporte prioritario'].map((b) => (
                <li key={b} className="flex gap-2"><CheckCircle2 className="h-5 w-5 shrink-0" />{b}</li>))}
            </ul>
          </div>
        </div>
        <p className="mt-6 flex items-center justify-center gap-2 text-center text-sm text-ink-400">
          <ShieldCheck className="h-4 w-4" />El Licitador no paga: publica sus licitaciones sin costo.
        </p>
      </section>
      <footer className="border-t border-white/10 py-6 text-center text-xs text-ink-500">© {new Date().getFullYear()} LicitaWatch · Proyecto de título</footer>
    </div>
  )
}
