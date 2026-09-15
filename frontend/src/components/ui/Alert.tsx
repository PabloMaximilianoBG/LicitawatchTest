type Tipo = "error" | "success" | "info";

const clasesPorTipo: Record<Tipo, string> = {
  error: "bg-red-500/10 border-red-500/30 text-red-300",
  success: "bg-emerald-500/10 border-emerald-500/30 text-emerald-300",
  info: "bg-brand-500/10 border-brand-500/30 text-brand-200",
};

export default function Alert({ tipo = "info", children }: { tipo?: Tipo; children: React.ReactNode }) {
  return (
    <div className={`rounded-xl border px-4 py-3 text-sm animate-fade-in ${clasesPorTipo[tipo]}`}>{children}</div>
  );
}
