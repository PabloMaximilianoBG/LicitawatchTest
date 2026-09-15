import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { actualizarEstadoPostulacion, obtenerLicitacion, postulacionesDeLicitacion } from "../../api/licitaciones";
import { obtenerMensajeError } from "../../api/client";
import type { EstadoPostulacion, LicitacionResponse, PostulacionResponse } from "../../types";
import Card from "../../components/ui/Card";
import Badge, { badgeEstadoPostulacion } from "../../components/ui/Badge";
import Spinner from "../../components/ui/Spinner";
import Alert from "../../components/ui/Alert";
import Button from "../../components/ui/Button";
import { formatoFecha } from "../../utils/formato";

const ACCIONES: { estado: EstadoPostulacion; etiqueta: string; variante: "primary" | "secondary" | "danger" }[] = [
  { estado: "EN_REVISION", etiqueta: "Marcar en revisión", variante: "secondary" },
  { estado: "ACEPTADA", etiqueta: "Aceptar", variante: "primary" },
  { estado: "RECHAZADA", etiqueta: "Rechazar", variante: "danger" },
];

export default function PostulacionesRecibidasPage() {
  const { id } = useParams<{ id: string }>();
  const [licitacion, setLicitacion] = useState<LicitacionResponse | null>(null);
  const [postulaciones, setPostulaciones] = useState<PostulacionResponse[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actualizando, setActualizando] = useState<number | null>(null);

  async function cargar() {
    if (!id) return;
    setCargando(true);
    try {
      const [lic, posts] = await Promise.all([obtenerLicitacion(Number(id)), postulacionesDeLicitacion(Number(id))]);
      setLicitacion(lic);
      setPostulaciones(posts);
    } catch (err) {
      setError(obtenerMensajeError(err));
    } finally {
      setCargando(false);
    }
  }

  useEffect(() => {
    cargar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  async function handleCambiarEstado(postulacionId: number, estado: EstadoPostulacion) {
    if (!id) return;
    setActualizando(postulacionId);
    try {
      await actualizarEstadoPostulacion(Number(id), postulacionId, estado);
      await cargar();
    } catch (err) {
      setError(obtenerMensajeError(err));
    } finally {
      setActualizando(null);
    }
  }

  if (cargando) return <Spinner />;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Postulaciones recibidas</h1>
        {licitacion && <p className="text-sm text-slate-400">Para la licitación "{licitacion.titulo}"</p>}
      </div>

      {error && <Alert tipo="error">{error}</Alert>}
      {postulaciones.length === 0 ? (
        <Card className="text-center text-slate-400">Esta licitación todavía no tiene postulaciones.</Card>
      ) : (
        <div className="space-y-3">
          {postulaciones.map((p) => (
            <Card key={p.id}>
              <div className="mb-2 flex items-start justify-between gap-3">
                <div>
                  <p className="text-sm text-slate-500">Postulada el {formatoFecha(p.fechaPostulacion)}</p>
                </div>
                <Badge tono={badgeEstadoPostulacion(p.estado)}>{p.estado.replace("_", " ")}</Badge>
              </div>
              <p className="mb-4 whitespace-pre-wrap text-sm text-slate-200">{p.propuesta}</p>
              <div className="flex flex-wrap gap-2">
                {ACCIONES.filter((a) => a.estado !== p.estado).map((a) => (
                  <Button
                    key={a.estado}
                    size="sm"
                    variant={a.variante}
                    loading={actualizando === p.id}
                    onClick={() => handleCambiarEstado(p.id, a.estado)}
                  >
                    {a.etiqueta}
                  </Button>
                ))}
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
