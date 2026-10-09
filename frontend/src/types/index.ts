/**
 * Tipos del frontend. Las interfaces "Fila*" son idénticas a las tablas del Modelo ER (snake_case -> camelCase);
 * las demás son las respuestas de la API (fila + catálogos resueltos), que es lo que consume la interfaz.
 */

// ============================================================ Catálogos del ER
export type Rol = 'LICITADOR' | 'PYME' | 'ADMINISTRADOR'
export type EstadoLicitacion = 'Abierta' | 'Cerrada' | 'Adjudicada'
export type EstadoPostulacion = 'Pendiente' | 'Aprobada' | 'Rechazada'
export type TipoArchivo = 'PDF' | 'DOCX' | 'XLSX' | 'Otro'
export type PlanNombre = 'Estándar' | 'Premium'
export type EstadoSuscripcion = 'Activa' | 'Vencida' | 'Cancelada'
export type EstadoPago = 'Aprobado' | 'Rechazado' | 'Pendiente'
export type MetodoPago = 'Crédito' | 'Débito'
export type TipoNotificacion = 'Licitación publicada' | 'Postulación recibida' | 'Postulación aprobada' | 'Postulación rechazada'
  | 'Pago confirmado' | 'Mensaje nuevo'
export type EstadoCuenta = 'ACTIVA' | 'PENDIENTE_CONFIRMACION' | 'DESACTIVADA'

export interface Catalogo { id: number; nombre: string }

// ============================================================ Tablas del ER (referencia 1:1)
export interface FilaUsuario { id: number; email: string; rolId: number; activo: boolean; createdAt: string }
export interface FilaAdministrador { id: number; usuarioId: number; nombre: string; area: string }
export interface FilaLicitador {
  id: number; usuarioId: number; razonSocial: string; rut: string; nombreContacto: string; emailContacto: string; telefono: string
  rubroId: number; ciudadId: number; descripcionEmpresa: string; sitioWeb: string | null; updatedAt: string
}
export interface FilaPyme extends FilaLicitador { tamanoEmpresaId: number }
export interface FilaCiudad { id: number; nombre: string; regionId: number }
export interface FilaLicitacion {
  id: number; licitadorId: number; titulo: string; descripcion: string; rubroId: number; regionId: number
  presupuestoMin: number | null; presupuestoMax: number | null; maxPostulantes: number | null; imagenUrl: string | null
  archivoUrl: string | null; archivoNombre: string | null; tipoArchivoId: number | null; fechaCierre: string
  estadoLicitacionId: number; createdAt: string
}
export interface FilaPostulacion {
  id: number; licitacionId: number; pymeId: number; mensaje: string | null; fechaPostulacion: string
  estadoPostulacionId: number; updatedAt: string
}
export interface FilaSuscripcion { id: number; usuarioId: number; planId: number; estadoSuscripcionId: number }
export interface FilaVenta { id: number; suscripcionId: number; monto: number; fecha: string }
export interface FilaPago { id: number; ventaId: number; idTransaccion: string; metodoPagoId: number; estadoPagoId: number }
export interface FilaNotificacion { id: number; usuarioId: number; tipoNotificacionId: number; canal: 'email'; createdAt: string }
export interface FilaConversacion { id: number; postulacionId: number; licitadorId: number; pymeId: number; createdAt: string }
export interface FilaMensaje { id: number; conversacionId: number; emisorId: number; contenido: string; enviadoAt: string; leido: boolean }

// ============================================================ Usuarios
export interface Ciudad { id: number; nombre: string; regionId: number; regionNombre: string }

/** usuario + perfil de su rol con los catálogos resueltos (GET /api/usuarios/me). */
export interface Perfil {
  usuarioId: number; email: string; rol: Rol; activo: boolean; estadoCuenta: EstadoCuenta; createdAt: string; perfilId: number | null
  razonSocial: string | null; rut: string | null; nombreContacto: string | null; emailContacto: string | null; telefono: string | null
  rubroId: number | null; rubroNombre: string | null; ciudadId: number | null; ciudadNombre: string | null
  regionId: number | null; regionNombre: string | null; tamanoEmpresaId: number | null; tamanoEmpresaNombre: string | null
  descripcionEmpresa: string | null; sitioWeb: string | null; updatedAt: string | null
  nombre: string | null; area: string | null; premium: boolean
}

export interface PerfilPublico {
  perfilId: number; rol: Rol; razonSocial: string; rut: string; nombreContacto: string; emailContacto: string; telefono: string
  rubroNombre: string; ciudadNombre: string; regionNombre: string; tamanoEmpresaNombre: string | null
  descripcionEmpresa: string; sitioWeb: string | null; premium: boolean
}

export interface LoginResponse { token: string; tipo: string; expiraEn: string; usuario: Perfil }
export interface RegistroResponse { usuario: Perfil; correoEnviado: boolean; mensaje: string }
export interface Mensaje { mensaje: string }

export interface DatosEmpresa {
  razonSocial: string; rut: string; nombreContacto: string; emailContacto: string; telefono: string
  rubroId: number | ''; ciudadId: number | ''; tamanoEmpresaId?: number | ''; descripcionEmpresa: string; sitioWeb: string
}
export interface RegistroInput extends DatosEmpresa { email: string; password: string; confirmPassword: string }

// ============================================================ Licitaciones
export interface Licitacion {
  id: number; licitadorId: number; licitadorNombre: string | null; titulo: string; descripcion: string
  rubroId: number; rubroNombre: string | null; regionId: number; regionNombre: string | null
  presupuestoMin: number | null; presupuestoMax: number | null; maxPostulantes: number | null
  imagenUrl: string | null; archivoUrl: string | null; archivoNombre: string | null; tipoArchivo: TipoArchivo | null
  fechaCierre: string; estado: EstadoLicitacion; createdAt: string; cantidadPostulaciones: number
  cuposDisponibles: number | null; diasParaCierre: number; disponibleParaPostular: boolean
  miPostulacionId: number | null; miPostulacionEstado: EstadoPostulacion | null
}

export interface LicitacionInput {
  titulo: string; descripcion: string; rubroId: number | ''; regionId: number | ''
  presupuestoMin: number | ''; presupuestoMax: number | ''; maxPostulantes: number | ''; fechaCierre: string
}

export interface Postulacion {
  id: number; licitacionId: number; licitacionTitulo: string; licitacionEstado: EstadoLicitacion; fechaCierre: string
  licitadorId: number; licitadorNombre: string | null; pymeId: number; pymeUsuarioId: number | null
  pymeRazonSocial: string | null; pymeRut: string | null; pymeRubro: string | null; pymeCiudad: string | null
  pymeRegion: string | null; pymeTamano: string | null; pymeContacto: string | null; pymeEmailContacto: string | null
  pymeTelefono: string | null; pymePremium: boolean; mensaje: string | null; fechaPostulacion: string
  estado: EstadoPostulacion; updatedAt: string; chatDisponible: boolean
}

export interface UsoPlan { plan: PlanNombre; premium: boolean; postulacionesMes: number; limitePostulacionesMes: number | null; restantes: number }
export interface CatalogosLicitacion { estadosLicitacion: EstadoLicitacion[]; estadosPostulacion: EstadoPostulacion[]; tiposArchivo: TipoArchivo[] }

// ============================================================ Ventas
export interface Plan {
  id: number; nombre: PlanNombre; precio: number; vigenciaDias: number | null; limitePostulacionesMes: number
  accesoLicitasist: boolean; prioridadVisibilidad: boolean; soporte: string; beneficios: string[]
}

export interface Venta {
  id: number; suscripcionId: number; usuarioId: number; clienteNombre: string | null; clienteEmail: string | null
  plan: PlanNombre; monto: number; fecha: string; estadoPago: EstadoPago; metodoPago: MetodoPago | null
  idTransaccion: string | null; suscripcionEstado: EstadoSuscripcion
}

export interface MiSuscripcion {
  suscripcionId: number | null; plan: PlanNombre; estado: EstadoSuscripcion; premium: boolean; fechaInicio: string | null
  fechaVencimiento: string | null; diasRestantes: number | null; limitePostulacionesMes: number; accesoLicitasist: boolean
  soporte: string; precioPremium: number; ventaPendiente: Venta | null; beneficios: string[]
}

export interface SuscripcionAdmin {
  id: number; usuarioId: number; clienteNombre: string | null; clienteEmail: string | null; plan: PlanNombre
  estado: EstadoSuscripcion; estadoVisible: string; fechaInicio: string | null; fechaVencimiento: string | null
}

export interface IniciarPago { ventaId: number; monto: number; token: string; url: string }
export interface ResumenVentas {
  totalRecaudado: number; pagosAprobados: number; pagosRechazados: number; ventasPendientes: number
  premiumActivas: number; estandarActivas: number
}

// ============================================================ Notificaciones y soporte
export interface Notificacion { id: number; usuarioId: number; usuarioEmail: string | null; tipo: TipoNotificacion; canal: string; createdAt: string }
export interface SoporteRespuesta { nivel: 'Estándar' | 'Prioritario'; mensaje: string }

// ============================================================ Chat
export interface Conversacion {
  id: number; postulacionId: number; licitacionId: number | null; licitacionTitulo: string | null
  licitadorId: number; licitadorNombre: string | null; pymeId: number; pymeNombre: string | null; pymePremium: boolean
  contraparteNombre: string | null; createdAt: string; ultimoMensaje: string | null; ultimoMensajeAt: string | null; noLeidos: number
}
export interface MensajeChat { id: number; conversacionId: number; emisorId: number; propio: boolean; contenido: string; enviadoAt: string; leido: boolean }

// ============================================================ LicitAsist
export interface EstadoAsistente { acceso: boolean; rol: Rol; plan: string | null; motivo: string; modelo: string; sugerencias: string[] }
export interface RespuestaAsistente { respuesta: string; modelo: string; rol: Rol; fuentes: string[]; generadoEn: string }
export interface MensajeAsistente { rol: 'user' | 'assistant'; contenido: string; fuentes?: string[]; fecha: string; error?: boolean }

// ============================================================ Comunes
export interface Page<T> { content: T[]; page: number; size: number; totalElements: number; totalPages: number }
export interface ApiError { status: number; codigo?: string; mensaje: string; errores?: Record<string, string> }
