import { Navigate } from "react-router-dom";
import { useUsuarioActivo } from "../context/UsuarioActivoContext";

export default function RequireUsuario({ children }) {
  const { usuario } = useUsuarioActivo();
  if (!usuario) return <Navigate to="/registro" replace />;
  return children;
}
