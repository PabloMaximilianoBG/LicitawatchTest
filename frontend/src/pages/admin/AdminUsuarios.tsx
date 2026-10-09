import { useState, type FormEvent } from 'react'
import { Pencil, Power, ShieldPlus, UserPlus } from 'lucide-react'
import { Button, Card, ConfirmDialog, ErrorState, Input, Modal, PageHeader, Pagination, PremiumBadge, SearchBar, Select, Spinner, StatusBadge, Table } from '@/components/ui'
import { EmpresaCampos, empresaVacia } from '@/components/EmpresaCampos'
import { adminUsuariosApi } from '@/api/services'
import { apiError } from '@/api/client'
import { useAsync } from '@/hooks/useAsync'
import { useAuth } from '@/store/auth'
import { toast } from '@/store/toast'
import type { DatosEmpresa, Perfil, Rol } from '@/types'
import { fecha, passwordValida } from '@/utils/format'
import { validarEmpresa } from '@/utils/validaciones'
import { empresaDe } from '@/pages/compartidas/Perfil'

const ROLES = [{ value: 'LICITADOR', label: 'Licitador' }, { value: 'PYME', label: 'Pyme' }, { value: 'ADMINISTRADOR', label: 'Administrador' }]

/** PPT: el Administrador gestiona usuarios y roles (crear, editar, activar/desactivar, cambiar rol, dar Administrador). */
export default function AdminUsuarios() {
  const yo = useAuth((s) => s.usuario)
  const [f, setF] = useState({ q: '', rol: '', activo: '', page: 0 })
  const { data, loading, error, reload } = useAsync(() => adminUsuariosApi.listar({ ...f, size: 15 }), [JSON.stringify(f)])
  const [estado, setEstado] = useState<Perfil | null>(null)
  const [rol, setRol] = useState<Perfil | null>(null)
  const [editar, setEditar] = useState<Perfil | null>(null)
  const [crear, setCrear] = useState(false)
  const [procesando, setProcesando] = useState(false)

  const cambiarEstado = async () => {
    if (!estado) return
    setProcesando(true)
    try {
      await adminUsuariosApi.cambiarEstado(estado.usuarioId, estado.estadoCuenta !== 'ACTIVA')
      toast.success('Estado actualizado')
      reload()
    } catch (e) { toast.error(apiError(e).mensaje) } finally { setProcesando(false); setEstado(null) }
  }

  return (
    <>
      <PageHeader eyebrow="Administración" title="Usuarios y roles" actions={<Button icon={<UserPlus className="h-4 w-4" />} onClick={() => setCrear(true)}>Crear usuario</Button>} />
      <Card className="mb-4 grid gap-3 p-4 md:grid-cols-[2fr_1fr_1fr]">
        <SearchBar value={f.q} onSearch={(q) => setF({ ...f, q, page: 0 })} placeholder="Correo, razón social o nombre…" />
        <Select aria-label="Rol" value={f.rol} placeholder="Todos los roles" options={ROLES} onChange={(e) => setF({ ...f, rol: e.target.value, page: 0 })} />
        <Select aria-label="Estado" value={f.activo} placeholder="Todos los estados" options={[{ value: 'true', label: 'Activas' }, { value: 'false', label: 'Inactivas' }]}
          onChange={(e) => setF({ ...f, activo: e.target.value, page: 0 })} />
      </Card>
      <Card>
        {loading ? <Spinner /> : error ? <ErrorState message={error.mensaje} onRetry={reload} /> : (
          <>
            <Table rows={data?.content ?? []} rowKey={(u) => u.usuarioId} columns={[
              { key: 'n', header: 'Usuario', render: (u) => <div><p className="flex items-center gap-2 font-medium text-ink-900">{u.razonSocial ?? u.nombre ?? '—'}{u.premium && <PremiumBadge />}</p><p className="text-xs text-ink-500">{u.email}</p></div> },
              { key: 'r', header: 'Rol', render: (u) => <StatusBadge estado={u.rol} /> },
              { key: 'e', header: 'Cuenta', render: (u) => <StatusBadge estado={u.estadoCuenta} /> },
              { key: 'f', header: 'Creada', render: (u) => fecha(u.createdAt), hideOnMobile: true },
              { key: 'a', header: '', render: (u) => u.usuarioId === yo?.usuarioId ? <span className="text-xs text-ink-400">Tu cuenta</span> : (
                <div className="flex justify-end gap-1">
                  <Button size="sm" variant="ghost" icon={<Pencil className="h-4 w-4" />} onClick={() => setEditar(u)} aria-label="Editar perfil" />
                  <Button size="sm" variant="ghost" icon={<ShieldPlus className="h-4 w-4" />} onClick={() => setRol(u)} aria-label="Cambiar rol" />
                  <Button size="sm" variant="ghost" icon={<Power className="h-4 w-4" />} onClick={() => setEstado(u)} aria-label="Activar o desactivar" />
                </div>) },
            ]} />
            {data && <Pagination page={data.page} totalPages={data.totalPages} totalElements={data.totalElements} onChange={(p) => setF({ ...f, page: p })} />}
          </>
        )}
      </Card>
      <ConfirmDialog open={!!estado} title={estado?.estadoCuenta === 'ACTIVA' ? 'Desactivar cuenta' : 'Activar cuenta'} loading={procesando}
        message={estado?.estadoCuenta === 'ACTIVA' ? `${estado?.email} no podrá iniciar sesión.` : `${estado?.email} podrá volver a iniciar sesión.`}
        danger={estado?.estadoCuenta === 'ACTIVA'} onConfirm={cambiarEstado} onClose={() => setEstado(null)} />
      {rol && <CambiarRolModal usuario={rol} onClose={() => setRol(null)} onListo={() => { setRol(null); reload() }} />}
      {editar && <EditarPerfilModal usuario={editar} onClose={() => setEditar(null)} onListo={() => { setEditar(null); reload() }} />}
      {crear && <CrearUsuarioModal onClose={() => setCrear(false)} onListo={() => { setCrear(false); reload() }} />}
    </>
  )
}

function CamposRol({ rol, empresa, setEmpresa, regionId, setRegionId, admin, setAdmin, errores, rutEditable }: {
  rol: Rol; empresa: DatosEmpresa; setEmpresa: (e: DatosEmpresa) => void; regionId: number | ''; setRegionId: (r: number | '') => void
  admin: { nombre: string; area: string }; setAdmin: (a: { nombre: string; area: string }) => void; errores: Record<string, string>; rutEditable: boolean
}) {
  return rol === 'ADMINISTRADOR' ? (
    <div className="grid gap-4 sm:grid-cols-2">
      <Input label="Nombre" required value={admin.nombre} error={errores.nombre} onChange={(e) => setAdmin({ ...admin, nombre: e.target.value })} />
      <Input label="Área" required value={admin.area} error={errores.area} onChange={(e) => setAdmin({ ...admin, area: e.target.value })} />
    </div>
  ) : <EmpresaCampos valor={empresa} onChange={setEmpresa} regionId={regionId} onRegion={setRegionId} errores={errores} esPyme={rol === 'PYME'} rutEditable={rutEditable} />
}

function validar(rol: Rol, empresa: DatosEmpresa, admin: { nombre: string; area: string }, conRut: boolean) {
  if (rol === 'ADMINISTRADOR') {
    const e: Record<string, string> = {}
    if (!admin.nombre.trim()) e.nombre = 'Obligatorio'
    if (!admin.area.trim()) e.area = 'Obligatorio'
    return e
  }
  return validarEmpresa(empresa, rol === 'PYME', conRut)
}

function CambiarRolModal({ usuario, onClose, onListo }: { usuario: Perfil; onClose: () => void; onListo: () => void }) {
  const [rol, setRol] = useState<Rol>(usuario.rol === 'ADMINISTRADOR' ? 'LICITADOR' : 'ADMINISTRADOR')
  const [empresa, setEmpresa] = useState<DatosEmpresa>(usuario.razonSocial ? empresaDe(usuario) : empresaVacia)
  const [regionId, setRegionId] = useState<number | ''>(usuario.regionId ?? '')
  const [admin, setAdmin] = useState({ nombre: '', area: '' })
  const [errores, setErrores] = useState<Record<string, string>>({})
  const [guardando, setGuardando] = useState(false)
  const guardar = async () => {
    setGuardando(true)
    try {
      await adminUsuariosApi.cambiarRol(usuario.usuarioId, rol === 'ADMINISTRADOR' ? { rol, ...admin } : { rol, ...empresa })
      toast.success('Rol actualizado')
      onListo()
    } catch (e) {
      const a = apiError(e)
      // Si el usuario no tiene el perfil del nuevo rol, el backend pide sus datos
      if (a.codigo === 'VALIDACION') setErrores(validar(rol, empresa, admin, true))
      toast.error(a.mensaje)
    } finally { setGuardando(false) }
  }
  return (
    <Modal open onClose={onClose} title="Cambiar rol" description={`${usuario.email} · rol actual: ${usuario.rol}`} size="lg"
      footer={<><Button variant="secondary" onClick={onClose}>Cancelar</Button><Button loading={guardando} onClick={guardar}>Guardar</Button></>}>
      <div className="space-y-4">
        <Select label="Nuevo rol" value={rol} options={ROLES.filter((r) => r.value !== usuario.rol)} onChange={(e) => setRol(e.target.value as Rol)} />
        <p className="text-xs text-ink-500">Si la persona no tiene aún el perfil de ese rol, completa sus datos. Si ya lo tiene, se reutiliza.</p>
        <CamposRol rol={rol} empresa={empresa} setEmpresa={setEmpresa} regionId={regionId} setRegionId={setRegionId} admin={admin} setAdmin={setAdmin} errores={errores} rutEditable />
      </div>
    </Modal>
  )
}

function EditarPerfilModal({ usuario, onClose, onListo }: { usuario: Perfil; onClose: () => void; onListo: () => void }) {
  const [empresa, setEmpresa] = useState<DatosEmpresa>(empresaDe(usuario))
  const [regionId, setRegionId] = useState<number | ''>(usuario.regionId ?? '')
  const [admin, setAdmin] = useState({ nombre: usuario.nombre ?? '', area: usuario.area ?? '' })
  const [errores, setErrores] = useState<Record<string, string>>({})
  const [guardando, setGuardando] = useState(false)
  const guardar = async () => {
    const e = validar(usuario.rol, empresa, admin, false)
    setErrores(e)
    if (Object.keys(e).length) return
    setGuardando(true)
    try {
      await adminUsuariosApi.actualizarPerfil(usuario.usuarioId, usuario.rol === 'ADMINISTRADOR' ? admin : empresa)
      toast.success('Perfil actualizado')
      onListo()
    } catch (x) { toast.error(apiError(x).mensaje) } finally { setGuardando(false) }
  }
  return (
    <Modal open onClose={onClose} title="Editar perfil" description={usuario.email} size="lg"
      footer={<><Button variant="secondary" onClick={onClose}>Cancelar</Button><Button loading={guardando} onClick={guardar}>Guardar</Button></>}>
      <CamposRol rol={usuario.rol} empresa={empresa} setEmpresa={setEmpresa} regionId={regionId} setRegionId={setRegionId} admin={admin} setAdmin={setAdmin} errores={errores} rutEditable={false} />
    </Modal>
  )
}

function CrearUsuarioModal({ onClose, onListo }: { onClose: () => void; onListo: () => void }) {
  const [rol, setRol] = useState<Rol>('ADMINISTRADOR')
  const [cuenta, setCuenta] = useState({ email: '', password: '', confirmPassword: '' })
  const [empresa, setEmpresa] = useState<DatosEmpresa>(empresaVacia)
  const [regionId, setRegionId] = useState<number | ''>('')
  const [admin, setAdmin] = useState({ nombre: '', area: '' })
  const [errores, setErrores] = useState<Record<string, string>>({})
  const [guardando, setGuardando] = useState(false)
  const guardar = async (ev?: FormEvent) => {
    ev?.preventDefault()
    const e = validar(rol, empresa, admin, true)
    if (!cuenta.email.includes('@')) e.email = 'Correo inválido'
    if (!passwordValida(cuenta.password)) e.password = 'Mínimo 8 caracteres, con letras y números'
    if (cuenta.password !== cuenta.confirmPassword) e.confirmPassword = 'Las contraseñas no coinciden'
    setErrores(e)
    if (Object.keys(e).length) return
    setGuardando(true)
    try {
      await adminUsuariosApi.crear(rol === 'ADMINISTRADOR' ? { ...cuenta, rol, ...admin } : { ...cuenta, rol, ...empresa })
      toast.success('Usuario creado (cuenta activa)')
      onListo()
    } catch (x) {
      const a = apiError(x)
      toast.error(a.mensaje)
      if (a.errores) setErrores(a.errores)
    } finally { setGuardando(false) }
  }
  return (
    <Modal open onClose={onClose} title="Crear usuario" size="lg"
      footer={<><Button variant="secondary" onClick={onClose}>Cancelar</Button><Button loading={guardando} onClick={() => guardar()}>Crear</Button></>}>
      <form onSubmit={guardar} className="space-y-4" noValidate>
        <Select label="Rol" value={rol} options={ROLES} onChange={(e) => setRol(e.target.value as Rol)} />
        <div className="grid gap-4 sm:grid-cols-3">
          <Input label="Correo de acceso" type="email" required value={cuenta.email} error={errores.email} onChange={(e) => setCuenta({ ...cuenta, email: e.target.value })} />
          <Input label="Contraseña" type="password" required value={cuenta.password} error={errores.password} onChange={(e) => setCuenta({ ...cuenta, password: e.target.value })} />
          <Input label="Confirmar" type="password" required value={cuenta.confirmPassword} error={errores.confirmPassword} onChange={(e) => setCuenta({ ...cuenta, confirmPassword: e.target.value })} />
        </div>
        <CamposRol rol={rol} empresa={empresa} setEmpresa={setEmpresa} regionId={regionId} setRegionId={setRegionId} admin={admin} setAdmin={setAdmin} errores={errores} rutEditable />
      </form>
    </Modal>
  )
}
