import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { cerrarLicitacion, misLicitaciones } from "../../api/licitaciones";
import { obtenerMensajeError } from "../../api/client";
import type { LicitacionResponse } from "../../types";
import Card from "../../components/ui/Card";
import Badge, { badgeEstadoLicitacion } from "../../components/ui/Badge";
import Spinner from "../../components/ui/Spinner";
import Alert from "../../components/ui/Alert";
import Button from "../../components/ui/Button";
import { formatoFecha, formatoMoneda } from "../../utils/formato";

export default function MisLicitacionesPage() {
  const [licitaciones, setLicitaciones] = useState<LicitacionResponse[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [cerrando, setCerrando] = useState<number | null>(null);

  async function cargar() {
    setCargando(true);
    try {
      setLicitaciones(await misLicitaciones());
    } catch (err) {
      setError(obtenerMensajeError(err));
    } finally {
      setCargando(false);
    }
  }

  useEffect(() => {
    cargar();
  }, []);

  async function handleCerrar(id: number) {
    setCerrando(id);
    try {
      await cerrarLicitacion(id);
      await cargar();
    } catch (err) {
      setError(obtenerMensajeError(err));
    } finally {
      setCerrando(null);
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">Mis licitaciones</h1>
          <p className="text-sm text-slate-400">Publicaciones activas y cerradas de tu empresa.</p>
        </div>
        <Link to="/empresa/licitaciones/nueva">
          <Button>+ Publicar licitación</Button>
        </Link>
      </div>

      {error && <Alert tipo="error">{error}</Alert>}
      {cargando ? (
        <Spinner />
      ) : licitaciones.length === 0 ? (
        <Card className="text-center text-slate-400">Aún no has publicado ninguna licitación.</Card>
      ) : (
        <div className="space-y-3">
          {licitaciones.map((l) => (
            <Card key={l.id} className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <div className="mb-1 flex items-center gap-2">
                  <h3 className="font-bold text-white">{l.titulo}</h3>
                  <Badge tono={badgeEstadoLicitacion(l.estado)}>{l.estado}</Badge>
                </div>
                <p className="text-sm text-slate-400">
                  {l.rubro} · {l.region} · {formatoMoneda(l.montoEstimado)} · Cierra {formatoFecha(l.fechaCierre)}
                </p>
              </div>
              <div className="flex gap-2">
                <Link to={`/empresa/licitaciones/${l.id}/postulaciones`}>
                  <Button variant="secondary" size="sm">
                    Postulaciones
                  </Button>
                </Link>
                {l.estado === "PUBLICADA" && (
                  <>
                    <Link to={`/empresa/licitaciones/${l.id}/editar`}>
                      <Button variant="ghost" size="sm">
                        Editar
                      </Button>
                    </Link>
                    <Button variant="danger" size="sm" loading={cerrando === l.id} onClick={() => handleCerrar(l.id)}>
                      Cerrar
                    </Button>
                  </>
                )}
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
