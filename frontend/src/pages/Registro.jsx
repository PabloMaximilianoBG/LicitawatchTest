import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { registrarUsuario } from "../api/usuarios";
import { useUsuarioActivo } from "../context/UsuarioActivoContext";

export default function Registro() {
  const [nombre, setNombre] = useState("");
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [enviando, setEnviando] = useState(false);
  const { setUsuario } = useUsuarioActivo();
  const navigate = useNavigate();

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setEnviando(true);
    try {
      const creado = await registrarUsuario({ nombre, email, rolId: 1 });
      setUsuario(creado);
      navigate("/preferencias");
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "No se pudo registrar. Verifica que usuarios-ms y el gateway estén corriendo."
      );
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="mx-auto max-w-md">
      <div className="mb-6">
        <h2 className="font-display text-2xl font-semibold tracking-tight text-ink">
          Crea tu cuenta en LicitaWatch
        </h2>
        <p className="mt-1.5 text-sm text-ink-2">
          Registra tu OTEC para empezar a recibir alertas de licitaciones que coincidan con tu perfil.
        </p>
      </div>

      <form
        onSubmit={onSubmit}
        className="rounded-[14px] border border-line bg-card p-6 shadow-[0_1px_2px_rgba(12,28,58,0.06),0_8px_24px_-12px_rgba(12,28,58,0.18)]"
      >
        <label className="mb-4 block">
          <span className="mb-2 block text-[13px] font-semibold text-ink">Nombre de la organización</span>
          <input
            required
            value={nombre}
            onChange={(e) => setNombre(e.target.value)}
            placeholder="OTEC Chile Digital"
            className="h-11 w-full rounded-[10px] border border-line px-3.5 text-sm text-ink outline-none focus:border-sky"
          />
        </label>

        <label className="mb-5 block">
          <span className="mb-2 block text-[13px] font-semibold text-ink">Correo de contacto</span>
          <input
            required
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="contacto@chiledigital.cl"
            className="h-11 w-full rounded-[10px] border border-line px-3.5 text-sm text-ink outline-none focus:border-sky"
          />
        </label>

        {error && <p className="mb-4 text-sm text-red-600">{error}</p>}

        <button
          disabled={enviando}
          className="h-11 w-full rounded-[10px] bg-sky text-sm font-semibold text-white hover:bg-sky-deep disabled:opacity-60"
        >
          {enviando ? "Creando cuenta…" : "Crear cuenta"}
        </button>
      </form>
    </div>
  );
}
