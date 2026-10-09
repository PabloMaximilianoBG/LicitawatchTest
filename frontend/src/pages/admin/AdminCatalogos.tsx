import { useState, type FormEvent } from 'react'
import { Plus } from 'lucide-react'
import { Button, Card, CardHeader, Input, PageHeader } from '@/components/ui'
import { adminUsuariosApi } from '@/api/services'
import { apiError } from '@/api/client'
import { invalidarCatalogos, useCatalogos } from '@/hooks/useCatalogos'
import { toast } from '@/store/toast'

/** Catálogo rubro (ER): el Administrador puede agregar rubros para los combobox. */
export default function AdminCatalogos() {
  const [version, setVersion] = useState(0)
  return <Contenido key={version} onCambio={() => { invalidarCatalogos(); setVersion((v) => v + 1) }} />
}

function Contenido({ onCambio }: { onCambio: () => void }) {
  const { rubros, regiones, tamanos } = useCatalogos()
  const [nombre, setNombre] = useState('')
  const [guardando, setGuardando] = useState(false)
  const agregar = async (e: FormEvent) => {
    e.preventDefault()
    if (!nombre.trim()) return
    setGuardando(true)
    try {
      await adminUsuariosApi.crearRubro(nombre.trim())
      toast.success('Rubro agregado')
      setNombre('')
      onCambio()
    } catch (x) { toast.error(apiError(x).mensaje) } finally { setGuardando(false) }
  }
  return (
    <>
      <PageHeader eyebrow="Administración" title="Rubros" subtitle="Catálogos que se muestran como combobox en los formularios." />
      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader title={`Rubros (${rubros.length})`} />
          <form onSubmit={agregar} className="flex gap-2 border-b border-ink-100 p-4">
            <Input aria-label="Nuevo rubro" placeholder="Nuevo rubro" value={nombre} maxLength={100} onChange={(e) => setNombre(e.target.value)} className="flex-1" />
            <Button type="submit" icon={<Plus className="h-4 w-4" />} loading={guardando}>Agregar</Button>
          </form>
          <ul className="divide-y divide-ink-100 text-sm">{rubros.map((r) => <li key={r.id} className="px-5 py-2.5">{r.nombre}</li>)}</ul>
        </Card>
        <Card>
          <CardHeader title="Otros catálogos" subtitle="Cargados desde la base de datos (no editables)." />
          <div className="space-y-2 p-5 text-sm text-ink-700">
            <p><b>{regiones.length}</b> regiones de Chile con sus comunas.</p>
            <p>Tamaños de empresa: {tamanos.map((t) => t.nombre).join(', ')}.</p>
          </div>
        </Card>
      </div>
    </>
  )
}
