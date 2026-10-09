import { useEffect, useState, type FormEvent } from 'react'
import { Pencil } from 'lucide-react'
import { Button, Card, Input, PageHeader, PremiumBadge, StatusBadge } from '@/components/ui'
import { EmpresaCampos } from '@/components/EmpresaCampos'
import { usuariosApi } from '@/api/services'
import { apiError } from '@/api/client'
import { useAuth } from '@/store/auth'
import { toast } from '@/store/toast'
import type { DatosEmpresa, Perfil as TPerfil } from '@/types'
import { etiquetaRol, fecha } from '@/utils/format'
import { validarEmpresa } from '@/utils/validaciones'

export const empresaDe = (u: TPerfil): DatosEmpresa => ({
  razonSocial: u.razonSocial ?? '', rut: u.rut ?? '', nombreContacto: u.nombreContacto ?? '', emailContacto: u.emailContacto ?? '',
  telefono: u.telefono ?? '', rubroId: u.rubroId ?? '', ciudadId: u.ciudadId ?? '', tamanoEmpresaId: u.tamanoEmpresaId ?? '',
  descripcionEmpresa: u.descripcionEmpresa ?? '', sitioWeb: u.sitioWeb ?? '',
})

/** PPT: Licitadores y Pymes actualizan sus datos (el RUT y el correo de acceso no son editables). */
export default function Perfil() {
  const { usuario, setUsuario } = useAuth()
  const [editando, setEditando] = useState(false)
  const [empresa, setEmpresa] = useState<DatosEmpresa | null>(null)
  const [regionId, setRegionId] = useState<number | ''>('')
  const [admin, setAdmin] = useState({ nombre: '', area: '' })
  const [errores, setErrores] = useState<Record<string, string>>({})
  const [guardando, setGuardando] = useState(false)

  useEffect(() => { usuariosApi.me().then(setUsuario).catch(() => undefined) }, [setUsuario])
  useEffect(() => {
    if (!usuario) return
    setEmpresa(empresaDe(usuario))
    setRegionId(usuario.regionId ?? '')
    setAdmin({ nombre: usuario.nombre ?? '', area: usuario.area ?? '' })
  }, [usuario, editando])

  if (!usuario) return null
  const esAdmin = usuario.rol === 'ADMINISTRADOR'
  const esPyme = usuario.rol === 'PYME'

  const guardar = async (e: FormEvent) => {
    e.preventDefault()
    const err = esAdmin ? {} : validarEmpresa(empresa!, esPyme, false)
    if (esAdmin && !admin.nombre.trim()) Object.assign(err, { nombre: 'Obligatorio' })
    if (esAdmin && !admin.area.trim()) Object.assign(err, { area: 'Obligatorio' })
    setErrores(err)
    if (Object.keys(err).length) return
    setGuardando(true)
    try {
      const actualizado = await usuariosApi.actualizarMe(esAdmin ? admin : { ...empresa! })
      setUsuario(actualizado)
      setEditando(false)
      toast.success('Perfil actualizado')
    } catch (x) {
      const a = apiError(x)
      toast.error(a.mensaje)
      if (a.errores) setErrores(a.errores)
    } finally {
      setGuardando(false)
    }
  }

  return (
    <>
      <PageHeader title="Mi perfil" subtitle={`Última edición: ${fecha(usuario.updatedAt ?? usuario.createdAt, true)}`}
        actions={!editando && <Button variant="secondary" icon={<Pencil className="h-4 w-4" />} onClick={() => setEditando(true)}>Editar datos</Button>} />
      <div className="grid gap-6 lg:grid-cols-[320px_1fr]">
        <Card className="h-fit space-y-3 p-5 text-sm">
          <p className="text-lg font-semibold text-ink-900">{usuario.razonSocial ?? usuario.nombre}</p>
          <div className="flex flex-wrap gap-2"><StatusBadge estado={usuario.rol} />{usuario.premium && <PremiumBadge />}</div>
          <dl className="space-y-2 pt-2">
            <div><dt className="text-xs text-ink-500">Correo de acceso (no editable)</dt><dd className="font-medium">{usuario.email}</dd></div>
            {usuario.rut && <div><dt className="text-xs text-ink-500">RUT (no editable)</dt><dd className="font-medium">{usuario.rut}</dd></div>}
            <div><dt className="text-xs text-ink-500">Perfil</dt><dd className="font-medium">{etiquetaRol[usuario.rol]}</dd></div>
            <div><dt className="text-xs text-ink-500">Cuenta creada</dt><dd className="font-medium">{fecha(usuario.createdAt)}</dd></div>
          </dl>
        </Card>
        <Card className="p-6">
          {esAdmin ? (
            <form onSubmit={guardar} className="grid gap-4 sm:grid-cols-2">
              <Input label="Nombre" required disabled={!editando} value={admin.nombre} error={errores.nombre} onChange={(e) => setAdmin({ ...admin, nombre: e.target.value })} />
              <Input label="Área" required disabled={!editando} value={admin.area} error={errores.area} onChange={(e) => setAdmin({ ...admin, area: e.target.value })} />
              {editando && <Acciones guardando={guardando} onCancelar={() => setEditando(false)} />}
            </form>
          ) : empresa && (
            editando ? (
              <form onSubmit={guardar} className="space-y-5" noValidate>
                <EmpresaCampos valor={empresa} onChange={setEmpresa} regionId={regionId} onRegion={setRegionId} errores={errores} esPyme={esPyme} rutEditable={false} />
                <Acciones guardando={guardando} onCancelar={() => setEditando(false)} />
              </form>
            ) : (
              <dl className="grid gap-4 text-sm sm:grid-cols-2">
                {[['Razón social', usuario.razonSocial], ['Nombre de contacto', usuario.nombreContacto], ['Correo de contacto', usuario.emailContacto],
                  ['Teléfono', usuario.telefono], ['Rubro', usuario.rubroNombre], ['Ciudad', `${usuario.ciudadNombre}, ${usuario.regionNombre}`],
                  ...(esPyme ? [['Tamaño de la empresa', usuario.tamanoEmpresaNombre]] : []), ['Sitio web', usuario.sitioWeb ?? '—']].map(([k, v]) => (
                  <div key={k}><dt className="text-xs text-ink-500">{k}</dt><dd className="font-medium text-ink-900">{v}</dd></div>
                ))}
                <div className="sm:col-span-2"><dt className="text-xs text-ink-500">Descripción</dt><dd className="whitespace-pre-line text-ink-800">{usuario.descripcionEmpresa}</dd></div>
              </dl>
            )
          )}
        </Card>
      </div>
    </>
  )
}

function Acciones({ guardando, onCancelar }: { guardando: boolean; onCancelar: () => void }) {
  return (
    <div className="flex justify-end gap-2 sm:col-span-2">
      <Button type="button" variant="secondary" onClick={onCancelar}>Cancelar</Button>
      <Button type="submit" loading={guardando}>Guardar cambios</Button>
    </div>
  )
}
