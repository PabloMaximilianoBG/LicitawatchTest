import { useEffect, useState } from "react";
import { actualizarActivoUsuario, listarUsuarios } from "../../api/admin";
import { obtenerMensajeError } from "../../api/client";
import type { UsuarioAdminResponse } from "../../types";
import Card from "../../components/ui/Card";
import Badge from "../../components/ui/Badge";
import Spinner from "../../components/ui/Spinner";
import Alert from "../../components/ui/Alert";
import Button from "../../components/ui/Button";
import { formatoFecha } from "../../utils/formato";

export default function AdminUsuariosPage() {
  const [usuarios, setUsuarios] = useState<UsuarioAdminResponse[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actualizando, setActualizando] = useState<number | null>(null);

  async function cargar() {
    setCargando(true);
    try {
      setUsuarios(await listarUsuarios());
    } catch (err) {
      setError(obtenerMensajeError(err));
    } finally {
      setCargando(false);
    }
  }

  useEffect(() => {
    cargar();
  }, []);

  async function toggleActivo(u: UsuarioAdminResponse) {
    setActualizando(u.id);
    try {
      await actualizarActivoUsuario(u.id, !u.activo);
      await cargar();
    } catch (err) {
      setError(obtenerMensajeError(err));
    } finally {
      setActualizando(null);
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Usuarios</h1>
        <p className="text-sm text-slate-400">Gestiona empresas, clientes y administradores.</p>
      </div>

      {error && <Alert tipo="error">{error}</Alert>}
      {cargando ? (
        <Spinner />
      ) : (
        <Card className="overflow-x-auto p-0">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-white/10 text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-4 py-3">Email</th>
                <th className="px-4 py-3">Rol</th>
                <th className="px-4 py-3">Creado</th>
                <th className="px-4 py-3">Estado</th>
                <th className="px-4 py-3 text-right">Acción</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {usuarios.map((u) => (
                <tr key={u.id} className="text-slate-200">
                  <td className="px-4 py-3">{u.email}</td>
                  <td className="px-4 py-3">
                    <Badge tono="brand">{u.rol}</Badge>
                  </td>
                  <td className="px-4 py-3 text-slate-400">{formatoFecha(u.creadoEn)}</td>
                  <td className="px-4 py-3">
                    <Badge tono={u.activo ? "green" : "red"}>{u.activo ? "Activo" : "Inactivo"}</Badge>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <Button
                      size="sm"
                      variant={u.activo ? "danger" : "secondary"}
                      loading={actualizando === u.id}
                      onClick={() => toggleActivo(u)}
                    >
                      {u.activo ? "Desactivar" : "Activar"}
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}
    </div>
  );
}
