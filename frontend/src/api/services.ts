import { api } from './client'
import type {
  Catalogo, CatalogosLicitacion, Ciudad, Conversacion, DatosEmpresa, EstadoAsistente, IniciarPago, Licitacion,
  LicitacionInput, LoginResponse, Mensaje, MensajeAsistente, MensajeChat, MiSuscripcion, Notificacion, Page, Perfil,
  PerfilPublico, Plan, Postulacion, RegistroInput, RegistroResponse, RespuestaAsistente, ResumenVentas, Rol,
  SoporteRespuesta, SuscripcionAdmin, UsoPlan, Venta,
} from '@/types'

const d = <T,>(p: Promise<{ data: T }>) => p.then((r) => r.data)
const vacioANull = (v: unknown) => (v === '' || v === undefined ? null : v)

const empresa = (e: DatosEmpresa) => ({
  ...e, rubroId: vacioANull(e.rubroId), ciudadId: vacioANull(e.ciudadId),
  tamanoEmpresaId: vacioANull(e.tamanoEmpresaId), sitioWeb: vacioANull(e.sitioWeb),
})

// ------------------------------------------------------------------ Usuarios (MS.bff.usuarios)
export const authApi = {
  login: (email: string, password: string) => d<LoginResponse>(api.post('/api/auth/login', { email, password })),
  registrarLicitador: (r: RegistroInput) => d<RegistroResponse>(api.post('/api/auth/registro/licitador', empresa(r))),
  registrarPyme: (r: RegistroInput) => d<RegistroResponse>(api.post('/api/auth/registro/pyme', empresa(r))),
  confirmarCuenta: (token: string) => d<Mensaje>(api.post('/api/auth/confirmar-cuenta', { token })),
  reenviarConfirmacion: (email: string) => d<Mensaje>(api.post('/api/auth/reenviar-confirmacion', { email })),
  recuperarPassword: (email: string) => d<Mensaje>(api.post('/api/auth/recuperar-password', { email })),
  restablecerPassword: (token: string, password: string, confirmPassword: string) =>
    d<Mensaje>(api.post('/api/auth/restablecer-password', { token, password, confirmPassword })),
}

export interface PerfilInput extends Partial<DatosEmpresa> { nombre?: string; area?: string }

export const usuariosApi = {
  me: () => d<Perfil>(api.get('/api/usuarios/me')),
  actualizarMe: (p: PerfilInput) => d<Perfil>(api.put('/api/usuarios/me', perfilPayload(p))),
  licitador: (id: number) => d<PerfilPublico>(api.get(`/api/usuarios/licitadores/${id}`)),
  pyme: (id: number) => d<PerfilPublico>(api.get(`/api/usuarios/pymes/${id}`)),
}

const perfilPayload = (p: PerfilInput) => ({
  ...p, rubroId: vacioANull(p.rubroId), ciudadId: vacioANull(p.ciudadId), tamanoEmpresaId: vacioANull(p.tamanoEmpresaId),
  sitioWeb: vacioANull(p.sitioWeb),
})

export const catalogosApi = {
  rubros: () => d<Catalogo[]>(api.get('/api/catalogos/rubros')),
  regiones: () => d<Catalogo[]>(api.get('/api/catalogos/regiones')),
  ciudades: (regionId: number) => d<Ciudad[]>(api.get(`/api/catalogos/regiones/${regionId}/ciudades`)),
  tamanos: () => d<Catalogo[]>(api.get('/api/catalogos/tamanos-empresa')),
}

export interface AdminCrearUsuarioInput extends Partial<DatosEmpresa> {
  email: string; password: string; confirmPassword: string; rol: Rol; nombre?: string; area?: string
}
export interface CambiarRolInput extends Partial<DatosEmpresa> { rol: Rol; nombre?: string; area?: string }

export const adminUsuariosApi = {
  listar: (p: { rol?: string; activo?: string; q?: string; page?: number; size?: number }) =>
    d<Page<Perfil>>(api.get('/api/admin/usuarios', { params: { ...p, rol: p.rol || undefined, activo: p.activo || undefined, q: p.q || undefined } })),
  obtener: (id: number) => d<Perfil>(api.get(`/api/admin/usuarios/${id}`)),
  crear: (u: AdminCrearUsuarioInput) => d<Perfil>(api.post('/api/admin/usuarios', perfilPayload(u))),
  actualizarPerfil: (id: number, p: PerfilInput) => d<Perfil>(api.put(`/api/admin/usuarios/${id}/perfil`, perfilPayload(p))),
  cambiarEstado: (id: number, activo: boolean) => d<Perfil>(api.patch(`/api/admin/usuarios/${id}/estado`, { activo })),
  cambiarRol: (id: number, r: CambiarRolInput) => d<Perfil>(api.patch(`/api/admin/usuarios/${id}/rol`, perfilPayload(r))),
  crearRubro: (nombre: string) => d<Catalogo>(api.post('/api/admin/catalogos/rubros', { nombre })),
}

// ------------------------------------------------------------------ Licitaciones (MS.bff.licitaciones)
export interface FiltrosBusqueda { q?: string; rubroId?: number | ''; regionId?: number | ''; presupuestoMin?: number | ''; presupuestoMax?: number | ''; orden?: string; page?: number; size?: number }

const limpiar = (o: Record<string, unknown>) => Object.fromEntries(Object.entries(o).filter(([, v]) => v !== '' && v !== undefined && v !== null))

const licitacionPayload = (l: LicitacionInput) => ({
  ...l, rubroId: vacioANull(l.rubroId), regionId: vacioANull(l.regionId), presupuestoMin: vacioANull(l.presupuestoMin),
  presupuestoMax: vacioANull(l.presupuestoMax), maxPostulantes: vacioANull(l.maxPostulantes),
})

const archivo = (f: File) => {
  const fd = new FormData()
  fd.append('archivo', f)
  return fd
}

export const licitacionesApi = {
  buscar: (f: FiltrosBusqueda) => d<Page<Licitacion>>(api.get('/api/licitaciones', { params: limpiar({ ...f }) })),
  mias: () => d<Licitacion[]>(api.get('/api/licitaciones/mias')),
  catalogos: () => d<CatalogosLicitacion>(api.get('/api/licitaciones/catalogos')),
  obtener: (id: number) => d<Licitacion>(api.get(`/api/licitaciones/${id}`)),
  crear: (l: LicitacionInput) => d<Licitacion>(api.post('/api/licitaciones', licitacionPayload(l))),
  actualizar: (id: number, l: LicitacionInput) => d<Licitacion>(api.put(`/api/licitaciones/${id}`, licitacionPayload(l))),
  cerrar: (id: number) => d<Licitacion>(api.patch(`/api/licitaciones/${id}/cerrar`)),
  eliminar: (id: number) => api.delete(`/api/licitaciones/${id}`),
  subirImagen: (id: number, f: File) => d<Licitacion>(api.post(`/api/licitaciones/${id}/imagen`, archivo(f))),
  quitarImagen: (id: number) => d<Licitacion>(api.delete(`/api/licitaciones/${id}/imagen`)),
  subirDocumento: (id: number, f: File) => d<Licitacion>(api.post(`/api/licitaciones/${id}/archivo`, archivo(f))),
  quitarDocumento: (id: number) => d<Licitacion>(api.delete(`/api/licitaciones/${id}/archivo`)),
  postular: (id: number, mensaje: string) => d<Postulacion>(api.post(`/api/licitaciones/${id}/postulaciones`, { mensaje: mensaje || null })),
  postulantes: (id: number) => d<Postulacion[]>(api.get(`/api/licitaciones/${id}/postulaciones`)),
}

export const postulacionesApi = {
  mias: () => d<Postulacion[]>(api.get('/api/postulaciones/mias')),
  uso: () => d<UsoPlan>(api.get('/api/postulaciones/uso')),
  aprobar: (id: number) => d<Postulacion>(api.patch(`/api/postulaciones/${id}/aprobar`)),
  rechazar: (id: number) => d<Postulacion>(api.patch(`/api/postulaciones/${id}/rechazar`)),
}

export const adminLicitacionesApi = {
  listar: (p: { q?: string; estado?: string; rubroId?: number | ''; regionId?: number | ''; page?: number; size?: number }) =>
    d<Page<Licitacion>>(api.get('/api/admin/licitaciones', { params: limpiar({ ...p }) })),
  estado: (id: number, estado: 'Abierta' | 'Cerrada') => d<Licitacion>(api.patch(`/api/admin/licitaciones/${id}/estado`, { estado })),
  actualizar: (id: number, l: LicitacionInput) => d<Licitacion>(api.put(`/api/admin/licitaciones/${id}`, licitacionPayload(l))),
  eliminar: (id: number) => api.delete(`/api/admin/licitaciones/${id}`),
  postulaciones: (id: number) => d<Postulacion[]>(api.get(`/api/admin/licitaciones/${id}/postulaciones`)),
  todasLasPostulaciones: (p: { estado?: string; page?: number; size?: number }) =>
    d<Page<Postulacion>>(api.get('/api/admin/postulaciones', { params: limpiar({ ...p }) })),
}

// ------------------------------------------------------------------ Ventas (MS.bff.ventas)
export const ventasApi = {
  planes: () => d<Plan[]>(api.get('/api/planes')),
  miSuscripcion: () => d<MiSuscripcion>(api.get('/api/suscripciones/mi')),
  iniciarPremium: () => d<IniciarPago>(api.post('/api/ventas/premium')),
  misVentas: () => d<Venta[]>(api.get('/api/ventas/mias')),
  venta: (id: number) => d<Venta>(api.get(`/api/ventas/${id}`)),
}

export const adminVentasApi = {
  resumen: () => d<ResumenVentas>(api.get('/api/admin/ventas/resumen')),
  ventas: (page = 0, size = 20) => d<Page<Venta>>(api.get('/api/admin/ventas', { params: { page, size } })),
  suscripciones: (p: { estado?: string; plan?: string; page?: number; size?: number }) =>
    d<Page<SuscripcionAdmin>>(api.get('/api/admin/suscripciones', { params: limpiar({ ...p }) })),
  cancelar: (id: number) => d<SuscripcionAdmin>(api.patch(`/api/admin/suscripciones/${id}/cancelar`)),
}

// ------------------------------------------------------------------ Notificaciones (MS.bff.notificaciones)
export const notificacionesApi = {
  mias: (page = 0, size = 20) => d<Page<Notificacion>>(api.get('/api/notificaciones/mias', { params: { page, size } })),
  soporte: (asunto: string, mensaje: string) => d<SoporteRespuesta>(api.post('/api/soporte', { asunto, mensaje })),
  admin: (p: { tipo?: string; page?: number; size?: number }) =>
    d<Page<Notificacion>>(api.get('/api/admin/notificaciones', { params: limpiar({ ...p }) })),
}

// ------------------------------------------------------------------ Chat (MS.bff.chat)
export const chatApi = {
  abrir: (postulacionId: number) => d<Conversacion>(api.post('/api/chat/conversaciones', { postulacionId })),
  mias: () => d<Conversacion[]>(api.get('/api/chat/conversaciones')),
  porPostulacion: (postulacionId: number) => d<Conversacion>(api.get(`/api/chat/conversaciones/postulacion/${postulacionId}`)),
  obtener: (id: number) => d<Conversacion>(api.get(`/api/chat/conversaciones/${id}`)),
  mensajes: (id: number, despuesDe?: number) => d<MensajeChat[]>(api.get(`/api/chat/conversaciones/${id}/mensajes`, { params: limpiar({ despuesDe }) })),
  enviar: (id: number, contenido: string) => d<MensajeChat>(api.post(`/api/chat/conversaciones/${id}/mensajes`, { contenido })),
}

// ------------------------------------------------------------------ LicitAsist (MS.bff.asistente)
export const asistenteApi = {
  estado: () => d<EstadoAsistente>(api.get('/api/asistente/estado')),
  chat: (mensaje: string, historial: MensajeAsistente[], licitacionId?: number) =>
    d<RespuestaAsistente>(api.post('/api/asistente/chat', {
      mensaje,
      historial: historial.filter((m) => !m.error).slice(-12).map((m) => ({ rol: m.rol, contenido: m.contenido.slice(0, 6000) })),
      licitacionId: licitacionId ?? null,
    })),
}
