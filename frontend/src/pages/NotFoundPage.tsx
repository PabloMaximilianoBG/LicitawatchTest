import { Link } from "react-router-dom";
import Button from "../components/ui/Button";

export default function NotFoundPage() {
  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center text-center">
      <p className="text-7xl font-black text-white/10">404</p>
      <h1 className="mt-2 text-xl font-bold text-white">Página no encontrada</h1>
      <p className="mt-1 text-sm text-slate-400">La página que buscas no existe o fue movida.</p>
      <Link to="/" className="mt-6">
        <Button variant="secondary">Volver al inicio</Button>
      </Link>
    </div>
  );
}
