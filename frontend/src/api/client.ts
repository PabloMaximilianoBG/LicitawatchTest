import axios, { type AxiosError, type InternalAxiosRequestConfig } from "axios";
import { useAuthStore } from "../store/authStore";

const BASE_URL = import.meta.env.VITE_API_BASE_URL as string;

const apiClient = axios.create({ baseURL: BASE_URL });

apiClient.interceptors.request.use((config) => {
  const { accessToken } = useAuthStore.getState();
  if (accessToken) {
    config.headers.set("Authorization", `Bearer ${accessToken}`);
  }
  return config;
});

type RequestConRetry = InternalAxiosRequestConfig & { _retry?: boolean };

let refrescoEnCurso: Promise<string | null> | null = null;

async function refrescarToken(): Promise<string | null> {
  const { refreshToken, setAccessToken, cerrarSesion } = useAuthStore.getState();
  if (!refreshToken) return null;
  try {
    const { data } = await axios.post(`${BASE_URL}/api/auth/refresh`, { refreshToken });
    setAccessToken(data.accessToken, data.refreshToken);
    return data.accessToken as string;
  } catch {
    cerrarSesion();
    return null;
  }
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as RequestConRetry | undefined;
    const esLoginORefresh = original?.url?.includes("/api/auth/login") || original?.url?.includes("/api/auth/refresh");

    if (error.response?.status === 401 && original && !original._retry && !esLoginORefresh) {
      original._retry = true;
      if (!refrescoEnCurso) {
        refrescoEnCurso = refrescarToken().finally(() => {
          refrescoEnCurso = null;
        });
      }
      const nuevoToken = await refrescoEnCurso;
      if (nuevoToken) {
        original.headers.set("Authorization", `Bearer ${nuevoToken}`);
        return apiClient(original);
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;

export function obtenerMensajeError(error: unknown, fallback = "Ocurrió un error inesperado."): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { mensaje?: string; detalles?: string[] } | undefined;
    if (data?.detalles?.length) return data.detalles.join(" · ");
    if (data?.mensaje) return data.mensaje;
  }
  return fallback;
}
