import client from "./client";

export const listarCoincidencias = (usuarioId) =>
  client.get(`/api/coincidencias/usuario/${usuarioId}`).then((r) => r.data);
