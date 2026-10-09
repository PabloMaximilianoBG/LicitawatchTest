import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { Lock, Mail } from 'lucide-react'
import { AuthLayout } from '@/components/layout/AuthLayout'
import { Button, Input } from '@/components/ui'
import { authApi } from '@/api/services'
import { apiError } from '@/api/client'
import { rutaInicio, useAuth } from '@/store/auth'
import { toast } from '@/store/toast'
import { EMAIL_REGEX } from '@/utils/format'

export default function Login() {
  const [params] = useSearchParams()
  const nav = useNavigate()
  const login = useAuth((s) => s.login)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [codigo, setCodigo] = useState<string | undefined>()
  const [cargando, setCargando] = useState(false)

  const enviar = async (e: FormEvent) => {
    e.preventDefault()
    if (!EMAIL_REGEX.test(email) || !password) { setError('Ingresa tu correo y contraseña'); return }
    setCargando(true)
    setError(null)
    try {
      const r = await authApi.login(email.trim(), password)
      login(r.token, r.expiraEn, r.usuario)
      toast.success('¡Bienvenido!')
      const destino = params.get('redirect')
      nav(destino && destino.startsWith('/') ? destino : rutaInicio(r.usuario.rol), { replace: true })
    } catch (err) {
      const a = apiError(err)
      setError(a.mensaje)
      setCodigo(a.codigo)
    } finally {
      setCargando(false)
    }
  }

  const reenviar = async () => {
    const r = await authApi.reenviarConfirmacion(email.trim())
    toast.info(r.mensaje)
  }

  return (
    <AuthLayout title="Iniciar sesión" subtitle={<>¿No tienes cuenta? <Link to="/registro" className="font-semibold text-brand-600 hover:underline">Regístrate</Link></>}>
      <form onSubmit={enviar} className="space-y-4" noValidate>
        <Input label="Correo" type="email" autoComplete="email" icon={<Mail className="h-4 w-4" />} value={email} onChange={(e) => setEmail(e.target.value)} />
        <Input label="Contraseña" type="password" autoComplete="current-password" icon={<Lock className="h-4 w-4" />} value={password}
          onChange={(e) => setPassword(e.target.value)} />
        <div className="flex justify-end">
          <Link to="/recuperar-password" className="text-sm font-medium text-brand-600 hover:underline">¿Olvidaste tu contraseña?</Link>
        </div>
        {error && (
          <div role="alert" className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
            {codigo === 'CUENTA_NO_CONFIRMADA' && (
              <button type="button" onClick={reenviar} className="mt-1 block font-semibold underline">Reenviar correo de confirmación</button>
            )}
          </div>
        )}
        <Button type="submit" full size="lg" loading={cargando}>Ingresar</Button>
      </form>
    </AuthLayout>
  )
}
