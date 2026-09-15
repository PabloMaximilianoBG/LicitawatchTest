import { type FormEvent, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { login } from "../api/auth";
import { obtenerMensajeError } from "../api/client";
import { useAuthStore } from "../store/authStore";
import Button from "../components/ui/Button";
import Input from "../components/ui/Input";
import Alert from "../components/ui/Alert";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [contrasena, setContrasena] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [cargando, setCargando] = useState(false);
  const setSesion = useAuthStore((s) => s.setSesion);
  const navigate = useNavigate();
  const location = useLocation();

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setCargando(true);
    try {
      const data = await login(email, contrasena);
      setSesion(data.accessToken, data.refreshToken, data.usuario);
      const destino = (location.state as { from?: Location })?.from?.pathname;
      if (data.usuario.rol === "ADMINISTRADOR") navigate(destino ?? "/admin");
      else navigate(destino ?? "/licitaciones");
    } catch (err) {
      setError(obtenerMensajeError(err, "Email o contraseña inválidos."));
    } finally {
      setCargando(false);
    }
  }

  return (
    <>
      <h1 className="mb-1 text-2xl font-bold text-white">Inicia sesión</h1>
      <p className="mb-6 text-sm text-slate-400">Accede a tu cuenta de LicitaWatch.</p>

      {error && (
        <div className="mb-4">
          <Alert tipo="error">{error}</Alert>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <Input
          label="Email"
          type="email"
          required
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="tu@empresa.cl"
        />
        <Input
          label="Contraseña"
          type="password"
          required
          value={contrasena}
          onChange={(e) => setContrasena(e.target.value)}
          placeholder="••••••••"
        />
        <Button type="submit" className="w-full" loading={cargando}>
          Ingresar
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-slate-400">
        ¿No tienes cuenta?{" "}
        <Link to="/registro" className="font-semibold text-brand-400 hover:text-brand-300">
          Regístrate
        </Link>
      </p>
    </>
  );
}
