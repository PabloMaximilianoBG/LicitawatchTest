export type Rol = "EMPRESA" | "CLIENTE" | "ADMINISTRADOR";

export interface UsuarioResumen {
  id: number;
  email: string;
  rol: Rol;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  expiraEnSegundos: number;
  usuario: UsuarioResumen;
}

export interface PerfilResponse {
  id: number;
  email: string;
  rol: Rol;
  activo: boolean;
  razonSocial: string | null;
  rubro: string | null;
  nombreContacto: string | null;
  rut: string | null;
  area: string | null;
}

export interface UsuarioAdminResponse {
  id: number;
  email: string;
  rol: Rol;
  activo: boolean;
  creadoEn: string;
}

export type EstadoLicitacion = "PUBLICADA" | "CERRADA";

export interface LicitacionResponse {
  id: number;
  empresaId: number;
  titulo: string;
  rubro: string;
  montoEstimado: number | null;
  region: string;
  estado: EstadoLicitacion;
  fechaCierre: string;
}

export type EstadoPostulacion = "ENVIADA" | "EN_REVISION" | "ACEPTADA" | "RECHAZADA";

export interface PostulacionResponse {
  id: number;
  licitacionId: number;
  licitacionTitulo: string;
  clienteId: number;
  fechaPostulacion: string;
  propuesta: string;
  estado: EstadoPostulacion;
}

export type NombrePlan = "ESTANDAR" | "PREMIUM";

export interface PlanResponse {
  id: number;
  nombre: NombrePlan;
  descripcion: string;
  precio: number;
  limitePublicacionesMes: number | null;
  limitePostulacionesMes: number | null;
  soportePrioritario: boolean;
  notificacionesAutomaticas: boolean;
}

export interface IniciarVentaResponse {
  ventaId: number;
  token: string;
  url: string;
}

export interface PagoResponse {
  estadoPago: "PENDIENTE" | "APROBADO" | "RECHAZADO";
  estadoSuscripcion: "ACTIVA" | "VENCIDA";
  plan: NombrePlan | null;
  fechaVencimiento: string | null;
}

export interface SuscripcionResponse {
  usuarioId: number;
  plan: NombrePlan | null;
  estado: "ACTIVA" | "VENCIDA";
  fechaInicio: string | null;
  fechaVencimiento: string | null;
}

export interface VentaAdminResponse {
  id: number;
  usuarioId: number;
  plan: NombrePlan;
  monto: number;
  fecha: string;
  estadoPago: "PENDIENTE" | "APROBADO" | "RECHAZADO";
}

export interface ChatResponse {
  respuesta: string;
  usoDelUsuario: number;
}

export interface ErrorResponse {
  status: number;
  mensaje: string;
  path: string;
  timestamp: string;
  detalles: string[] | null;
}
