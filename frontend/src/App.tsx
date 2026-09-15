import { Route, Routes } from "react-router-dom";
import AppLayout from "./components/layout/AppLayout";
import AuthLayout from "./components/layout/AuthLayout";
import ProtectedRoute from "./components/layout/ProtectedRoute";

import LandingPage from "./pages/LandingPage";
import LoginPage from "./pages/LoginPage";
import RegistroPage from "./pages/RegistroPage";
import LicitacionesPublicasPage from "./pages/LicitacionesPublicasPage";
import LicitacionDetallePage from "./pages/LicitacionDetallePage";
import MisPostulacionesPage from "./pages/MisPostulacionesPage";
import PlanesPage from "./pages/PlanesPage";
import PagoResultadoPage from "./pages/PagoResultadoPage";
import PerfilPage from "./pages/PerfilPage";
import NotFoundPage from "./pages/NotFoundPage";

import MisLicitacionesPage from "./pages/empresa/MisLicitacionesPage";
import PublicarLicitacionPage from "./pages/empresa/PublicarLicitacionPage";
import PostulacionesRecibidasPage from "./pages/empresa/PostulacionesRecibidasPage";

import AdminDashboardPage from "./pages/admin/AdminDashboardPage";
import AdminUsuariosPage from "./pages/admin/AdminUsuariosPage";
import AdminLicitacionesPage from "./pages/admin/AdminLicitacionesPage";
import AdminVentasPage from "./pages/admin/AdminVentasPage";

export default function App() {
  return (
    <Routes>
      {/* Publicas (sin layout de sesion) */}
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/registro" element={<RegistroPage />} />
      </Route>

      {/* Autenticadas, cualquier rol */}
      <Route element={<AppLayout />}>
        <Route path="/" element={<LandingPage />} />
        <Route path="/pago/resultado" element={<PagoResultadoPage />} />

        <Route element={<ProtectedRoute />}>
          <Route path="/perfil" element={<PerfilPage />} />
          <Route path="/licitaciones" element={<LicitacionesPublicasPage />} />
          <Route path="/licitaciones/:id" element={<LicitacionDetallePage />} />
          <Route path="/planes" element={<PlanesPage />} />
        </Route>

        <Route element={<ProtectedRoute rolesPermitidos={["CLIENTE"]} />}>
          <Route path="/mis-postulaciones" element={<MisPostulacionesPage />} />
        </Route>

        <Route element={<ProtectedRoute rolesPermitidos={["EMPRESA"]} />}>
          <Route path="/empresa/licitaciones" element={<MisLicitacionesPage />} />
          <Route path="/empresa/licitaciones/nueva" element={<PublicarLicitacionPage />} />
          <Route path="/empresa/licitaciones/:id/editar" element={<PublicarLicitacionPage />} />
          <Route path="/empresa/licitaciones/:id/postulaciones" element={<PostulacionesRecibidasPage />} />
        </Route>

        <Route element={<ProtectedRoute rolesPermitidos={["ADMINISTRADOR"]} />}>
          <Route path="/admin" element={<AdminDashboardPage />} />
          <Route path="/admin/usuarios" element={<AdminUsuariosPage />} />
          <Route path="/admin/licitaciones" element={<AdminLicitacionesPage />} />
          <Route path="/admin/ventas" element={<AdminVentasPage />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}
