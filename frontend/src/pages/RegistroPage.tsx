import { type FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { registrarCliente, registrarEmpresa } from "../api/auth";
import { obtenerMensajeError } from "../api/client";
import Button from "../components/ui/Button";
import Input from "../components/ui/Input";
import Alert from "../components/ui/Alert";

type Tipo = "EMPRESA" | "CLIENTE";

export default function RegistroPage() {
  const [tipo, setTipo] = useState<Tipo>("EMPRESA");
  const [email, setEmail] = useState("");
  const [contrasena, setContrasena] = useState("");
  const [rut, setRut] = useState("");
  const [razonSocial, setRazonSocial] = useState("");
  const [rubro, setRubro] = useState("");
  const [nombreContacto, setNombreContacto] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [exito, setExito] = useState(false);
  const [cargando, setCargando] = useState(false);
  const navigate = useNavigate();

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setCargando(true);
    try {
      if (tipo === "EMPRESA") {
        await registrarEmpresa({ email, contrasena, razonSocial, rut, rubro });
      } else {
        await registrarCliente({ email, contrasena, nombreContacto, rut });
      }
      setExito(true);
      setTimeout(() => navigate("/login"), 1500);
    } catch (err) {
      setError(obtenerMensajeError(err, "No se pudo completar el registro."));
    } finally {
      setCargando(false);
    }
  }

  return (
    <>
      <h1 className="mb-1 text-2xl font-bold text-white">Crea tu cuenta</h1>
      <p className="mb-6 text-sm text-slate-400">Elige el tipo de cuenta que quieres crear.</p>

      <div className="mb-6 grid grid-cols-2 gap-2 rounded-xl bg-white/5 p-1">
        {(["EMPRESA", "CLIENTE"] as Tipo[]).map((t) => (
          <button
            key={t}
            type="button"
            onClick={() => setTipo(t)}
            className={`rounded-lg py-2 text-sm font-semibold transition-colors ${
              tipo === t ? "bg-gradient-to-r from-brand-600 to-violet-600 text-white" : "text-slate-400 hover:text-slate-200"
            }`}
          >
            {t === "EMPRESA" ? "Empresa" : "Cliente"}
          </button>
        ))}
      </div>

      {error && (
        <div className="mb-4">
          <Alert tipo="error">{error}</Alert>
        </div>
      )}
      {exito && (
        <div className="mb-4">
          <Alert tipo="success">Cuenta creada con éxito. Redirigiendo a iniciar sesión…</Alert>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <Input label="Email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
        <Input
          label="Contraseña"
          type="password"
          required
          minLength={8}
          value={contrasena}
          onChange={(e) => setContrasena(e.target.value)}
        />
        <Input
          label="RUT"
          required
          placeholder="12345678-9"
          pattern="\d{7,8}-[\dkK]"
          title="Formato: 12345678-9"
          value={rut}
          onChange={(e) => setRut(e.target.value)}
        />

        {tipo === "EMPRESA" ? (
          <>
            <Input label="Razón social" required value={razonSocial} onChange={(e) => setRazonSocial(e.target.value)} />
            <Input label="Rubro" value={rubro} onChange={(e) => setRubro(e.target.value)} />
          </>
        ) : (
          <Input
            label="Nombre de contacto"
            required
            value={nombreContacto}
            onChange={(e) => setNombreContacto(e.target.value)}
          />
        )}

        <Button type="submit" className="w-full" loading={cargando}>
          Crear cuenta
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-slate-400">
        ¿Ya tienes cuenta?{" "}
        <Link to="/login" className="font-semibold text-brand-400 hover:text-brand-300">
          Inicia sesión
        </Link>
      </p>
    </>
  );
}
