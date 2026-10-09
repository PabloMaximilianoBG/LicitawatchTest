import { Link } from 'react-router-dom'
import { Briefcase, Search } from 'lucide-react'
import { AuthLayout } from '@/components/layout/AuthLayout'

/** PPT diap. 6: dos perfiles se registran (el Administrador pertenece a LicitaWatch). */
export default function RegistroSeleccion() {
  return (
    <AuthLayout title="Crea tu cuenta" subtitle={<>¿Ya tienes cuenta? <Link to="/login" className="font-semibold text-brand-600 hover:underline">Inicia sesión</Link></>}>
      <div className="grid gap-4">
        <Link to="/registro/licitador" className="card group flex gap-4 p-5 transition hover:border-brand-300 hover:shadow-glow">
          <div className="rounded-xl bg-indigo-50 p-3 text-indigo-600"><Briefcase className="h-6 w-6" /></div>
          <div>
            <p className="font-semibold text-ink-900">Soy Licitador</p>
            <p className="mt-1 text-sm text-ink-500">Publico y administro licitaciones. Defino condiciones y plazos, y reviso postulaciones. Sin costo.</p>
          </div>
        </Link>
        <Link to="/registro/pyme" className="card group flex gap-4 p-5 transition hover:border-brand-300 hover:shadow-glow">
          <div className="rounded-xl bg-brand-50 p-3 text-brand-600"><Search className="h-6 w-6" /></div>
          <div>
            <p className="font-semibold text-ink-900">Soy Pyme</p>
            <p className="mt-1 text-sm text-ink-500">Busco, reviso y postulo a licitaciones. Comienzo con el plan Estándar gratis.</p>
          </div>
        </Link>
      </div>
    </AuthLayout>
  )
}
