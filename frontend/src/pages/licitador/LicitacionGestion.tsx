import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Check, Lock, MessagesSquare, Pencil, Trash2, X } from 'lucide-react'
import { Button, Card, CardHeader, ConfirmDialog, EmptyState, ErrorState, ForbiddenState, PageHeader, PremiumBadge, Spinner, StatusBadge } from '@/components/ui'
import { LicitacionResumen } from '@/components/LicitacionResumen'
import { PerfilEmpresaModal } from '@/components/PerfilEmpresaModal'
import { chatApi, licitacionesApi, postulacionesApi } from '@/api/services'
import { apiError } from '@/api/client'
import { useAsync } from '@/hooks/useAsync'
import { useAuth } from '@/store/auth'
import { toast } from '@/store/toast'
import type { Postulacion } from '@/types'
import { fecha } from '@/utils/format'

/** PPT: el Licitador edita/cierra, ve postulantes (Premium primero), aprueba o rechaza y adjudica; luego chatea con la Pyme. */
export default function LicitacionGestion() {
  const id = Number(useParams().id)
  const nav = useNavigate()
  const perfilId = useAuth((s) => s.usuario?.perfilId)
  const lic = useAsync(() => licitacionesApi.obtener(id), [id])
  const postulantes = useAsync(() => licitacionesApi.postulantes(id), [id])
  const [confirmar, setConfirmar] = useState<{ tipo: 'aprobar' | 'rechazar' | 'cerrar' | 'eliminar'; p?: Postulacion } | null>(null)
  const [procesando, setProcesando] = useState(false)
  const [perfil, setPerfil] = useState<number | null>(null)

  const ejecutar = async () => {
    if (!confirmar) return
    setProcesando(true)
    try {
      if (confirmar.tipo === 'aprobar') { await postulacionesApi.aprobar(confirmar.p!.id); toast.success('Licitación adjudicada. Ya puedes chatear con la Pyme.') }
      if (confirmar.tipo === 'rechazar') { await postulacionesApi.rechazar(confirmar.p!.id); toast.success('Postulación rechazada') }
      if (confirmar.tipo === 'cerrar') { await licitacionesApi.cerrar(id); toast.success('Licitación cerrada') }
      if (confirmar.tipo === 'eliminar') { await licitacionesApi.eliminar(id); toast.success('Licitación eliminada'); nav('/licitador/licitaciones'); return }
      lic.reload()
      postulantes.reload()
    } catch (e) {
      toast.error(apiError(e).mensaje)
    } finally {
      setProcesando(false)
      setConfirmar(null)
    }
  }

  const chatear = async (p: Postulacion) => {
    try {
      const c = await chatApi.abrir(p.id)
      nav(`/chat/${c.id}`)
    } catch (e) {
      toast.error(apiError(e).mensaje)
    }
  }

  if (lic.loading) return <Spinner />
  if (lic.error || !lic.data) return <ErrorState message={lic.error?.mensaje} onRetry={lic.reload} />
  const l = lic.data
  if (l.licitadorId !== perfilId) return <ForbiddenState message="Solo el Licitador que publicó esta licitación puede gestionarla." />
  const adjudicada = l.estado === 'Adjudicada'
  const textos = {
    aprobar: { t: 'Aprobar y adjudicar', m: `Se adjudicará la licitación a ${confirmar?.p?.pymeRazonSocial}. Las demás postulaciones pendientes se rechazarán y se notificará a todas las Pymes por correo.` },
    rechazar: { t: 'Rechazar postulación', m: `Se notificará a ${confirmar?.p?.pymeRazonSocial} por correo.` },
    cerrar: { t: 'Cerrar licitación', m: 'Ya no recibirá postulaciones. Podrás seguir revisando y adjudicando.' },
    eliminar: { t: 'Eliminar licitación', m: 'Se eliminará la licitación con sus postulaciones, archivos y chats. Esta acción no se puede deshacer.' },
  }
  return (
    <>
      <Link to="/licitador/licitaciones" className="mb-4 inline-flex items-center gap-1 text-sm text-ink-500 hover:text-ink-900"><ArrowLeft className="h-4 w-4" />Mis licitaciones</Link>
      <PageHeader title="Gestionar licitación" actions={<>
        {l.estado === 'Abierta' && <Link to={`/licitador/licitaciones/${id}/editar`}><Button variant="secondary" icon={<Pencil className="h-4 w-4" />}>Editar</Button></Link>}
        {l.estado === 'Abierta' && <Button variant="secondary" icon={<Lock className="h-4 w-4" />} onClick={() => setConfirmar({ tipo: 'cerrar' })}>Cerrar</Button>}
        <Button variant="danger" icon={<Trash2 className="h-4 w-4" />} onClick={() => setConfirmar({ tipo: 'eliminar' })}>Eliminar</Button>
      </>} />
      <div className="grid gap-6 xl:grid-cols-[1fr_1.1fr]">
        <LicitacionResumen l={l} />
        <Card>
          <CardHeader title={`Postulantes (${postulantes.data?.length ?? 0})`} subtitle="Las Pymes Premium aparecen primero." />
          {postulantes.loading ? <Spinner /> : postulantes.error ? <ErrorState message={postulantes.error.mensaje} onRetry={postulantes.reload} /> : !postulantes.data?.length ? <EmptyState title="Aún no hay postulaciones" /> : (
            <ul className="divide-y divide-ink-100">
              {postulantes.data.map((p) => (
                <li key={p.id} className="space-y-2 px-5 py-4">
                  <div className="flex flex-wrap items-center gap-2">
                    <button onClick={() => setPerfil(p.pymeId)} className="font-semibold text-ink-900 hover:text-brand-700 hover:underline">{p.pymeRazonSocial}</button>
                    {p.pymePremium && <PremiumBadge />}
                    <StatusBadge estado={p.estado} />
                  </div>
                  <p className="text-xs text-ink-500">{p.pymeRubro} · {p.pymeTamano} · {p.pymeCiudad}, {p.pymeRegion} · postuló el {fecha(p.fechaPostulacion)}</p>
                  {p.mensaje && <p className="whitespace-pre-line rounded-xl bg-ink-50 p-3 text-sm text-ink-700">{p.mensaje}</p>}
                  <div className="flex flex-wrap gap-2">
                    {p.estado === 'Pendiente' && !adjudicada && <>
                      <Button size="sm" variant="success" icon={<Check className="h-4 w-4" />} onClick={() => setConfirmar({ tipo: 'aprobar', p })}>Aprobar y adjudicar</Button>
                      <Button size="sm" variant="secondary" icon={<X className="h-4 w-4" />} onClick={() => setConfirmar({ tipo: 'rechazar', p })}>Rechazar</Button>
                    </>}
                    {p.chatDisponible && <Button size="sm" icon={<MessagesSquare className="h-4 w-4" />} onClick={() => chatear(p)}>Chatear</Button>}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </Card>
      </div>
      <ConfirmDialog open={!!confirmar} title={confirmar ? textos[confirmar.tipo].t : ''} message={confirmar ? textos[confirmar.tipo].m : ''}
        danger={confirmar?.tipo === 'eliminar' || confirmar?.tipo === 'rechazar'} loading={procesando} onConfirm={ejecutar} onClose={() => setConfirmar(null)} />
      <PerfilEmpresaModal tipo="pyme" id={perfil} onClose={() => setPerfil(null)} />
    </>
  )
}
