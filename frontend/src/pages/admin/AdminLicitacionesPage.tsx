import { useEffect, useState } from "react";
import { listarTodasLicitaciones } from "../../api/admin";
import { obtenerMensajeError } from "../../api/client";
import type { LicitacionResponse } from "../../types";
import Card from "../../components/ui/Card";
import Badge, { badgeEstadoLicitacion } from "../../components/ui/Badge";
import Spinner from "../../components/ui/Spinner";
import Alert from "../../components/ui/Alert";
import { formatoFecha, formatoMoneda } from "../../utils/formato";

export default function AdminLicitacionesPage() {
  const [licitaciones, setLicitaciones] = useState<LicitacionResponse[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listarTodasLicitaciones()
      .then(setLicitaciones)
      .catch((err) => setError(obtenerMensajeError(err)))
      .finally(() => setCargando(false));
  }, []);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Todas las licitaciones</h1>
        <p className="text-sm text-slate-400">Vista de moderación de la plataforma completa.</p>
      </div>

      {error && <Alert tipo="error">{error}</Alert>}
      {cargando ? (
        <Spinner />
      ) : (
        <Card className="overflow-x-auto p-0">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-white/10 text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-4 py-3">Título</th>
                <th className="px-4 py-3">Empresa (id)</th>
                <th className="px-4 py-3">Rubro</th>
                <th className="px-4 py-3">Monto</th>
                <th className="px-4 py-3">Cierre</th>
                <th className="px-4 py-3">Estado</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {licitaciones.map((l) => (
                <tr key={l.id} className="text-slate-200">
                  <td className="px-4 py-3">{l.titulo}</td>
                  <td className="px-4 py-3 text-slate-400">#{l.empresaId}</td>
                  <td className="px-4 py-3">{l.rubro}</td>
                  <td className="px-4 py-3">{formatoMoneda(l.montoEstimado)}</td>
                  <td className="px-4 py-3">{formatoFecha(l.fechaCierre)}</td>
                  <td className="px-4 py-3">
                    <Badge tono={badgeEstadoLicitacion(l.estado)}>{l.estado}</Badge>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}
    </div>
  );
}
