import { useEffect, useRef, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { confirmarPago } from "../api/ventas";
import { obtenerMensajeError } from "../api/client";
import type { PagoResponse } from "../types";
import Card from "../components/ui/Card";
import Spinner from "../components/ui/Spinner";
import Alert from "../components/ui/Alert";
import Button from "../components/ui/Button";
import { formatoFecha } from "../utils/formato";

export default function PagoResultadoPage() {
  const [searchParams] = useSearchParams();
  const tokenWs = searchParams.get("token_ws");
  const cancelado = searchParams.get("cancelado");

  const [pago, setPago] = useState<PagoResponse | null>(null);
  const [cargando, setCargando] = useState(Boolean(tokenWs));
  const [error, setError] = useState<string | null>(null);
  // React 18 StrictMode (solo en desarrollo) invoca cada efecto dos veces a
  // proposito para detectar efectos no idempotentes. Confirmar un pago dos
  // veces casi al mismo tiempo puede hacer que Transbank rechace la segunda
  // llamada ("Transaction already locked by another process"), asi que este
  // ref evita que el segundo montaje repita la llamada de red.
  const yaConfirmado = useRef(false);

  useEffect(() => {
    if (!tokenWs || yaConfirmado.current) return;
    yaConfirmado.current = true;
    confirmarPago(tokenWs)
      .then(setPago)
      .catch((err) => setError(obtenerMensajeError(err, "No se pudo confirmar el pago.")))
      .finally(() => setCargando(false));
  }, [tokenWs]);

  return (
    <div className="mx-auto max-w-lg">
      <Card className="text-center">
        {cancelado ? (
          <>
            <div className="mb-3 text-4xl">✋</div>
            <h1 className="mb-1 text-xl font-bold text-white">Pago cancelado</h1>
            <p className="text-sm text-slate-400">Cancelaste el pago en Webpay antes de completarlo.</p>
          </>
        ) : cargando ? (
          <Spinner />
        ) : error ? (
          <Alert tipo="error">{error}</Alert>
        ) : pago?.estadoPago === "APROBADO" ? (
          <>
            <div className="mb-3 text-4xl">🎉</div>
            <h1 className="mb-1 text-xl font-bold text-white">¡Pago aprobado!</h1>
            <p className="text-sm text-slate-400">
              Tu plan <strong className="text-slate-200">{pago.plan}</strong> está activo hasta{" "}
              {formatoFecha(pago.fechaVencimiento)}.
            </p>
          </>
        ) : pago ? (
          <>
            <div className="mb-3 text-4xl">⚠️</div>
            <h1 className="mb-1 text-xl font-bold text-white">Pago rechazado</h1>
            <p className="text-sm text-slate-400">La pasarela de pago rechazó la transacción. Puedes intentarlo de nuevo.</p>
          </>
        ) : (
          <Alert tipo="info">No se recibió información de pago.</Alert>
        )}

        <Link to="/planes" className="mt-6 inline-block">
          <Button variant="secondary">Volver a planes</Button>
        </Link>
      </Card>
    </div>
  );
}
