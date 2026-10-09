import { useEffect, useRef, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, MessagesSquare, SendHorizonal } from 'lucide-react'
import { Button, Card, EmptyState, PageHeader, PremiumBadge, Spinner } from '@/components/ui'
import { chatApi } from '@/api/services'
import { apiError } from '@/api/client'
import { useAsync } from '@/hooks/useAsync'
import { toast } from '@/store/toast'
import type { MensajeChat } from '@/types'
import { cn, fecha, haceCuanto, hora } from '@/utils/format'

/** Chat privado Licitador–Pyme (solo texto, consulta periódica cada 4 s: versión MVP del ER). */
export default function Chat() {
  const { id } = useParams()
  const nav = useNavigate()
  const lista = useAsync(chatApi.mias)
  const actual = id ? Number(id) : null
  return (
    <>
      <PageHeader title="Chat" subtitle="Conversaciones privadas con la contraparte de cada licitación adjudicada." />
      <div className="grid gap-4 lg:grid-cols-[320px_1fr]">
        <Card className={cn('overflow-hidden', actual && 'hidden lg:block')}>
          {lista.loading ? <Spinner /> : !lista.data?.length ? (
            <EmptyState icon={<MessagesSquare className="h-6 w-6" />} title="Sin conversaciones"
              message="El chat se habilita cuando el Licitador aprueba (adjudica) una postulación." />
          ) : (
            <ul className="divide-y divide-ink-100">
              {lista.data.map((c) => (
                <li key={c.id}>
                  <button onClick={() => nav(`/chat/${c.id}`)} className={cn('w-full px-4 py-3 text-left hover:bg-ink-50', c.id === actual && 'bg-brand-50/60')}>
                    <div className="flex items-center gap-2">
                      <p className="min-w-0 flex-1 truncate font-semibold text-ink-900">{c.contraparteNombre}</p>
                      {c.noLeidos > 0 && <span className="grid h-5 min-w-5 place-items-center rounded-full bg-red-500 px-1.5 text-[10px] font-bold text-white">{c.noLeidos}</span>}
                    </div>
                    <p className="truncate text-xs text-ink-500">{c.licitacionTitulo}</p>
                    {c.ultimoMensaje && <p className="mt-1 truncate text-xs text-ink-400">{c.ultimoMensaje} · {haceCuanto(c.ultimoMensajeAt)}</p>}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </Card>
        {actual ? <Conversacion id={actual} onLeido={lista.reload} /> : (
          <Card className="hidden place-items-center p-10 text-sm text-ink-500 lg:grid">Selecciona una conversación</Card>
        )}
      </div>
    </>
  )
}

function Conversacion({ id, onLeido }: { id: number; onLeido: () => void }) {
  const conv = useAsync(() => chatApi.obtener(id), [id])
  const [mensajes, setMensajes] = useState<MensajeChat[]>([])
  const [texto, setTexto] = useState('')
  const [enviando, setEnviando] = useState(false)
  const ultimo = useRef(0)
  const fin = useRef<HTMLDivElement>(null)

  useEffect(() => {
    let vivo = true
    ultimo.current = 0
    setMensajes([])
    const cargar = async () => {
      try {
        const nuevos = await chatApi.mensajes(id, ultimo.current || undefined)
        if (!vivo || !nuevos.length) return
        ultimo.current = nuevos[nuevos.length - 1].id
        setMensajes((m) => [...m, ...nuevos.filter((n) => !m.some((x) => x.id === n.id))])
        onLeido()
      } catch { /* el siguiente ciclo reintenta */ }
    }
    cargar()
    const t = setInterval(cargar, 4000)
    return () => { vivo = false; clearInterval(t) }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])
  useEffect(() => { fin.current?.scrollIntoView({ behavior: 'smooth' }) }, [mensajes.length])

  const enviar = async (e: FormEvent) => {
    e.preventDefault()
    if (!texto.trim()) return
    setEnviando(true)
    try {
      const m = await chatApi.enviar(id, texto.trim())
      ultimo.current = Math.max(ultimo.current, m.id)
      setMensajes((x) => [...x, m])
      setTexto('')
    } catch (x) {
      toast.error(apiError(x).mensaje)
    } finally {
      setEnviando(false)
    }
  }

  if (conv.loading) return <Card><Spinner /></Card>
  if (conv.error || !conv.data) return <Card className="p-6 text-sm text-red-600">{conv.error?.mensaje}</Card>
  const c = conv.data
  return (
    <Card className="flex h-[calc(100vh-14rem)] min-h-[420px] flex-col overflow-hidden">
      <header className="flex items-center gap-3 border-b border-ink-100 px-4 py-3">
        <Link to="/chat" className="rounded-lg p-1 text-ink-500 hover:bg-ink-100 lg:hidden" aria-label="Volver"><ArrowLeft className="h-5 w-5" /></Link>
        <div className="min-w-0 flex-1">
          <p className="flex items-center gap-2 truncate font-semibold text-ink-900">{c.contraparteNombre}{c.pymePremium && c.contraparteNombre === c.pymeNombre && <PremiumBadge />}</p>
          <p className="truncate text-xs text-ink-500">Licitación: {c.licitacionTitulo} · desde {fecha(c.createdAt)}</p>
        </div>
      </header>
      <div className="scrollbar-thin flex-1 space-y-3 overflow-y-auto bg-ink-50/50 px-4 py-4" aria-live="polite">
        {mensajes.length === 0 && <p className="py-10 text-center text-sm text-ink-500">Escribe el primer mensaje para coordinar la licitación.</p>}
        {mensajes.map((m) => (
          <div key={m.id} className={cn('flex', m.propio && 'justify-end')}>
            <div className={cn('max-w-[80%] rounded-2xl px-4 py-2.5 text-sm', m.propio ? 'rounded-br-sm bg-brand-600 text-white' : 'rounded-bl-sm border border-ink-200 bg-white text-ink-800')}>
              <p className="whitespace-pre-wrap [overflow-wrap:anywhere]">{m.contenido}</p>
              <p className={cn('mt-1 text-right text-[10px]', m.propio ? 'text-white/70' : 'text-ink-400')}>{hora(m.enviadoAt)}{m.propio && (m.leido ? ' · leído' : '')}</p>
            </div>
          </div>
        ))}
        <div ref={fin} />
      </div>
      <form onSubmit={enviar} className="flex gap-2 border-t border-ink-100 p-3">
        <label htmlFor="mensaje" className="sr-only">Mensaje</label>
        <input id="mensaje" className="field" maxLength={2000} placeholder="Escribe un mensaje…" value={texto} onChange={(e) => setTexto(e.target.value)} />
        <Button type="submit" loading={enviando} disabled={!texto.trim()} icon={!enviando && <SendHorizonal className="h-4 w-4" />} aria-label="Enviar" />
      </form>
    </Card>
  )
}
