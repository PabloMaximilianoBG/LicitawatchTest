import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { UsuarioActivoProvider } from "./context/UsuarioActivoContext";
import Layout from "./components/Layout";
import RequireUsuario from "./components/RequireUsuario";
import Registro from "./pages/Registro";
import Preferencias from "./pages/Preferencias";
import Dashboard from "./pages/Dashboard";

export default function App() {
  return (
    <UsuarioActivoProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/registro" element={<Registro />} />

          <Route element={<Layout />}>
            <Route
              path="/dashboard"
              element={
                <RequireUsuario>
                  <Dashboard />
                </RequireUsuario>
              }
            />
            <Route
              path="/preferencias"
              element={
                <RequireUsuario>
                  <Preferencias />
                </RequireUsuario>
              }
            />
          </Route>

          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </UsuarioActivoProvider>
  );
}
