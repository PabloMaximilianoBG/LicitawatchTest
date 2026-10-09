const clpFmt = new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP', maximumFractionDigits: 0 })
const numFmt = new Intl.NumberFormat('es-CL')

export const clp = (n?: number | null) => (n === undefined || n === null ? '—' : clpFmt.format(n))
export const num = (n?: number | null) => (n === undefined || n === null ? '—' : numFmt.format(n))

export function presupuesto(min?: number | null, max?: number | null) {
  if (min == null && max == null) return 'No informado'
  if (min != null && max != null) return min === max ? clp(min) : `${clp(min)} – ${clp(max)}`
  return min != null ? `Desde ${clp(min)}` : `Hasta ${clp(max)}`
}

/** "2026-10-20" → "20 oct 2026" (fechas sin zona horaria se toman como fecha local). */
export function fecha(iso?: string | null, conHora = false) {
  if (!iso) return '—'
  const d = iso.length === 10 ? new Date(`${iso}T00:00:00`) : new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleString('es-CL', { day: '2-digit', month: 'short', year: 'numeric', ...(conHora ? { hour: '2-digit', minute: '2-digit' } : {}) })
}

export function hora(iso?: string | null) {
  if (!iso) return ''
  return new Date(iso).toLocaleTimeString('es-CL', { hour: '2-digit', minute: '2-digit' })
}

export function haceCuanto(iso?: string | null) {
  if (!iso) return ''
  const seg = Math.round((Date.now() - new Date(iso).getTime()) / 1000)
  if (seg < 60) return 'hace un momento'
  if (seg < 3600) return `hace ${Math.floor(seg / 60)} min`
  if (seg < 86400) return `hace ${Math.floor(seg / 3600)} h`
  return `hace ${Math.floor(seg / 86400)} d`
}

export function hoyMas(dias: number) {
  const d = new Date()
  d.setDate(d.getDate() + dias)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

/** Validación de RUT chileno (módulo 11), igual que el backend. */
export function rutValido(rut: string) {
  const limpio = rut.replace(/[.\-\s]/g, '').toUpperCase()
  if (!/^\d{7,8}[\dK]$/.test(limpio)) return false
  const cuerpo = limpio.slice(0, -1)
  const dv = limpio.slice(-1)
  let s = 0, m = 2
  for (let i = cuerpo.length - 1; i >= 0; i--) { s += Number(cuerpo[i]) * m; m = m === 7 ? 2 : m + 1 }
  const r = 11 - (s % 11)
  return (r === 11 ? '0' : r === 10 ? 'K' : String(r)) === dv
}

/** Formatea mientras se escribe: 765432103 → 76.543.210-3 */
export function formatearRut(v: string) {
  const limpio = v.replace(/[^\dkK]/g, '').toUpperCase().slice(0, 9)
  if (limpio.length < 2) return limpio
  const cuerpo = limpio.slice(0, -1).replace(/\B(?=(\d{3})+(?!\d))/g, '.')
  return `${cuerpo}-${limpio.slice(-1)}`
}

export const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
export const TELEFONO_REGEX = /^\+?[0-9 ]{8,15}$/
export const passwordValida = (p: string) => p.length >= 8 && /[A-Za-zÁÉÍÓÚáéíóúÑñ]/.test(p) && /\d/.test(p)

export const cn = (...c: unknown[]) => c.filter((x): x is string => typeof x === 'string' && x.length > 0).join(' ')

export const etiquetaRol: Record<string, string> = { PYME: 'Pyme', LICITADOR: 'Licitador', ADMINISTRADOR: 'Administrador' }
export const etiquetaCuenta: Record<string, string> = {
  ACTIVA: 'Activa', PENDIENTE_CONFIRMACION: 'Sin confirmar', DESACTIVADA: 'Desactivada',
}
