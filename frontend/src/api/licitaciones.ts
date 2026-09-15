import apiClient from "./client";
import type { EstadoPostulacion, LicitacionResponse, PostulacionResponse } from "../types";

export interface CrearLicitacionPayload {
  titulo: string;
  rubro: string;
  montoEstimado: number | null;
  region: string;
  fechaCierre: string;
}

export async function buscarLicitaciones(params: { rubro?: string; region?: string } = {}): Promise<LicitacionResponse[]> {
  const { data } = await apiClient.get<LicitacionResponse[]>("/api/licitaciones", { params });
  return data;
}

export async function obtenerLicitacion(id: number): Promise<LicitacionResponse> {
  const { data } = await apiClient.get<LicitacionResponse>(`/api/licitaciones/${id}`);
  return data;
}

export async function misLicitaciones(): Promise<LicitacionResponse[]> {
  const { data } = await apiClient.get<LicitacionResponse[]>("/api/licitaciones/mias");
  return data;
}

export async function crearLicitacion(payload: CrearLicitacionPayload): Promise<LicitacionResponse> {
  const { data } = await apiClient.post<LicitacionResponse>("/api/licitaciones", payload);
  return data;
}

export async function actualizarLicitacion(id: number, payload: CrearLicitacionPayload): Promise<LicitacionResponse> {
  const { data } = await apiClient.put<LicitacionResponse>(`/api/licitaciones/${id}`, payload);
  return data;
}

export async function cerrarLicitacion(id: number): Promise<LicitacionResponse> {
  const { data } = await apiClient.put<LicitacionResponse>(`/api/licitaciones/${id}/cerrar`);
  return data;
}

export async function postular(licitacionId: number, propuesta: string): Promise<PostulacionResponse> {
  const { data } = await apiClient.post<PostulacionResponse>(`/api/licitaciones/${licitacionId}/postulaciones`, { propuesta });
  return data;
}

export async function postulacionesDeLicitacion(licitacionId: number): Promise<PostulacionResponse[]> {
  const { data } = await apiClient.get<PostulacionResponse[]>(`/api/licitaciones/${licitacionId}/postulaciones`);
  return data;
}

export async function actualizarEstadoPostulacion(
  licitacionId: number,
  postulacionId: number,
  estado: EstadoPostulacion
): Promise<PostulacionResponse> {
  const { data } = await apiClient.put<PostulacionResponse>(
    `/api/licitaciones/${licitacionId}/postulaciones/${postulacionId}/estado`,
    { estado }
  );
  return data;
}

export async function misPostulaciones(): Promise<PostulacionResponse[]> {
  const { data } = await apiClient.get<PostulacionResponse[]>("/api/postulaciones/mias");
  return data;
}
