import type { DatosEmpresa } from '@/types'
import { EMAIL_REGEX, TELEFONO_REGEX, rutValido } from './format'

/** Mismas reglas que valida MS.usuarios.bs (el backend vuelve a validar). */
export function validarEmpresa(e: DatosEmpresa, esPyme: boolean, conRut = true) {
  const err: Record<string, string> = {}
  if (!e.razonSocial.trim()) err.razonSocial = 'La razón social es obligatoria'
  if (conRut && !rutValido(e.rut)) err.rut = 'RUT inválido'
  if (!e.nombreContacto.trim()) err.nombreContacto = 'El nombre de contacto es obligatorio'
  if (!EMAIL_REGEX.test(e.emailContacto)) err.emailContacto = 'Correo de contacto inválido'
  if (!TELEFONO_REGEX.test(e.telefono)) err.telefono = 'Teléfono inválido'
  if (!e.rubroId) err.rubroId = 'Selecciona un rubro'
  if (!e.ciudadId) err.ciudadId = 'Selecciona una ciudad'
  if (esPyme && !e.tamanoEmpresaId) err.tamanoEmpresaId = 'Selecciona el tamaño'
  if (!e.descripcionEmpresa.trim()) err.descripcionEmpresa = 'La descripción es obligatoria'
  return err
}
