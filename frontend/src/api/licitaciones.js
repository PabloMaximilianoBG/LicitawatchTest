import client from "./client";

export const listarLicitaciones = () =>
  client.get("/api/licitaciones").then((r) => r.data);

export const obtenerLicitacion = (id) =>
  client.get(`/api/licitaciones/${id}`).then((r) => r.data);
