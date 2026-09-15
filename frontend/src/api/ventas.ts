import apiClient from "./client";
import type { IniciarVentaResponse, PagoResponse, PlanResponse, SuscripcionResponse } from "../types";

export async function obtenerPlanes(): Promise<PlanResponse[]> {
  const { data } = await apiClient.get<PlanResponse[]>("/api/planes");
  return data;
}

export async function iniciarVenta(planId: number): Promise<IniciarVentaResponse> {
  const { data } = await apiClient.post<IniciarVentaResponse>("/api/ventas/iniciar", { planId });
  return data;
}

export async function confirmarPago(tokenWs: string): Promise<PagoResponse> {
  const { data } = await apiClient.post<PagoResponse>("/api/ventas/confirmar", { tokenWs });
  return data;
}

export async function miSuscripcion(): Promise<SuscripcionResponse> {
  const { data } = await apiClient.get<SuscripcionResponse>("/api/suscripciones/mia");
  return data;
}
