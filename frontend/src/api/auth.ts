import apiClient from "./client";
import type { TokenResponse, UsuarioResumen } from "../types";

export interface RegistroEmpresaPayload {
  email: string;
  contrasena: string;
  razonSocial: string;
  rut: string;
  rubro: string;
}

export interface RegistroClientePayload {
  email: string;
  contrasena: string;
  nombreContacto: string;
  rut: string;
}

export async function registrarEmpresa(payload: RegistroEmpresaPayload): Promise<UsuarioResumen> {
  const { data } = await apiClient.post<UsuarioResumen>("/api/auth/registro/empresa", payload);
  return data;
}

export async function registrarCliente(payload: RegistroClientePayload): Promise<UsuarioResumen> {
  const { data } = await apiClient.post<UsuarioResumen>("/api/auth/registro/cliente", payload);
  return data;
}

export async function login(email: string, contrasena: string): Promise<TokenResponse> {
  const { data } = await apiClient.post<TokenResponse>("/api/auth/login", { email, contrasena });
  return data;
}

export async function logout(refreshToken: string): Promise<void> {
  await apiClient.post("/api/auth/logout", { refreshToken });
}
