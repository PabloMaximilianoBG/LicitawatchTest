import { useEffect, useRef, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, FileUp, ImageUp, Trash2 } from 'lucide-react'
import { Button, Card, ErrorState, Input, PageHeader, Select, Spinner, Textarea } from '@/components/ui'
import { adminLicitacionesApi, licitacionesApi } from '@/api/services'
import { apiError } from '@/api/client'
import { useCatalogos } from '@/hooks/useCatalogos'
import { useAuth } from '@/store/auth'
import { toast } from '@/store/toast'
import type { Licitacion, LicitacionInput } from '@/types'
import { hoyMas } from '@/utils/format'

const vacio: LicitacionInput = { titulo: '', descripcion: '', rubroId: '', regionId: '', presupuestoMin: '', presupuestoMax: '', maxPostulantes: '', fechaCierre: hoyMas(15) }

/** Publicar / editar (columnas de licitacion del ER). La imagen y el documento se suben y su URL se genera sola. */
export default function LicitacionForm() {
  const { id } = useParams()
  const editando = !!id
  const esAdmin = useAuth((s) => s.rol) === 'ADMINISTRADOR'
  const nav = useNavigate()
  const { rubros, regiones } = useCatalogos()
  const [f, setF] = useState<LicitacionInput>(vacio)
  const [lic, setLic] = useState<Licitacion | null>(null)
  const [imagen, setImagen] = useState<File | null>(null)
  const [documento, setDocumento] = useState<File | null>(null)
  const [errores, setErrores] = useState<Record<string, string>>({})
  const [cargando, setCargando] = useState(editando)
  const [error, setError] = useState<string | null>(null)
  const [guardando, setGuardando] = useState(false)
  const imgRef = useRef<HTMLInputElement>(null)
  const docRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    if (!id) return
    licitacionesApi.obtener(Number(id)).then((l) => {
      setLic(l)
      setF({ titulo: l.titulo, descripcion: l.descripcion, rubroId: l.rubroId, regionId: l.regionId, presupuestoMin: l.presupuestoMin ?? '',
        presupuestoMax: l.presupuestoMax ?? '', maxPostulantes: l.maxPostulantes ?? '', fechaCierre: l.fechaCierre })
    }).catch((e) => setError(apiError(e).mensaje)).finally(() => setCargando(false))
  }, [id])

  const set = <K extends keyof LicitacionInput>(k: K, v: LicitacionInput[K]) => setF((x) => ({ ...x, [k]: v }))

  const guardar = async (e: FormEvent) => {
    e.preventDefault()
    const err: Record<string, string> = {}
    if (f.titulo.trim().length < 5) err.titulo = 'Mínimo 5 caracteres'
    if (f.descripcion.trim().length < 20) err.descripcion = 'Mínimo 20 caracteres'
    if (!f.rubroId) err.rubroId = 'Selecciona un rubro'
    if (!f.regionId) err.regionId = 'Selecciona una región'
    if (!f.fechaCierre || (!editando && f.fechaCierre <= hoyMas(0))) err.fechaCierre = 'La fecha de cierre debe ser posterior a hoy'
    if (f.presupuestoMin !== '' && f.presupuestoMax !== '' && Number(f.presupuestoMin) > Number(f.presupuestoMax)) err.presupuestoMax = 'Debe ser mayor o igual al mínimo'
    setErrores(err)
    if (Object.keys(err).length) return
    setGuardando(true)
    try {
      let l = editando
        ? (esAdmin ? await adminLicitacionesApi.actualizar(Number(id), f) : await licitacionesApi.actualizar(Number(id), f))
        : await licitacionesApi.crear(f)
      if (imagen) l = await licitacionesApi.subirImagen(l.id, imagen)
      if (documento) l = await licitacionesApi.subirDocumento(l.id, documento)
      toast.success(editando ? 'Licitación actualizada' : 'Licitación publicada. Las Pymes Premium de tu rubro reciben una alerta por correo.')
      nav(esAdmin ? '/admin/licitaciones' : `/licitador/licitaciones/${l.id}`)
    } catch (x) {
      const a = apiError(x)
      toast.error(a.mensaje)
      if (a.errores) setErrores(a.errores)
    } finally {
      setGuardando(false)
    }
  }

  const quitar = async (tipo: 'imagen' | 'archivo') => {
    if (!lic) return
    try {
      setLic(tipo === 'imagen' ? await licitacionesApi.quitarImagen(lic.id) : await licitacionesApi.quitarDocumento(lic.id))
      toast.success('Archivo eliminado')
    } catch (x) {
      toast.error(apiError(x).mensaje)
    }
  }

  if (cargando) return <Spinner />
  if (error) return <ErrorState message={error} />
  return (
    <>
      <Link to={editando ? `/licitador/licitaciones/${id}` : '/licitador/licitaciones'} className="mb-4 inline-flex items-center gap-1 text-sm text-ink-500 hover:text-ink-900"><ArrowLeft className="h-4 w-4" />Volver</Link>
      <PageHeader eyebrow="Licitador" title={editando ? 'Editar licitación' : 'Publicar licitación'} subtitle="Define las condiciones y plazos. Publicar no tiene costo." />
      <form onSubmit={guardar} noValidate className="grid gap-6 lg:grid-cols-[1fr_340px]">
        <Card className="space-y-4 p-6">
          <Input label="Título" required maxLength={200} value={f.titulo} error={errores.titulo} onChange={(e) => set('titulo', e.target.value)} />
          <Textarea label="Descripción" required rows={7} maxLength={5000} value={f.descripcion} error={errores.descripcion}
            hint="Alcance, condiciones, requisitos y plazos de ejecución." onChange={(e) => set('descripcion', e.target.value)} />
          <div className="grid gap-4 sm:grid-cols-2">
            <Select label="Rubro" required value={String(f.rubroId)} error={errores.rubroId} placeholder="Selecciona un rubro"
              options={rubros.map((r) => ({ value: String(r.id), label: r.nombre }))} onChange={(e) => set('rubroId', e.target.value ? Number(e.target.value) : '')} />
            <Select label="Región" required value={String(f.regionId)} error={errores.regionId} placeholder="Selecciona una región"
              options={regiones.map((r) => ({ value: String(r.id), label: r.nombre }))} onChange={(e) => set('regionId', e.target.value ? Number(e.target.value) : '')} />
            <Input label="Presupuesto mínimo (CLP, opcional)" type="number" min={0} value={f.presupuestoMin} error={errores.presupuestoMin}
              onChange={(e) => set('presupuestoMin', e.target.value ? Number(e.target.value) : '')} />
            <Input label="Presupuesto máximo (CLP, opcional)" type="number" min={0} value={f.presupuestoMax} error={errores.presupuestoMax}
              onChange={(e) => set('presupuestoMax', e.target.value ? Number(e.target.value) : '')} />
            <Input label="Máximo de postulantes (opcional)" type="number" min={1} value={f.maxPostulantes} error={errores.maxPostulantes}
              hint="Al alcanzarlo, la licitación deja de estar disponible." onChange={(e) => set('maxPostulantes', e.target.value ? Number(e.target.value) : '')} />
            <Input label="Fecha de cierre" type="date" required min={hoyMas(1)} value={f.fechaCierre} error={errores.fechaCierre}
              hint="Se cierra sola al pasar esta fecha." onChange={(e) => set('fechaCierre', e.target.value)} />
          </div>
        </Card>
        <div className="space-y-4">
          <Card className="space-y-3 p-5">
            <p className="font-semibold text-ink-900">Imagen (opcional)</p>
            {lic?.imagenUrl && !imagen && (
              <div className="relative"><img src={lic.imagenUrl} alt="" className="h-32 w-full rounded-xl object-cover" />
                <button type="button" onClick={() => quitar('imagen')} className="absolute right-2 top-2 rounded-lg bg-white/90 p-1.5 text-red-600" aria-label="Quitar imagen"><Trash2 className="h-4 w-4" /></button></div>
            )}
            <input ref={imgRef} type="file" accept="image/png,image/jpeg,image/webp,image/gif" hidden onChange={(e) => setImagen(e.target.files?.[0] ?? null)} />
            <Button type="button" variant="secondary" full icon={<ImageUp className="h-4 w-4" />} onClick={() => imgRef.current?.click()}>
              {imagen ? imagen.name : 'Elegir imagen (máx. 5 MB)'}</Button>
          </Card>
          <Card className="space-y-3 p-5">
            <p className="font-semibold text-ink-900">Documento complementario (opcional)</p>
            {lic?.archivoUrl && !documento && (
              <div className="flex items-center gap-2 rounded-xl bg-ink-50 p-3 text-sm"><span className="min-w-0 flex-1 truncate">{lic.archivoNombre} ({lic.tipoArchivo})</span>
                <button type="button" onClick={() => quitar('archivo')} className="text-red-600" aria-label="Quitar documento"><Trash2 className="h-4 w-4" /></button></div>
            )}
            <input ref={docRef} type="file" accept=".pdf,.docx,.xlsx,.doc,.xls,.zip,.txt,.pptx" hidden onChange={(e) => setDocumento(e.target.files?.[0] ?? null)} />
            <Button type="button" variant="secondary" full icon={<FileUp className="h-4 w-4" />} onClick={() => docRef.current?.click()}>
              {documento ? documento.name : 'PDF, DOCX, XLSX u otro (máx. 10 MB)'}</Button>
          </Card>
          <Button type="submit" full size="lg" loading={guardando}>{editando ? 'Guardar cambios' : 'Publicar licitación'}</Button>
        </div>
      </form>
    </>
  )
}
