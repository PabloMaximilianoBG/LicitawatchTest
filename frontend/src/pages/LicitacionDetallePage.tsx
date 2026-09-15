import { type FormEvent, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { obtenerLicitacion, postular } from "../api/licitaciones";
import { obtenerMensajeError } from "../api/client";
import { useAuthStore } from "../store/authStore";
import type { LicitacionResponse } from "../types";
import Card from "../components/ui/Card";
import Badge, { badgeEstadoLicitacion } from "../components/ui/Badge";
import Spinner from "../components/ui/Spinner";
import Alert from "../components/ui/Alert";
import Button from "../components/ui/Button";
import { formatoFecha, formatoMoneda } from "../utils/formato";

export default function LicitacionDetallePage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const usuario = useAuthStore((s) => s.usuario);
  const [licitacion, setLicitacion] = useState<LicitacionResponse | null>(null);
  const [propuesta, setPropuesta] = useState("");
  const [cargando, setCargando] = useState(true);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [exito, setExito] = useState(false);

  useEffect(() => {
    if (!id) return;
    obtenerLicitacion(Number(id))
      .then(setLicitacion)
      .catch((err) => setError(obtenerMensajeError(err, "No se encontró la licitación.")))
      .finally(() => setCargando(false));
  }, [id]);

  async function handlePostular(e: FormEvent) {
    e.preventDefault();
    if (!licitacion) return;
    setEnviando(true);
    setError(null);
    try {
      await postular(licitacion.id, propuesta);
      setExito(true);
      setPropuesta("");
    } catch (err) {
      setError(obtenerMensajeError(err, "No se pudo enviar tu postulación."));
    } finally {
      setEnviando(false);
    }
  }

  if (cargando) return <Spinner />;
  if (!licitacion) return <Alert tipo="error">{error ?? "Licitación no encontrada."}</Alert>;

  const esDueno = usuario?.rol === "EMPRESA" && usuario.id === licitacion.empresaId;
  const puedePostular = usuario?.rol === "CLIENTE" && licitacion.estado === "PUBLICADA";

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <button onClick={() => navigate(-1)} className="text-sm text-slate-400 hover:text-slate-200">
        ← Volver
      </button>

      <Card>
        <div className="mb-4 flex items-start justify-between gap-3">
          <h1 className="text-2xl font-bold text-white">{licitacion.titulo}</h1>
          <Badge tono={badgeEstadoLicitacion(licitacion.estado)}>{licitacion.estado}</Badge>
        </div>
        <dl className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <dt className="text-slate-500">Rubro</dt>
            <dd className="font-medium text-slate-200">{licitacion.rubro}</dd>
          </div>
          <div>
            <dt className="text-slate-500">Región</dt>
            <dd className="font-medium text-slate-200">{licitacion.region}</dd>
          </div>
          <div>
            <dt className="text-slate-500">Monto estimado</dt>
            <dd className="font-medium text-slate-200">{formatoMoneda(licitacion.montoEstimado)}</dd>
          </div>
          <div>
            <dt className="text-slate-500">Fecha de cierre</dt>
            <dd className="font-medium text-slate-200">{formatoFecha(licitacion.fechaCierre)}</dd>
          </div>
        </dl>

        {esDueno && (
          <div className="mt-6 flex gap-3 border-t border-white/10 pt-4">
            <Link to={`/empresa/licitaciones/${licitacion.id}/postulaciones`}>
              <Button variant="secondary" size="sm">
                Ver postulaciones recibidas
              </Button>
            </Link>
            <Link to={`/empresa/licitaciones/${licitacion.id}/editar`}>
              <Button variant="ghost" size="sm">
                Editar
              </Button>
            </Link>
          </div>
        )}
      </Card>

      {puedePostular && (
        <Card>
          <h2 className="mb-3 font-bold text-white">Postular a esta licitación</h2>
          {error && (
            <div className="mb-3">
              <Alert tipo="error">{error}</Alert>
            </div>
          )}
          {exito ? (
            <Alert tipo="success">¡Tu postulación fue enviada! Puedes revisar su estado en "Mis postulaciones".</Alert>
          ) : (
            <form onSubmit={handlePostular} className="space-y-3">
              <textarea
                required
                minLength={10}
                maxLength={4000}
                value={propuesta}
                onChange={(e) => setPropuesta(e.target.value)}
                placeholder="Describe tu propuesta para esta licitación..."
                rows={5}
                className="w-full rounded-xl border border-white/10 bg-white/5 px-3.5 py-2.5 text-sm text-slate-100 placeholder:text-slate-500 outline-none focus:border-brand-400 focus:ring-2 focus:ring-brand-500/30"
              />
              <Button type="submit" loading={enviando}>
                Enviar postulación
              </Button>
            </form>
          )}
        </Card>
      )}
    </div>
  );
}
