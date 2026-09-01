import { useEffect, useMemo, useState } from "react";
import { listarLicitaciones } from "../api/licitaciones";
import { listarCoincidencias } from "../api/coincidencias";
import { useUsuarioActivo } from "../context/UsuarioActivoContext";
import ScoreRing from "../components/ScoreRing";

const clp = (n) => "$" + Number(n ?? 0).toLocaleString("es-CL");

export default function Dashboard() {
  const { usuario } = useUsuarioActivo();
  const [licitaciones, setLicitaciones] = useState([]);
  const [coincidencias, setCoincidencias] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const [filtroRubro, setFiltroRubro] = useState("");
  const [busqueda, setBusqueda] = useState("");

  useEffect(() => {
    let activo = true;
    setCargando(true);
    Promise.all([listarLicitaciones(), listarCoincidencias(usuario.id)])
      .then(([lics, coincs]) => {
        if (!activo) return;
        setLicitaciones(lics);
        setCoincidencias(coincs);
      })
      .catch(() => activo && setError("No se pudieron cargar los datos. Revisa que ingesta-ms, coincidencias-ms y el gateway estén arriba."))
      .finally(() => activo && setCargando(false));
    return () => {
      activo = false;
    };
  }, [usuario.id]);

  const scoreDe = (licitacionId) => coincidencias.find((c) => c.licitacionId === licitacionId)?.score ?? 0;

  const rubros = useMemo(() => [...new Set(licitaciones.map((l) => l.rubro).filter(Boolean))], [licitaciones]);

  const filtradas = licitaciones
    .filter((l) => !filtroRubro || l.rubro === filtroRubro)
    .filter((l) => !busqueda || `${l.codigo ?? ""}`.toLowerCase().includes(busqueda.toLowerCase()))
    .sort((a, b) => scoreDe(b.id) - scoreDe(a.id));

  const altas = coincidencias.filter((c) => c.score >= 75).length;
  const porNotificar = coincidencias.filter((c) => c.score >= 60).length;

  return (
    <div>
      <div className="mb-6">
        <h2 className="font-display text-2xl font-semibold tracking-tight text-ink">
          Buenos días, {usuario.nombre}
        </h2>
        <p className="mt-1.5 max-w-[70ch] text-sm text-ink-2">
          Licitaciones ingeridas desde ChileCompra, cruzadas contra tus preferencias por coincidencias-ms.
        </p>
      </div>

      <div className="mb-6 grid grid-cols-4 gap-4">
        <Kpi valor={licitaciones.length} etiqueta="Licitaciones ingeridas" color="text-ink" />
        <Kpi valor={altas} etiqueta="Coincidencias altas (≥75%)" color="text-go" />
        <Kpi valor={porNotificar} etiqueta="Alertas enviadas" color="text-amber" />
        <Kpi valor={coincidencias.length} etiqueta="Coincidencias calculadas" color="text-sky-deep" />
      </div>

      <div className="rounded-[14px] border border-line bg-card shadow-[0_1px_2px_rgba(12,28,58,0.06),0_8px_24px_-12px_rgba(12,28,58,0.18)]">
        <div className="flex flex-wrap gap-2.5 border-b border-line p-3.5">
          <select
            value={filtroRubro}
            onChange={(e) => setFiltroRubro(e.target.value)}
            className="h-8.5 rounded-lg border border-line px-2.5 text-xs"
          >
            <option value="">Todos los rubros</option>
            {rubros.map((r) => (
              <option key={r}>{r}</option>
            ))}
          </select>
          <input
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
            placeholder="Buscar por código…"
            className="h-8.5 flex-1 min-w-[180px] rounded-lg border border-line px-2.5 text-xs"
          />
        </div>

        {error && <p className="p-5 text-sm text-red-600">{error}</p>}

        {!error && cargando && <div className="p-6 text-sm text-ink-3">Cargando…</div>}

        {!error && !cargando && filtradas.length === 0 && (
          <div className="p-10 text-center text-sm text-ink-3">
            Aún no hay licitaciones ingeridas. Esta lista se llena cuando ingesta-ms trae datos reales de
            Mercado Público (job pendiente de implementar).
          </div>
        )}

        {!error &&
          !cargando &&
          filtradas.map((l) => (
            <div key={l.id} className="flex items-start gap-3.5 border-b border-[#eef2f8] px-4.5 py-3.5 last:border-0">
              <ScoreRing score={scoreDe(l.id)} />
              <div className="min-w-0 flex-1">
                <div className="text-[14.5px] font-semibold text-sky-deep">{l.codigo}</div>
                <div className="mt-1.5 flex flex-wrap gap-x-3.5 gap-y-1 text-xs text-ink-2">
                  <span>{clp(l.monto)}</span>
                  <span>{l.region}</span>
                  <span>{l.estado}</span>
                  {l.fechaCierre && <span>cierra {l.fechaCierre}</span>}
                </div>
                <div className="mt-2 flex flex-wrap gap-1.5">
                  <span className="rounded-full bg-[#eef2f8] px-2.5 py-0.5 text-[11px] text-ink-2">{l.rubro}</span>
                </div>
              </div>
            </div>
          ))}
      </div>
    </div>
  );
}

function Kpi({ valor, etiqueta, color }) {
  return (
    <div className="rounded-[14px] border border-line bg-card p-4 shadow-[0_1px_2px_rgba(12,28,58,0.06),0_8px_24px_-12px_rgba(12,28,58,0.18)]">
      <div className={`font-display text-[28px] font-semibold leading-none tracking-tight ${color}`}>{valor}</div>
      <div className="mt-1.5 text-[12.5px] text-ink-2">{etiqueta}</div>
    </div>
  );
}
