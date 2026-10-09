import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { CheckCircle2 } from 'lucide-react'
import { Logo } from '@/components/ui'
import { cn } from '@/utils/format'

/** Layout de autenticación: panel de marca + formulario. En mobile solo el formulario. */
export function AuthLayout({ children, title, subtitle, ancho }: { children: ReactNode; title: string; subtitle?: ReactNode; ancho?: boolean }) {
  return (
    <div className="grid min-h-screen lg:grid-cols-[0.9fr_1.1fr]">
      <aside className="relative hidden overflow-hidden bg-ink-950 p-12 text-white lg:flex lg:flex-col">
        <div className="absolute inset-0 bg-grid-dark [background-size:34px_34px]" />
        <div className="absolute -left-24 -top-24 h-96 w-96 rounded-full bg-brand-600/30 blur-3xl" />
        <Link to="/" className="relative"><Logo light size="lg" /></Link>
        <div className="relative mt-auto max-w-md">
          <p className="text-sm font-semibold uppercase tracking-widest text-brand-300">Plataforma privada de licitaciones</p>
          <h2 className="mt-3 text-4xl font-bold leading-tight">Publica, encuentra y adjudica licitaciones en un solo lugar.</h2>
          <ul className="mt-8 space-y-3 text-ink-300">
            {['Licitadores publican sin costo', 'Pymes buscan, filtran y postulan', 'Chat privado al adjudicar', 'LicitAsist: asistente con IA'].map((t) => (
              <li key={t} className="flex items-center gap-3"><CheckCircle2 className="h-5 w-5 text-brand-400" />{t}</li>
            ))}
          </ul>
        </div>
        <p className="relative mt-10 text-xs text-ink-500">© {new Date().getFullYear()} LicitaWatch · Proyecto de título</p>
      </aside>
      <main className="flex items-start justify-center bg-white px-4 py-10 sm:px-8 lg:items-center">
        <div className={cn('w-full animate-fade-in', ancho ? 'max-w-2xl' : 'max-w-md')}>
          <Link to="/" className="mb-8 inline-block lg:hidden"><Logo /></Link>
          <h1 className="text-2xl font-bold tracking-tight text-ink-900 sm:text-3xl">{title}</h1>
          {subtitle && <div className="mt-2 text-sm text-ink-500">{subtitle}</div>}
          <div className="mt-8">{children}</div>
        </div>
      </main>
    </div>
  )
}
