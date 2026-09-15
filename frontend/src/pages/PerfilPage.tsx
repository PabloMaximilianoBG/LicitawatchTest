import { type FormEvent, useEffect, useState } from "react";
import { actualizarPerfilCliente, actualizarPerfilEmpresa, obtenerMiPerfil } from "../api/perfil";
import { obtenerMensajeError } from "../api/client";
import type { PerfilResponse } from "../types";
import Card from "../components/ui/Card";
import Input from "../components/ui/Input";
import Button from "../components/ui/Button";
import Alert from "../components/ui/Alert";
import Spinner from "../components/ui/Spinner";
import Badge from "../components/ui/Badge";

export default function PerfilPage() {
  const [perfil, setPerfil] = useState<PerfilResponse | null>(null);
  const [razonSocial, setRazonSocial] = useState("");
  const [rubro, setRubro] = useState("");
  const [nombreContacto, setNombreContacto] = useState("");
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [exito, setExito] = useState(false);

  useEffect(() => {
    obtenerMiPerfil()
      .then((p) => {
        setPerfil(p);
        setRazonSocial(p.razonSocial ?? "");
        setRubro(p.rubro ?? "");
        setNombreContacto(p.nombreContacto ?? "");
      })
      .catch((err) => setError(obtenerMensajeError(err)))
      .finally(() => setCargando(false));
  }, []);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setExito(false);
    setGuardando(true);
    try {
      const actualizado =
        perfil?.rol === "EMPRESA"
          ? await actualizarPerfilEmpresa(razonSocial, rubro)
          : await actualizarPerfilCliente(nombreContacto);
      setPerfil(actualizado);
      setExito(true);
    } catch (err) {
      setError(obtenerMensajeError(err));
    } finally {
      setGuardando(false);
    }
  }

  if (cargando) return <Spinner />;
  if (!perfil) return <Alert tipo="error">{error ?? "No se pudo cargar tu perfil."}</Alert>;

  return (
    <div className="mx-auto max-w-lg space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Mi perfil</h1>
        <p className="text-sm text-slate-400">Administra tus datos de cuenta.</p>
      </div>

      <Card className="space-y-4">
        <div className="flex items-center justify-between border-b border-white/10 pb-4">
          <div>
            <p className="font-semibold text-white">{perfil.email}</p>
            <p className="text-sm text-slate-500">RUT: {perfil.rut ?? "-"}</p>
          </div>
          <Badge tono="brand">{perfil.rol}</Badge>
        </div>

        {error && <Alert tipo="error">{error}</Alert>}
        {exito && <Alert tipo="success">Perfil actualizado correctamente.</Alert>}

        {perfil.rol === "ADMINISTRADOR" ? (
          <p className="text-sm text-slate-400">Área: {perfil.area ?? "-"}</p>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            {perfil.rol === "EMPRESA" ? (
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
            <Button type="submit" loading={guardando}>
              Guardar cambios
            </Button>
          </form>
        )}
      </Card>
    </div>
  );
}
