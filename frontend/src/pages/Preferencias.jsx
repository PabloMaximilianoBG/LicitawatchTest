import { useEffect, useState } from "react";
import {
  listarPreferencias,
  crearPreferencia,
  actualizarPreferencia,
  eliminarPreferencia,
} from "../api/usuarios";
import { useUsuarioActivo } from "../context/UsuarioActivoContext";
import { RUBROS, REGIONES, CANALES, FRECUENCIAS } from "../constants";

const FORM_VACIO = { rubro: RUBROS[0], monto: 20000000, region: REGIONES[0], canal: CANALES[0], frecuencia: FRECUENCIAS[0] };

export default function Preferencias() {
  const { usuario } = useUsuarioActivo();
  const [preferencias, setPreferencias] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const [form, setForm] = useState(FORM_VACIO);
  const [editandoId, setEditandoId] = useState(null);

  const cargar = async () => {
    setCargando(true);
    setError("");
    try {
      const data = await listarPreferencias(usuario.id);
      setPreferencias(data);
    } catch (err) {
      setError("No se pudieron cargar tus preferencias. ¿usuarios-ms está corriendo?");
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    cargar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      if (editandoId) {
        await actualizarPreferencia(usuario.id, editandoId, form);
      } else {
        await crearPreferencia(usuario.id, form);
      }
      setForm(FORM_VACIO);
      setEditandoId(null);
      cargar();
    } catch (err) {
      setError("No se pudo guardar la preferencia.");
    }
  };

  const onEditar = (p) => {
    setEditandoId(p.id);
    setForm({ rubro: p.rubro, monto: p.monto, region: p.region, canal: p.canal, frecuencia: p.frecuencia });
  };

  const onEliminar = async (id) => {
    await eliminarPreferencia(usuario.id, id);
    cargar();
  };

  return (
    <div className="max-w-3xl">
      <div className="mb-5">
        <h2 className="font-display text-2xl font-semibold tracking-tight text-ink">Preferencias de alerta</h2>
        <p className="mt-1.5 text-sm text-ink-2 max-w-[70ch]">
          Cada preferencia alimenta al motor de coincidencia (rubro, región y monto ponderados). Cuando guardas,
          usuarios-ms publica el evento y coincidencias-ms vuelve a puntuar tus licitaciones.
        </p>
      </div>

      <form
        onSubmit={onSubmit}
        className="mb-6 grid grid-cols-2 gap-4 rounded-[14px] border border-line bg-card p-5 shadow-[0_1px_2px_rgba(12,28,58,0.06),0_8px_24px_-12px_rgba(12,28,58,0.18)]"
      >
        <label className="block">
          <span className="mb-1.5 block text-[13px] font-semibold text-ink">Rubro</span>
          <select
            value={form.rubro}
            onChange={(e) => setForm({ ...form, rubro: e.target.value })}
            className="h-10 w-full rounded-lg border border-line px-2.5 text-sm"
          >
            {RUBROS.map((r) => (
              <option key={r}>{r}</option>
            ))}
          </select>
        </label>

        <label className="block">
          <span className="mb-1.5 block text-[13px] font-semibold text-ink">Región</span>
          <select
            value={form.region}
            onChange={(e) => setForm({ ...form, region: e.target.value })}
            className="h-10 w-full rounded-lg border border-line px-2.5 text-sm"
          >
            {REGIONES.map((r) => (
              <option key={r}>{r}</option>
            ))}
          </select>
        </label>

        <label className="block">
          <span className="mb-1.5 block text-[13px] font-semibold text-ink">Monto máximo (CLP)</span>
          <input
            type="number"
            step={1000000}
            value={form.monto}
            onChange={(e) => setForm({ ...form, monto: Number(e.target.value) })}
            className="h-10 w-full rounded-lg border border-line px-2.5 text-sm"
          />
        </label>

        <label className="block">
          <span className="mb-1.5 block text-[13px] font-semibold text-ink">Canal</span>
          <select
            value={form.canal}
            onChange={(e) => setForm({ ...form, canal: e.target.value })}
            className="h-10 w-full rounded-lg border border-line px-2.5 text-sm"
          >
            {CANALES.map((c) => (
              <option key={c}>{c}</option>
            ))}
          </select>
        </label>

        <label className="block">
          <span className="mb-1.5 block text-[13px] font-semibold text-ink">Frecuencia</span>
          <select
            value={form.frecuencia}
            onChange={(e) => setForm({ ...form, frecuencia: e.target.value })}
            className="h-10 w-full rounded-lg border border-line px-2.5 text-sm"
          >
            {FRECUENCIAS.map((f) => (
              <option key={f}>{f}</option>
            ))}
          </select>
        </label>

        <div className="flex items-end gap-2">
          <button className="h-10 flex-1 rounded-lg bg-sky text-sm font-semibold text-white hover:bg-sky-deep">
            {editandoId ? "Guardar cambios" : "Agregar preferencia"}
          </button>
          {editandoId && (
            <button
              type="button"
              onClick={() => {
                setEditandoId(null);
                setForm(FORM_VACIO);
              }}
              className="h-10 rounded-lg border border-line px-3 text-sm text-ink-2"
            >
              Cancelar
            </button>
          )}
        </div>
      </form>

      {error && <p className="mb-4 text-sm text-red-600">{error}</p>}

      <div className="overflow-hidden rounded-[14px] border border-line bg-card shadow-[0_1px_2px_rgba(12,28,58,0.06),0_8px_24px_-12px_rgba(12,28,58,0.18)]">
        {cargando ? (
          <div className="p-6 text-sm text-ink-3">Cargando…</div>
        ) : preferencias.length === 0 ? (
          <div className="p-6 text-sm text-ink-3">Aún no tienes preferencias configuradas.</div>
        ) : (
          preferencias.map((p) => (
            <div key={p.id} className="flex items-center gap-4 border-b border-[#eef2f8] px-5 py-4 last:border-0">
              <div className="flex-1">
                <div className="flex flex-wrap gap-2 text-[11px]">
                  <span className="rounded-full bg-sky-soft px-2.5 py-1 font-medium text-sky-deep">{p.rubro}</span>
                  <span className="rounded-full bg-[#eef2f8] px-2.5 py-1 text-ink-2">{p.region}</span>
                </div>
                <div className="mt-2 text-[13px] text-ink-2">
                  Hasta ${Number(p.monto).toLocaleString("es-CL")} · {p.canal} · {p.frecuencia}
                </div>
              </div>
              <button onClick={() => onEditar(p)} className="rounded-lg border border-line px-3 py-1.5 text-xs text-ink-2 hover:border-sky">
                Editar
              </button>
              <button onClick={() => onEliminar(p.id)} className="rounded-lg border border-line px-3 py-1.5 text-xs text-red-600 hover:border-red-400">
                Eliminar
              </button>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
