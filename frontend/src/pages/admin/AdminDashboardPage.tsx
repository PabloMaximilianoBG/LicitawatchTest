import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listarTodasLicitaciones, listarTodasVentas, listarUsuarios } from "../../api/admin";
import Card from "../../components/ui/Card";
import Spinner from "../../components/ui/Spinner";

interface Resumen {
  usuarios: number;
  empresas: number;
  clientes: number;
  licitaciones: number;
  ventas: number;
}

export default function AdminDashboardPage() {
  const [resumen, setResumen] = useState<Resumen | null>(null);

  useEffect(() => {
    Promise.all([listarUsuarios(), listarTodasLicitaciones(), listarTodasVentas()]).then(([usuarios, licitaciones, ventas]) => {
      setResumen({
        usuarios: usuarios.length,
        empresas: usuarios.filter((u) => u.rol === "EMPRESA").length,
        clientes: usuarios.filter((u) => u.rol === "CLIENTE").length,
        licitaciones: licitaciones.length,
        ventas: ventas.length,
      });
    });
  }, []);

  if (!resumen) return <Spinner />;

  const stats = [
    { label: "Usuarios totales", valor: resumen.usuarios, to: "/admin/usuarios" },
    { label: "Empresas", valor: resumen.empresas, to: "/admin/usuarios" },
    { label: "Clientes", valor: resumen.clientes, to: "/admin/usuarios" },
    { label: "Licitaciones", valor: resumen.licitaciones, to: "/admin/licitaciones" },
    { label: "Ventas", valor: resumen.ventas, to: "/admin/ventas" },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Panel de administración</h1>
        <p className="text-sm text-slate-400">Vista general de la plataforma.</p>
      </div>
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-5">
        {stats.map((s) => (
          <Link key={s.label} to={s.to}>
            <Card className="text-center transition-transform hover:-translate-y-1 hover:border-brand-400/40">
              <p className="text-3xl font-extrabold text-white">{s.valor}</p>
              <p className="mt-1 text-xs text-slate-400">{s.label}</p>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  );
}
