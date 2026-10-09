import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { Mail } from 'lucide-react'
import { AuthLayout } from '@/components/layout/AuthLayout'
import { Button, Input } from '@/components/ui'
import { authApi } from '@/api/services'
import { apiError } from '@/api/client'
import { EMAIL_REGEX } from '@/utils/format'

export default function RecuperarPassword() {
  const [email, setEmail] = useState('')
  const [mensaje, setMensaje] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [cargando, setCargando] = useState(false)
  const enviar = async (e: FormEvent) => {
    e.preventDefault()
    if (!EMAIL_REGEX.test(email)) { setError('Ingresa un correo válido'); return }
    setCargando(true)
    setError(null)
    try {
      setMensaje((await authApi.recuperarPassword(email.trim())).mensaje)
    } catch (x) {
      setError(apiError(x).mensaje)
    } finally {
      setCargando(false)
    }
  }
  return (
    <AuthLayout title="Recuperar contraseña" subtitle="Te enviaremos un enlace a tu correo para crear una nueva contraseña.">
      {mensaje ? (
        <div className="card p-6 text-center text-sm text-ink-700">
          {mensaje}
          <Link to="/login" className="mt-6 block"><Button full>Volver a iniciar sesión</Button></Link>
        </div>
      ) : (
        <form onSubmit={enviar} className="space-y-4" noValidate>
          <Input label="Correo de tu cuenta" type="email" icon={<Mail className="h-4 w-4" />} value={email} error={error ?? undefined}
            onChange={(e) => setEmail(e.target.value)} />
          <Button type="submit" full size="lg" loading={cargando}>Enviar enlace</Button>
          <p className="text-center text-sm"><Link to="/login" className="font-semibold text-brand-600 hover:underline">Volver</Link></p>
        </form>
      )}
    </AuthLayout>
  )
}
