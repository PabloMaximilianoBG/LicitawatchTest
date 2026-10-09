import { useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { CheckCircle2 } from 'lucide-react'
import { AuthLayout } from '@/components/layout/AuthLayout'
import { Button, Input } from '@/components/ui'
import { authApi } from '@/api/services'
import { apiError } from '@/api/client'
import { passwordValida } from '@/utils/format'

export default function RestablecerPassword() {
  const [params] = useSearchParams()
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [errores, setErrores] = useState<Record<string, string>>({})
  const [error, setError] = useState<string | null>(null)
  const [ok, setOk] = useState<string | null>(null)
  const [cargando, setCargando] = useState(false)
  const enviar = async (e: FormEvent) => {
    e.preventDefault()
    const err: Record<string, string> = {}
    if (!passwordValida(password)) err.password = 'Mínimo 8 caracteres, con al menos una letra y un número'
    if (password !== confirm) err.confirm = 'Las contraseñas no coinciden'
    setErrores(err)
    if (Object.keys(err).length) return
    setCargando(true)
    setError(null)
    try {
      setOk((await authApi.restablecerPassword(params.get('token') ?? '', password, confirm)).mensaje)
    } catch (x) {
      setError(apiError(x).mensaje)
    } finally {
      setCargando(false)
    }
  }
  return (
    <AuthLayout title="Nueva contraseña">
      {ok ? (
        <div className="card p-6 text-center">
          <CheckCircle2 className="mx-auto h-10 w-10 text-emerald-600" />
          <p className="mt-4 text-sm text-ink-700">{ok}</p>
          <Link to="/login" className="mt-6 inline-block"><Button>Iniciar sesión</Button></Link>
        </div>
      ) : (
        <form onSubmit={enviar} className="space-y-4" noValidate>
          <Input label="Nueva contraseña" type="password" autoComplete="new-password" value={password} error={errores.password}
            onChange={(e) => setPassword(e.target.value)} />
          <Input label="Confirmar contraseña" type="password" autoComplete="new-password" value={confirm} error={errores.confirm}
            onChange={(e) => setConfirm(e.target.value)} />
          {error && <div role="alert" className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}
            {' '}<Link to="/recuperar-password" className="font-semibold underline">Solicitar un nuevo enlace</Link></div>}
          <Button type="submit" full size="lg" loading={cargando}>Guardar contraseña</Button>
        </form>
      )}
    </AuthLayout>
  )
}
