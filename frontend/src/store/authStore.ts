import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { UsuarioResumen } from "../types";

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  usuario: UsuarioResumen | null;
  setSesion: (accessToken: string, refreshToken: string, usuario: UsuarioResumen) => void;
  setAccessToken: (accessToken: string, refreshToken?: string) => void;
  cerrarSesion: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      usuario: null,
      setSesion: (accessToken, refreshToken, usuario) => set({ accessToken, refreshToken, usuario }),
      setAccessToken: (accessToken, refreshToken) =>
        set((state) => ({ accessToken, refreshToken: refreshToken ?? state.refreshToken })),
      cerrarSesion: () => set({ accessToken: null, refreshToken: null, usuario: null }),
    }),
    { name: "licitawatch-auth" }
  )
);
