import { useEffect, useState } from "react";
import { listarTodasVentas } from "../../api/admin";
import { obtenerMensajeError } from "../../api/client";
import type { VentaAdminResponse } from "../../types";
import Card from "../../components/ui/Card";
import Badge from "../../components/ui/Badge";
import Spinner from "../../components/ui/Spinner";
import Alert from "../../components/ui/Alert";
import { formatoFecha, formatoMoneda } from "../../utils/formato";

function badgeEstadoPago(estado: string) {
  if (estado === "APROBADO") return "green" as const;
  if (estado === "RECHAZADO") return "red" as const;
  return "amber" as const;
}

export default function AdminVentasPage() {
  const [ventas, setVentas] = useState<VentaAdminResponse[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listarTodasVentas()
      .then(setVentas)
      .catch((err) => setError(obtenerMensajeError(err)))
      .finally(() => setCargando(false));
  }, []);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Ventas y pagos</h1>
        <p className="text-sm text-slate-400">Historial de ventas de planes de suscripción.</p>
      </div>

      {error && <Alert tipo="error">{error}</Alert>}
      {cargando ? (
        <Spinner />
      ) : ventas.length === 0 ? (
        <Card className="text-center text-slate-400">Todavía no hay ventas registradas.</Card>
      ) : (
        <Card className="overflow-x-auto p-0">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-white/10 text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-4 py-3">Venta</th>
                <th className="px-4 py-3">Usuario (id)</th>
                <th className="px-4 py-3">Plan</th>
                <th className="px-4 py-3">Monto</th>
                <th className="px-4 py-3">Fecha</th>
                <th className="px-4 py-3">Estado del pago</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {ventas.map((v) => (
                <tr key={v.id} className="text-slate-200">
                  <td className="px-4 py-3">#{v.id}</td>
                  <td className="px-4 py-3 text-slate-400">#{v.usuarioId}</td>
                  <td className="px-4 py-3">{v.plan}</td>
                  <td className="px-4 py-3">{formatoMoneda(v.monto)}</td>
                  <td className="px-4 py-3">{formatoFecha(v.fecha)}</td>
                  <td className="px-4 py-3">
                    <Badge tono={badgeEstadoPago(v.estadoPago)}>{v.estadoPago}</Badge>
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
