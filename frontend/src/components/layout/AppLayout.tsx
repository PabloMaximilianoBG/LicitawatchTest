import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuthStore } from "../../store/authStore";
import { logout as logoutRequest } from "../../api/auth";
import ChatWidget from "../chat/ChatWidget";

const linksPorRol: Record<string, { to: string; label: string }[]> = {
  EMPRESA: [
    { to: "/licitaciones", label: "Buscar licitaciones" },
    { to: "/empresa/licitaciones", label: "Mis licitaciones" },
    { to: "/empresa/licitaciones/nueva", label: "Publicar" },
    { to: "/planes", label: "Planes" },
  ],
  CLIENTE: [
    { to: "/licitaciones", label: "Buscar licitaciones" },
    { to: "/mis-postulaciones", label: "Mis postulaciones" },
    { to: "/planes", label: "Planes" },
  ],
  ADMINISTRADOR: [
    { to: "/admin", label: "Dashboard" },
    { to: "/admin/usuarios", label: "Usuarios" },
    { to: "/admin/licitaciones", label: "Licitaciones" },
    { to: "/admin/ventas", label: "Ventas" },
  ],
};

function navLinkClases(activo: boolean) {
  return `rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
    activo ? "bg-white/10 text-white" : "text-slate-400 hover:text-slate-100 hover:bg-white/5"
  }`;
}

export default function AppLayout() {
  const { usuario, refreshToken, cerrarSesion } = useAuthStore();
  const navigate = useNavigate();

  async function handleLogout() {
    try {
      if (refreshToken) await logoutRequest(refreshToken);
    } catch {
      // si falla el logout remoto igual limpiamos la sesion local
    }
    cerrarSesion();
    navigate("/login");
  }

  const links = usuario ? linksPorRol[usuario.rol] ?? [] : [];

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100">
      <div className="pointer-events-none fixed inset-0 overflow-hidden">
        <div className="absolute -top-40 left-1/4 h-96 w-96 rounded-full bg-brand-600/20 blur-3xl" />
        <div className="absolute top-1/3 -right-40 h-96 w-96 rounded-full bg-violet-600/10 blur-3xl" />
      </div>

      <header className="relative z-10 border-b border-white/10 bg-slate-950/70 backdrop-blur-md">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-3">
          <div className="flex items-center gap-8">
            <NavLink to="/" className="flex items-center gap-2 font-extrabold tracking-tight text-white">
              <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-brand-500 to-violet-600 text-sm">
                LW
              </span>
              LicitaWatch
            </NavLink>
            <nav className="hidden items-center gap-1 md:flex">
              {links.map((link) => (
                <NavLink key={link.to} to={link.to} className={({ isActive }) => navLinkClases(isActive)} end>
                  {link.label}
                </NavLink>
              ))}
            </nav>
          </div>

          <div className="flex items-center gap-3">
            <NavLink to="/perfil" className={({ isActive }) => navLinkClases(isActive)}>
              {usuario?.email}
            </NavLink>
            <span className="hidden rounded-full bg-white/10 px-2.5 py-1 text-xs font-semibold text-slate-300 sm:inline">
              {usuario?.rol}
            </span>
            <button
              onClick={handleLogout}
              className="rounded-lg px-3 py-2 text-sm font-medium text-slate-400 transition-colors hover:bg-white/5 hover:text-red-300"
            >
              Salir
            </button>
          </div>
        </div>
        <nav className="flex gap-1 overflow-x-auto px-4 pb-3 md:hidden">
          {links.map((link) => (
            <NavLink key={link.to} to={link.to} className={({ isActive }) => navLinkClases(isActive)} end>
              {link.label}
            </NavLink>
          ))}
        </nav>
      </header>

      <main className="relative z-10 mx-auto max-w-7xl px-6 py-8">
        <Outlet />
      </main>

      {(usuario?.rol === "EMPRESA" || usuario?.rol === "CLIENTE") && <ChatWidget />}
    </div>
  );
}
