import client from "./client";

export const listarNotificaciones = (usuarioId) =>
  client.get(`/api/notificaciones/usuario/${usuarioId}`).then((r) => r.data);
