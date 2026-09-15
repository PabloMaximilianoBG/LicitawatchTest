import apiClient from "./client";
import type { LicitacionResponse, PerfilResponse, UsuarioAdminResponse, VentaAdminResponse } from "../types";

export async function listarUsuarios(): Promise<UsuarioAdminResponse[]> {
  const { data } = await apiClient.get<UsuarioAdminResponse[]>("/api/admin/usuarios");
  return data;
}

export async function obtenerUsuario(id: number): Promise<PerfilResponse> {
  const { data } = await apiClient.get<PerfilResponse>(`/api/admin/usuarios/${id}`);
  return data;
}

export async function actualizarActivoUsuario(id: number, activo: boolean): Promise<void> {
  await apiClient.put(`/api/admin/usuarios/${id}/activo`, { activo });
}

export async function crearAdministrador(email: string, contrasena: string, area: string): Promise<void> {
  await apiClient.post("/api/admin/usuarios/administradores", { email, contrasena, area });
}

export async function listarTodasLicitaciones(): Promise<LicitacionResponse[]> {
  const { data } = await apiClient.get<LicitacionResponse[]>("/api/licitaciones/todas");
  return data;
}

export async function listarTodasVentas(): Promise<VentaAdminResponse[]> {
  const { data } = await apiClient.get<VentaAdminResponse[]>("/api/ventas/todas");
  return data;
}
