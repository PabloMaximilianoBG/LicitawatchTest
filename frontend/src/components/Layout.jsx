import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useUsuarioActivo } from "../context/UsuarioActivoContext";

const navItems = [
  { to: "/dashboard", label: "Radar" },
  { to: "/preferencias", label: "Preferencias de alerta" },
];

export default function Layout() {
  const { usuario, setUsuario } = useUsuarioActivo();
  const navigate = useNavigate();

  const cerrarSesion = () => {
    setUsuario(null);
    navigate("/registro");
  };

  return (
    <div className="flex h-screen overflow-hidden font-sans text-ink">
      <aside className="flex w-62 flex-shrink-0 flex-col border-r border-[#05101f] bg-navy-1 text-[#c8d6ee]">
        <div className="flex items-center gap-3 px-5 pt-5 pb-4">
          <div className="grid h-9 w-9 flex-shrink-0 place-items-center rounded-[10px] bg-[radial-gradient(circle_at_50%_50%,#2e8be6_0%,#1a5fb0_55%,#0d3d7d_100%)] shadow-[inset_0_0_0_1px_rgba(255,255,255,0.12)]">
            <span className="h-1.5 w-1.5 rounded-full bg-white shadow-[0_0_8px_#bfe0ff]" />
          </div>
          <div>
            <div className="font-display text-[19px] font-semibold leading-none tracking-tight text-white">
              Licita<span className="text-[#7bb8f2]">Watch</span>
            </div>
            <div className="mt-1 text-[10.5px] uppercase tracking-[0.14em] text-[#5f7cad]">
              Radar de licitaciones
            </div>
          </div>
        </div>

        <nav className="flex flex-1 flex-col gap-0.5 px-3 py-1.5">
          <div className="px-2.5 pb-1.5 pt-3.5 text-[10px] uppercase tracking-[0.14em] text-[#556f9c]">
            Monitoreo
          </div>
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `rounded-[10px] px-3 py-2.5 text-sm transition-colors ${
                  isActive ? "bg-[#153163] font-medium text-white" : "text-[#aebdd8] hover:bg-[#12294e] hover:text-[#eaf2ff]"
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="m-3 rounded-xl border border-[#17356a] bg-[#0c2044] px-3.5 py-3">
          <div className="text-[10px] uppercase tracking-[0.12em] text-[#5f7cad]">Perfil activo</div>
          <div className="mt-0.5 text-[13.5px] font-semibold text-[#eaf2ff]">
            {usuario ? usuario.nombre : "Sin usuario"}
          </div>
          {usuario && (
            <button
              onClick={cerrarSesion}
              className="mt-2 inline-block rounded-full border border-[#1d5c3e] bg-[#123a2a] px-2 py-0.5 text-[11px] text-[#8fd4a8] hover:brightness-110"
            >
              Cambiar de usuario
            </button>
          )}
        </div>
      </aside>

      <main className="flex min-w-0 flex-1 flex-col bg-canvas">
        <div className="flex-1 overflow-y-auto px-8 py-7">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
