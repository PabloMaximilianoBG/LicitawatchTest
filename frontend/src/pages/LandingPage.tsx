import { Link } from "react-router-dom";
import { useAuthStore } from "../store/authStore";
import Button from "../components/ui/Button";
import Card from "../components/ui/Card";

const CARACTERISTICAS = [
  {
    titulo: "Publica licitaciones",
    descripcion: "Las empresas publican, editan y administran sus licitaciones desde un solo lugar.",
    icono: "📋",
  },
  {
    titulo: "Postula en minutos",
    descripcion: "Los proveedores buscan, filtran y postulan a licitaciones activas de forma simple.",
    icono: "🤝",
  },
  {
    titulo: "LicitAsist, tu copiloto con IA",
    descripcion: "Un asistente conversacional te ayuda a redactar, resumir y decidir más rápido.",
    icono: "✨",
  },
  {
    titulo: "Planes flexibles",
    descripcion: "Estándar o Premium, con pago seguro mediante Webpay Plus.",
    icono: "💳",
  },
];

export default function LandingPage() {
  const usuario = useAuthStore((s) => s.usuario);

  return (
    <div className="space-y-24 py-8">
      <section className="mx-auto max-w-3xl text-center">
        <span className="mb-4 inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/5 px-4 py-1.5 text-xs font-medium text-slate-300">
          🚀 Plataforma privada de licitaciones
        </span>
        <h1 className="mb-6 text-4xl font-extrabold tracking-tight text-white sm:text-6xl">
          Licitaciones, sin depender de{" "}
          <span className="bg-gradient-to-r from-brand-400 to-violet-400 bg-clip-text text-transparent">
            nadie más
          </span>
        </h1>
        <p className="mb-8 text-lg text-slate-400">
          LicitaWatch conecta empresas que publican licitaciones con proveedores que postulan — con un asistente de
          IA que te ayuda en el camino.
        </p>
        <div className="flex flex-wrap items-center justify-center gap-3">
          {usuario ? (
            <Link to="/licitaciones">
              <Button size="lg">Ir a licitaciones</Button>
            </Link>
          ) : (
            <>
              <Link to="/registro">
                <Button size="lg">Crear una cuenta</Button>
              </Link>
              <Link to="/login">
                <Button size="lg" variant="secondary">
                  Ya tengo cuenta
                </Button>
              </Link>
            </>
          )}
        </div>
      </section>

      <section className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        {CARACTERISTICAS.map((c) => (
          <Card key={c.titulo} className="text-center">
            <div className="mb-3 text-3xl">{c.icono}</div>
            <h3 className="mb-1.5 font-bold text-white">{c.titulo}</h3>
            <p className="text-sm text-slate-400">{c.descripcion}</p>
          </Card>
        ))}
      </section>
    </div>
  );
}
