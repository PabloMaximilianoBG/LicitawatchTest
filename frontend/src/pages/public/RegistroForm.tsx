import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { MailCheck } from 'lucide-react'
import { AuthLayout } from '@/components/layout/AuthLayout'
import { Button, Input } from '@/components/ui'
import { EmpresaCampos, empresaVacia } from '@/components/EmpresaCampos'
import { authApi } from '@/api/services'
import { apiError } from '@/api/client'
import type { DatosEmpresa } from '@/types'
import { EMAIL_REGEX, passwordValida } from '@/utils/format'
import { validarEmpresa } from '@/utils/validaciones'

/** Registro (ER pág. 7): pide TODOS los campos de usuario + licitador/pyme. Combobox para rubro, región-ciudad y tamaño. */
export default function RegistroForm({ perfil }: { perfil: 'licitador' | 'pyme' }) {
  const esPyme = perfil === 'pyme'
  const [empresa, setEmpresa] = useState<DatosEmpresa>(empresaVacia)
  const [regionId, setRegionId] = useState<number | ''>('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [errores, setErrores] = useState<Record<string, string>>({})
  const [error, setError] = useState<string | null>(null)
  const [cargando, setCargando] = useState(false)
  const [listo, setListo] = useState<string | null>(null)

  const enviar = async (e: FormEvent) => {
    e.preventDefault()
    const err = validarEmpresa(empresa, esPyme)
    if (!EMAIL_REGEX.test(email)) err.email = 'Correo inválido'
    if (!passwordValida(password)) err.password = 'Mínimo 8 caracteres, con al menos una letra y un número'
    if (password !== confirmPassword) err.confirmPassword = 'Las contraseñas no coinciden'
    setErrores(err)
    if (Object.keys(err).length) return
    setCargando(true)
    setError(null)
    try {
      const datos = { ...empresa, email: email.trim(), password, confirmPassword }
      const r = esPyme ? await authApi.registrarPyme(datos) : await authApi.registrarLicitador(datos)
      setListo(r.mensaje)
    } catch (x) {
      const a = apiError(x)
      setError(a.mensaje)
      if (a.errores) setErrores(a.errores)
    } finally {
      setCargando(false)
    }
  }

  if (listo) {
    return (
      <AuthLayout title="Revisa tu correo">
        <div className="card p-6 text-center">
          <MailCheck className="mx-auto h-10 w-10 text-brand-600" />
          <p className="mt-4 text-sm text-ink-700">{listo}</p>
          <p className="mt-2 text-xs text-ink-500">Debes confirmar tu cuenta desde el enlace del correo antes de iniciar sesión.</p>
          <Link to="/login" className="mt-6 inline-block"><Button>Ir a iniciar sesión</Button></Link>
        </div>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout ancho title={esPyme ? 'Registro de Pyme' : 'Registro de Licitador'}
      subtitle={esPyme ? 'Comienzas con el plan Estándar (gratis).' : 'Publica tus licitaciones sin costo.'}>
      <form onSubmit={enviar} noValidate className="space-y-6">
        <section>
          <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-ink-500">Cuenta de acceso</h2>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Correo de acceso" type="email" required autoComplete="email" value={email} error={errores.email}
              onChange={(e) => setEmail(e.target.value)} className="sm:col-span-2" />
            <Input label="Contraseña" type="password" required autoComplete="new-password" value={password} error={errores.password}
              hint="Mínimo 8 caracteres, con letras y números" onChange={(e) => setPassword(e.target.value)} />
            <Input label="Confirmar contraseña" type="password" required autoComplete="new-password" value={confirmPassword}
              error={errores.confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} />
          </div>
        </section>
        <section>
          <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-ink-500">Datos de la empresa</h2>
          <EmpresaCampos valor={empresa} onChange={setEmpresa} regionId={regionId} onRegion={setRegionId} errores={errores} esPyme={esPyme} rutEditable />
        </section>
        {error && <div role="alert" className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}
        <Button type="submit" full size="lg" loading={cargando}>Crear cuenta</Button>
        <p className="text-center text-sm text-ink-500">¿Ya tienes cuenta? <Link to="/login" className="font-semibold text-brand-600 hover:underline">Inicia sesión</Link></p>
      </form>
    </AuthLayout>
  )
}
