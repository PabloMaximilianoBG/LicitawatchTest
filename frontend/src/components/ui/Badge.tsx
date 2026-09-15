type Tono = "brand" | "green" | "red" | "amber" | "slate";

const tonoClasses: Record<Tono, string> = {
  brand: "bg-brand-500/15 text-brand-300 ring-1 ring-inset ring-brand-500/30",
  green: "bg-emerald-500/15 text-emerald-300 ring-1 ring-inset ring-emerald-500/30",
  red: "bg-red-500/15 text-red-300 ring-1 ring-inset ring-red-500/30",
  amber: "bg-amber-500/15 text-amber-300 ring-1 ring-inset ring-amber-500/30",
  slate: "bg-slate-500/15 text-slate-300 ring-1 ring-inset ring-slate-500/30",
};

export default function Badge({ tono = "slate", children }: { tono?: Tono; children: React.ReactNode }) {
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium ${tonoClasses[tono]}`}>
      {children}
    </span>
  );
}

export function badgeEstadoLicitacion(estado: string): Tono {
  return estado === "PUBLICADA" ? "green" : "slate";
}

export function badgeEstadoPostulacion(estado: string): Tono {
  switch (estado) {
    case "ACEPTADA":
      return "green";
    case "RECHAZADA":
      return "red";
    case "EN_REVISION":
      return "amber";
    default:
      return "brand";
  }
}
