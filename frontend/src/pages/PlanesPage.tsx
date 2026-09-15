import { useEffect, useState } from "react";
import { iniciarVenta, miSuscripcion, obtenerPlanes } from "../api/ventas";
import { obtenerMensajeError } from "../api/client";
import { irAWebpay } from "../utils/webpay";
import type { PlanResponse, SuscripcionResponse } from "../types";
import Card from "../components/ui/Card";
import Button from "../components/ui/Button";
import Badge from "../components/ui/Badge";
import Spinner from "../components/ui/Spinner";
import Alert from "../components/ui/Alert";
import { formatoFecha, formatoMoneda } from "../utils/formato";

function limite(valor: number | null, unidad: string): string {
  return valor === null ? `${unidad} ilimitadas` : `${valor} ${unidad} / mes`;
}

export default function PlanesPage() {
  const [planes, setPlanes] = useState<PlanResponse[]>([]);
  const [suscripcion, setSuscripcion] = useState<SuscripcionResponse | null>(null);
  const [cargando, setCargando] = useState(true);
  const [contratando, setContratando] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([obtenerPlanes(), miSuscripcion()])
      .then(([p, s]) => {
        setPlanes(p);
        setSuscripcion(s);
      })
      .catch((err) => setError(obtenerMensajeError(err)))
      .finally(() => setCargando(false));
  }, []);

  async function handleContratar(planId: number) {
    setContratando(planId);
    setError(null);
    try {
      const { url, token } = await iniciarVenta(planId);
      irAWebpay(url, token);
    } catch (err) {
      setError(obtenerMensajeError(err, "No se pudo iniciar el pago."));
      setContratando(null);
    }
  }

  if (cargando) return <Spinner />;

  const planActivo = suscripcion?.estado === "ACTIVA" ? suscripcion.plan : null;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Planes de suscripción</h1>
        <p className="text-sm text-slate-400">Elige el plan que mejor se ajuste a tu volumen de operación.</p>
      </div>

      {planActivo && (
        <Alert tipo="success">
          Tu plan actual es <strong>{planActivo}</strong>
          {suscripcion?.fechaVencimiento ? ` — vigente hasta ${formatoFecha(suscripcion.fechaVencimiento)}.` : "."}
        </Alert>
      )}
      {error && <Alert tipo="error">{error}</Alert>}

      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
        {planes.map((plan) => {
          const esPremium = plan.nombre === "PREMIUM";
          const esActual = planActivo === plan.nombre;
          return (
            <Card
              key={plan.id}
              className={`relative flex flex-col ${esPremium ? "border-brand-500/40 shadow-glow" : ""}`}
            >
              {esPremium && (
                <span className="absolute -top-3 left-6 rounded-full bg-gradient-to-r from-brand-600 to-violet-600 px-3 py-1 text-xs font-bold text-white">
                  Recomendado
                </span>
              )}
              <div className="mb-4 flex items-center justify-between">
                <h2 className="text-lg font-bold text-white">{plan.nombre === "ESTANDAR" ? "Estándar" : "Premium"}</h2>
                {esActual && <Badge tono="green">Tu plan</Badge>}
              </div>
              <p className="mb-4 text-3xl font-extrabold text-white">
                {plan.precio === 0 ? (
                  "Gratis"
                ) : (
                  <>
                    {formatoMoneda(plan.precio)}
                    <span className="text-sm font-normal text-slate-400"> /mes</span>
                  </>
                )}
              </p>
              {plan.precio === 0 && (
                <p className="-mt-3 mb-4 text-xs text-slate-500">Incluido automáticamente, sin pago</p>
              )}
              <p className="mb-5 text-sm text-slate-400">{plan.descripcion}</p>
              <ul className="mb-6 flex-1 space-y-2 text-sm text-slate-300">
                <li className="flex items-center gap-2">✅ {limite(plan.limitePublicacionesMes, "publicaciones")}</li>
                <li className="flex items-center gap-2">✅ {limite(plan.limitePostulacionesMes, "postulaciones")}</li>
                <li className="flex items-center gap-2">{plan.soportePrioritario ? "✅" : "⬜"} Soporte prioritario</li>
                <li className="flex items-center gap-2">
                  {plan.notificacionesAutomaticas ? "✅" : "⬜"} Notificaciones automáticas
                </li>
              </ul>
              <Button
                variant={esPremium ? "primary" : "secondary"}
                className="w-full"
                disabled={esActual || plan.precio === 0}
                loading={contratando === plan.id}
                onClick={() => handleContratar(plan.id)}
              >
                {plan.precio === 0 ? "Plan gratuito" : esActual ? "Plan actual" : "Contratar con Webpay"}
              </Button>
            </Card>
          );
        })}
      </div>
    </div>
  );
}
