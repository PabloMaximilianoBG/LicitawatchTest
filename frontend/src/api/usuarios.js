import client from "./client";

export const registrarUsuario = (usuario) =>
  client.post("/api/usuarios", usuario).then((r) => r.data);

export const obtenerUsuario = (id) =>
  client.get(`/api/usuarios/${id}`).then((r) => r.data);

export const listarPreferencias = (usuarioId) =>
  client.get(`/api/usuarios/${usuarioId}/preferencias`).then((r) => r.data);

export const crearPreferencia = (usuarioId, preferencia) =>
  client.post(`/api/usuarios/${usuarioId}/preferencias`, preferencia).then((r) => r.data);

export const actualizarPreferencia = (usuarioId, id, cambios) =>
  client.put(`/api/usuarios/${usuarioId}/preferencias/${id}`, cambios).then((r) => r.data);

export const eliminarPreferencia = (usuarioId, id) =>
  client.delete(`/api/usuarios/${usuarioId}/preferencias/${id}`);
