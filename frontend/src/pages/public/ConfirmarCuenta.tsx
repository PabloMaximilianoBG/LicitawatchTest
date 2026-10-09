import { useEffect, useRef, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { CheckCircle2, XCircle } from 'lucide-react'
import { AuthLayout } from '@/components/layout/AuthLayout'
import { Button, Spinner } from '@/components/ui'
import { authApi } from '@/api/services'
import { apiError } from '@/api/client'

export default function ConfirmarCuenta() {
  const [params] = useSearchParams()
  const [estado, setEstado] = useState<{ ok: boolean; mensaje: string } | null>(null)
  const enviado = useRef(false)
  useEffect(() => {
    if (enviado.current) return
    enviado.current = true
    const token = params.get('token')
    if (!token) { setEstado({ ok: false, mensaje: 'El enlace no es válido.' }); return }
    authApi.confirmarCuenta(token).then((r) => setEstado({ ok: true, mensaje: r.mensaje }))
      .catch((e) => setEstado({ ok: false, mensaje: apiError(e).mensaje }))
  }, [params])
  return (
    <AuthLayout title="Confirmación de cuenta">
      {!estado ? <Spinner label="Confirmando tu cuenta…" /> : (
        <div className="card p-6 text-center">
          {estado.ok ? <CheckCircle2 className="mx-auto h-10 w-10 text-emerald-600" /> : <XCircle className="mx-auto h-10 w-10 text-red-500" />}
          <p className="mt-4 text-sm text-ink-700">{estado.mensaje}</p>
          <Link to="/login" className="mt-6 inline-block"><Button>Ir a iniciar sesión</Button></Link>
        </div>
      )}
    </AuthLayout>
  )
}
