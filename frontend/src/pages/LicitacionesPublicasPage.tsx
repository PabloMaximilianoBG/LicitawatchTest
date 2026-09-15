import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { buscarLicitaciones } from "../api/licitaciones";
import { obtenerMensajeError } from "../api/client";
import type { LicitacionResponse } from "../types";
import Card from "../components/ui/Card";
import Input from "../components/ui/Input";
import Badge, { badgeEstadoLicitacion } from "../components/ui/Badge";
import Spinner from "../components/ui/Spinner";
import Alert from "../components/ui/Alert";
import { formatoFecha, formatoMoneda } from "../utils/formato";

export default function LicitacionesPublicasPage() {
  const [licitaciones, setLicitaciones] = useState<LicitacionResponse[]>([]);
  const [rubro, setRubro] = useState("");
  const [region, setRegion] = useState("");
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function cargar() {
    setCargando(true);
    setError(null);
    try {
      const data = await buscarLicitaciones({ rubro: rubro || undefined, region: region || undefined });
      setLicitaciones(data);
    } catch (err) {
      setError(obtenerMensajeError(err, "No se pudieron cargar las licitaciones."));
    } finally {
      setCargando(false);
    }
  }

  useEffect(() => {
    cargar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Licitaciones publicadas</h1>
        <p className="text-sm text-slate-400">Busca oportunidades activas por rubro y región.</p>
      </div>

      <Card className="flex flex-col gap-3 sm:flex-row sm:items-end">
        <div className="flex-1">
          <Input label="Rubro" placeholder="Tecnología, Construcción..." value={rubro} onChange={(e) => setRubro(e.target.value)} />
        </div>
        <div className="flex-1">
          <Input label="Región" placeholder="Metropolitana..." value={region} onChange={(e) => setRegion(e.target.value)} />
        </div>
        <button
          onClick={cargar}
          className="h-fit rounded-xl bg-gradient-to-r from-brand-600 to-violet-600 px-5 py-2.5 text-sm font-semibold text-white transition-opacity hover:opacity-90"
        >
          Buscar
        </button>
      </Card>

      {error && <Alert tipo="error">{error}</Alert>}
      {cargando ? (
        <Spinner />
      ) : licitaciones.length === 0 ? (
        <Card className="text-center text-slate-400">No hay licitaciones publicadas con esos filtros.</Card>
      ) : (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
          {licitaciones.map((l) => (
            <Link key={l.id} to={`/licitaciones/${l.id}`}>
              <Card className="h-full transition-transform hover:-translate-y-1 hover:border-brand-400/40">
                <div className="mb-3 flex items-start justify-between gap-2">
                  <h3 className="font-bold text-white">{l.titulo}</h3>
                  <Badge tono={badgeEstadoLicitacion(l.estado)}>{l.estado}</Badge>
                </div>
                <dl className="space-y-1.5 text-sm text-slate-400">
                  <div className="flex justify-between">
                    <dt>Rubro</dt>
                    <dd className="text-slate-200">{l.rubro}</dd>
                  </div>
                  <div className="flex justify-between">
                    <dt>Región</dt>
                    <dd className="text-slate-200">{l.region}</dd>
                  </div>
                  <div className="flex justify-between">
                    <dt>Monto est.</dt>
                    <dd className="text-slate-200">{formatoMoneda(l.montoEstimado)}</dd>
                  </div>
                  <div className="flex justify-between">
                    <dt>Cierra</dt>
                    <dd className="text-slate-200">{formatoFecha(l.fechaCierre)}</dd>
                  </div>
                </dl>
              </Card>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
