import { useEffect, useState } from "react";
import { misPostulaciones } from "../api/licitaciones";
import { obtenerMensajeError } from "../api/client";
import type { PostulacionResponse } from "../types";
import Card from "../components/ui/Card";
import Badge, { badgeEstadoPostulacion } from "../components/ui/Badge";
import Spinner from "../components/ui/Spinner";
import Alert from "../components/ui/Alert";
import { formatoFecha } from "../utils/formato";

export default function MisPostulacionesPage() {
  const [postulaciones, setPostulaciones] = useState<PostulacionResponse[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    misPostulaciones()
      .then(setPostulaciones)
      .catch((err) => setError(obtenerMensajeError(err)))
      .finally(() => setCargando(false));
  }, []);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Mis postulaciones</h1>
        <p className="text-sm text-slate-400">El estado de todas las licitaciones a las que has postulado.</p>
      </div>

      {error && <Alert tipo="error">{error}</Alert>}
      {cargando ? (
        <Spinner />
      ) : postulaciones.length === 0 ? (
        <Card className="text-center text-slate-400">Todavía no has postulado a ninguna licitación.</Card>
      ) : (
        <div className="space-y-3">
          {postulaciones.map((p) => (
            <Card key={p.id} className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <h3 className="font-bold text-white">{p.licitacionTitulo}</h3>
                <p className="text-sm text-slate-400">Postulada el {formatoFecha(p.fechaPostulacion)}</p>
                <p className="mt-1 line-clamp-2 max-w-lg text-sm text-slate-500">{p.propuesta}</p>
              </div>
              <Badge tono={badgeEstadoPostulacion(p.estado)}>{p.estado.replace("_", " ")}</Badge>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
