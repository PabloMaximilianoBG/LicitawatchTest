import { Building2, Globe, Mail, MapPin, Phone, User } from 'lucide-react'
import { Modal, PremiumBadge, Spinner } from '@/components/ui'
import { usuariosApi } from '@/api/services'
import { useAsync } from '@/hooks/useAsync'

/** Perfil de la Pyme postulante (con insignia Premium) o del Licitador. */
export function PerfilEmpresaModal({ tipo, id, onClose }: { tipo: 'pyme' | 'licitador'; id: number | null; onClose: () => void }) {
  const { data, loading } = useAsync(() => (id ? (tipo === 'pyme' ? usuariosApi.pyme(id) : usuariosApi.licitador(id)) : Promise.resolve(null)), [id, tipo])
  return (
    <Modal open={!!id} onClose={onClose} title="Perfil de la empresa" size="md">
      {loading || !data ? <Spinner /> : (
        <div className="space-y-4 text-sm">
          <div>
            <p className="flex flex-wrap items-center gap-2 text-lg font-semibold text-ink-900">{data.razonSocial}{data.premium && <PremiumBadge />}</p>
            <p className="text-ink-500">RUT {data.rut} · {data.rubroNombre}{data.tamanoEmpresaNombre ? ` · ${data.tamanoEmpresaNombre}` : ''}</p>
          </div>
          <p className="whitespace-pre-line text-ink-700">{data.descripcionEmpresa}</p>
          <ul className="space-y-2 text-ink-700">
            <li className="flex gap-2"><User className="h-4 w-4 text-ink-400" />{data.nombreContacto}</li>
            <li className="flex gap-2"><Mail className="h-4 w-4 text-ink-400" />{data.emailContacto}</li>
            <li className="flex gap-2"><Phone className="h-4 w-4 text-ink-400" />{data.telefono}</li>
            <li className="flex gap-2"><MapPin className="h-4 w-4 text-ink-400" />{data.ciudadNombre}, {data.regionNombre}</li>
            {data.sitioWeb && <li className="flex gap-2"><Globe className="h-4 w-4 text-ink-400" /><a href={data.sitioWeb} target="_blank" rel="noreferrer" className="text-brand-600 hover:underline">{data.sitioWeb}</a></li>}
            <li className="flex gap-2"><Building2 className="h-4 w-4 text-ink-400" />{tipo === 'pyme' ? 'Pyme' : 'Licitador'}</li>
          </ul>
        </div>
      )}
    </Modal>
  )
}
