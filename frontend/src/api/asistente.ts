import apiClient from "./client";
import type { ChatResponse } from "../types";

export async function enviarMensajeChat(mensaje: string): Promise<ChatResponse> {
  const { data } = await apiClient.post<ChatResponse>("/api/asistente/chat", { mensaje });
  return data;
}

export async function obtenerUsoAsistente(): Promise<{ usoDelUsuario: number; usoGlobal: number }> {
  const { data } = await apiClient.get("/api/asistente/uso");
  return data;
}
