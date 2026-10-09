import { Input, Select, Textarea } from '@/components/ui'
import { useCatalogos, useCiudades } from '@/hooks/useCatalogos'
import type { DatosEmpresa } from '@/types'
import { formatearRut } from '@/utils/format'

interface Props {
  valor: DatosEmpresa
  regionId: number | ''
  onRegion: (id: number | '') => void
  onChange: (v: DatosEmpresa) => void
  errores: Record<string, string>
  esPyme: boolean
  rutEditable: boolean
}

/** Campos de las tablas licitador / pyme del ER. Rubro, región-ciudad y tamaño son combobox cargados desde la BD. */
export function EmpresaCampos({ valor, regionId, onRegion, onChange, errores, esPyme, rutEditable }: Props) {
  const { rubros, regiones, tamanos } = useCatalogos()
  const ciudades = useCiudades(regionId)
  const set = <K extends keyof DatosEmpresa>(k: K, v: DatosEmpresa[K]) => onChange({ ...valor, [k]: v })
  return (
    <div className="grid gap-4 sm:grid-cols-2">
      <Input label="Razón social" required value={valor.razonSocial} error={errores.razonSocial} maxLength={200}
        onChange={(e) => set('razonSocial', e.target.value)} className="sm:col-span-2" />
      <Input label="RUT de la empresa" required value={valor.rut} error={errores.rut} disabled={!rutEditable}
        hint={rutEditable ? 'Ej: 76.543.210-3' : 'El RUT no es editable'} onChange={(e) => set('rut', formatearRut(e.target.value))} />
      <Input label="Teléfono" required value={valor.telefono} error={errores.telefono} placeholder="+56 9 1234 5678"
        onChange={(e) => set('telefono', e.target.value)} />
      <Input label="Nombre de contacto" required value={valor.nombreContacto} error={errores.nombreContacto} maxLength={150}
        onChange={(e) => set('nombreContacto', e.target.value)} />
      <Input label="Correo de contacto" type="email" required value={valor.emailContacto} error={errores.emailContacto}
        onChange={(e) => set('emailContacto', e.target.value)} />
      <Select label="Rubro" required value={String(valor.rubroId)} error={errores.rubroId} placeholder="Selecciona un rubro"
        options={rubros.map((r) => ({ value: String(r.id), label: r.nombre }))} onChange={(e) => set('rubroId', e.target.value ? Number(e.target.value) : '')} />
      {esPyme && (
        <Select label="Tamaño de la empresa" required value={String(valor.tamanoEmpresaId ?? '')} error={errores.tamanoEmpresaId}
          placeholder="Selecciona el tamaño" options={tamanos.map((t) => ({ value: String(t.id), label: t.nombre }))}
          onChange={(e) => set('tamanoEmpresaId', e.target.value ? Number(e.target.value) : '')} />
      )}
      <Select label="Región" required value={String(regionId)} placeholder="Selecciona una región"
        options={regiones.map((r) => ({ value: String(r.id), label: r.nombre }))}
        onChange={(e) => { onRegion(e.target.value ? Number(e.target.value) : ''); set('ciudadId', '') }} className={esPyme ? '' : ''} />
      <Select label="Ciudad / comuna" required value={String(valor.ciudadId)} error={errores.ciudadId} disabled={!regionId}
        placeholder={regionId ? 'Selecciona una ciudad' : 'Primero elige la región'} options={ciudades.map((c) => ({ value: String(c.id), label: c.nombre }))}
        onChange={(e) => set('ciudadId', e.target.value ? Number(e.target.value) : '')} />
      <Input label="Sitio web (opcional)" value={valor.sitioWeb} error={errores.sitioWeb} placeholder="https://"
        onChange={(e) => set('sitioWeb', e.target.value)} className="sm:col-span-2" />
      <Textarea label="Descripción de la empresa" required rows={3} value={valor.descripcionEmpresa} error={errores.descripcionEmpresa}
        maxLength={2000} onChange={(e) => set('descripcionEmpresa', e.target.value)} className="sm:col-span-2" />
    </div>
  )
}

export const empresaVacia: DatosEmpresa = {
  razonSocial: '', rut: '', nombreContacto: '', emailContacto: '', telefono: '', rubroId: '', ciudadId: '', tamanoEmpresaId: '',
  descripcionEmpresa: '', sitioWeb: '',
}
