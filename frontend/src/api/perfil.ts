import apiClient from "./client";
import type { PerfilResponse } from "../types";

export async function obtenerMiPerfil(): Promise<PerfilResponse> {
  const { data } = await apiClient.get<PerfilResponse>("/api/perfil");
  return data;
}

export async function actualizarPerfilEmpresa(razonSocial: string, rubro: string): Promise<PerfilResponse> {
  const { data } = await apiClient.put<PerfilResponse>("/api/perfil/empresa", { razonSocial, rubro });
  return data;
}

export async function actualizarPerfilCliente(nombreContacto: string): Promise<PerfilResponse> {
  const { data } = await apiClient.put<PerfilResponse>("/api/perfil/cliente", { nombreContacto });
  return data;
}
