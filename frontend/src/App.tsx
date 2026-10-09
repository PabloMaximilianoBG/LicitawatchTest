import { Route, Routes } from 'react-router-dom'
import { Toaster } from '@/components/ui'
import { ProtectedRoute, RedirectInicio } from '@/components/ProtectedRoute'
import { DashboardLayout } from '@/components/layout/DashboardLayout'
import Landing from '@/pages/public/Landing'
import Login from '@/pages/public/Login'
import RegistroSeleccion from '@/pages/public/RegistroSeleccion'
import RegistroForm from '@/pages/public/RegistroForm'
import ConfirmarCuenta from '@/pages/public/ConfirmarCuenta'
import RecuperarPassword from '@/pages/public/RecuperarPassword'
import RestablecerPassword from '@/pages/public/RestablecerPassword'
import Planes from '@/pages/public/Planes'
import PagoResultado from '@/pages/public/PagoResultado'
import NotFound from '@/pages/public/NotFound'
import BuscarLicitaciones from '@/pages/pyme/BuscarLicitaciones'
import LicitacionDetallePyme from '@/pages/pyme/LicitacionDetallePyme'
import MisPostulaciones from '@/pages/pyme/MisPostulaciones'
import MiPlan from '@/pages/pyme/MiPlan'
import MisLicitaciones from '@/pages/licitador/MisLicitaciones'
import LicitacionForm from '@/pages/licitador/LicitacionForm'
import LicitacionGestion from '@/pages/licitador/LicitacionGestion'
import Perfil from '@/pages/compartidas/Perfil'
import Chat from '@/pages/compartidas/Chat'
import LicitAsist from '@/pages/compartidas/LicitAsist'
import Soporte from '@/pages/compartidas/Soporte'
import Avisos from '@/pages/compartidas/Avisos'
import AdminUsuarios from '@/pages/admin/AdminUsuarios'
import AdminLicitaciones from '@/pages/admin/AdminLicitaciones'
import AdminPostulaciones from '@/pages/admin/AdminPostulaciones'
import AdminVentas from '@/pages/admin/AdminVentas'
import AdminNotificaciones from '@/pages/admin/AdminNotificaciones'
import AdminCatalogos from '@/pages/admin/AdminCatalogos'

/** Rutas por rol, organizadas por los 6 dominios (usuarios, licitaciones, ventas, notificaciones, asistente, chat). */
export default function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/login" element={<Login />} />
        <Route path="/registro" element={<RegistroSeleccion />} />
        <Route path="/registro/licitador" element={<RegistroForm perfil="licitador" />} />
        <Route path="/registro/pyme" element={<RegistroForm perfil="pyme" />} />
        <Route path="/confirmar-cuenta" element={<ConfirmarCuenta />} />
        <Route path="/recuperar-password" element={<RecuperarPassword />} />
        <Route path="/restablecer-password" element={<RestablecerPassword />} />
        <Route path="/planes" element={<Planes />} />
        <Route path="/pago/resultado" element={<PagoResultado />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<DashboardLayout />}>
            <Route path="/inicio" element={<RedirectInicio />} />
            <Route path="/perfil" element={<Perfil />} />
            <Route path="/asistente" element={<LicitAsist />} />
            <Route path="/notificaciones" element={<Avisos />} />

            <Route element={<ProtectedRoute roles={['PYME']} />}>
              <Route path="/pyme/licitaciones" element={<BuscarLicitaciones />} />
              <Route path="/pyme/licitaciones/:id" element={<LicitacionDetallePyme />} />
              <Route path="/pyme/postulaciones" element={<MisPostulaciones />} />
              <Route path="/pyme/suscripcion" element={<MiPlan />} />
            </Route>

            <Route element={<ProtectedRoute roles={['LICITADOR']} />}>
              <Route path="/licitador/licitaciones" element={<MisLicitaciones />} />
              <Route path="/licitador/licitaciones/nueva" element={<LicitacionForm />} />
              <Route path="/licitador/licitaciones/:id" element={<LicitacionGestion />} />
              <Route path="/licitador/licitaciones/:id/editar" element={<LicitacionForm />} />
            </Route>

            <Route element={<ProtectedRoute roles={['LICITADOR', 'PYME']} />}>
              <Route path="/chat" element={<Chat />} />
              <Route path="/chat/:id" element={<Chat />} />
              <Route path="/soporte" element={<Soporte />} />
            </Route>

            <Route element={<ProtectedRoute roles={['ADMINISTRADOR']} />}>
              <Route path="/admin/usuarios" element={<AdminUsuarios />} />
              <Route path="/admin/licitaciones" element={<AdminLicitaciones />} />
              <Route path="/admin/licitaciones/:id/editar" element={<LicitacionForm />} />
              <Route path="/admin/postulaciones" element={<AdminPostulaciones />} />
              <Route path="/admin/ventas" element={<AdminVentas />} />
              <Route path="/admin/notificaciones" element={<AdminNotificaciones />} />
              <Route path="/admin/catalogos" element={<AdminCatalogos />} />
            </Route>
          </Route>
        </Route>
        <Route path="*" element={<NotFound />} />
      </Routes>
      <Toaster />
    </>
  )
}
