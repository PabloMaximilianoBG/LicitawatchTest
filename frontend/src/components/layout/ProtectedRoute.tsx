import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuthStore } from "../../store/authStore";
import type { Rol } from "../../types";

export default function ProtectedRoute({ rolesPermitidos }: { rolesPermitidos?: Rol[] }) {
  const { accessToken, usuario } = useAuthStore();
  const location = useLocation();

  if (!accessToken || !usuario) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (rolesPermitidos && !rolesPermitidos.includes(usuario.rol)) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
