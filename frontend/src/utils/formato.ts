export function formatoMoneda(valor: number | null): string {
  if (valor === null || valor === undefined) return "No especificado";
  return new Intl.NumberFormat("es-CL", { style: "currency", currency: "CLP", maximumFractionDigits: 0 }).format(valor);
}

export function formatoFecha(fecha: string | null): string {
  if (!fecha) return "-";
  return new Intl.DateTimeFormat("es-CL", { day: "2-digit", month: "short", year: "numeric" }).format(new Date(fecha));
}
