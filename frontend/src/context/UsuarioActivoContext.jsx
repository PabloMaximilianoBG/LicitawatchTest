import { createContext, useContext, useEffect, useState } from "react";

// Sesión simplificada mientras el login del frontend contra Keycloak no está integrado:
// guardamos el usuario recién registrado en localStorage y lo usamos como "usuario activo".
const UsuarioActivoContext = createContext(null);

const STORAGE_KEY = "licitawatch.usuarioActivo";

export function UsuarioActivoProvider({ children }) {
  const [usuario, setUsuario] = useState(() => {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  });

  useEffect(() => {
    if (usuario) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(usuario));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  }, [usuario]);

  return (
    <UsuarioActivoContext.Provider value={{ usuario, setUsuario }}>
      {children}
    </UsuarioActivoContext.Provider>
  );
}

export function useUsuarioActivo() {
  const ctx = useContext(UsuarioActivoContext);
  if (!ctx) throw new Error("useUsuarioActivo debe usarse dentro de UsuarioActivoProvider");
  return ctx;
}
