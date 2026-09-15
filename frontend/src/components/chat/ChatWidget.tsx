import { useEffect, useRef, useState } from "react";
import { enviarMensajeChat } from "../../api/asistente";
import { obtenerMensajeError } from "../../api/client";

interface Mensaje {
  id: string;
  rol: "user" | "assistant";
  texto: string;
}

const SUGERENCIAS = [
  "¿Qué puedes hacer por mí?",
  "¿Cuáles son las diferencias entre los planes?",
  "Recomiéndame licitaciones activas",
];

function IconoChispa({ className = "h-6 w-6" }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={className}>
      <path
        d="M12 2l1.8 5.2L19 9l-5.2 1.8L12 16l-1.8-5.2L5 9l5.2-1.8L12 2z"
        fill="currentColor"
      />
      <path d="M19 15l.8 2.2L22 18l-2.2.8L19 21l-.8-2.2L16 18l2.2-.8L19 15z" fill="currentColor" opacity="0.7" />
    </svg>
  );
}

function IconoEnviar({ className = "h-5 w-5" }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={className}>
      <path d="M4 12L20 4L14 20L11 13L4 12Z" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" />
    </svg>
  );
}

function IconoCerrar({ className = "h-5 w-5" }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={className}>
      <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  );
}

function BurbujaEscribiendo() {
  return (
    <div className="flex items-center gap-1.5 rounded-2xl rounded-bl-sm bg-white/10 px-4 py-3">
      <span className="h-1.5 w-1.5 animate-blink rounded-full bg-slate-300" style={{ animationDelay: "0ms" }} />
      <span className="h-1.5 w-1.5 animate-blink rounded-full bg-slate-300" style={{ animationDelay: "160ms" }} />
      <span className="h-1.5 w-1.5 animate-blink rounded-full bg-slate-300" style={{ animationDelay: "320ms" }} />
    </div>
  );
}

export default function ChatWidget() {
  const [abierto, setAbierto] = useState(false);
  const [mensajes, setMensajes] = useState<Mensaje[]>([]);
  const [entrada, setEntrada] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const finRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    finRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [mensajes, enviando, abierto]);

  async function enviar(texto: string) {
    const contenido = texto.trim();
    if (!contenido || enviando) return;

    setError(null);
    setMensajes((prev) => [...prev, { id: crypto.randomUUID(), rol: "user", texto: contenido }]);
    setEntrada("");
    setEnviando(true);

    try {
      const { respuesta } = await enviarMensajeChat(contenido);
      setMensajes((prev) => [...prev, { id: crypto.randomUUID(), rol: "assistant", texto: respuesta }]);
    } catch (err) {
      setError(obtenerMensajeError(err, "LicitAsist no pudo responder. Intenta nuevamente en unos minutos."));
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="fixed bottom-6 right-6 z-50 flex flex-col items-end gap-4">
      {abierto && (
        <div className="flex h-[32rem] w-[23rem] max-w-[calc(100vw-3rem)] flex-col overflow-hidden rounded-3xl border border-white/10 bg-slate-900/90 shadow-2xl shadow-black/50 backdrop-blur-xl animate-slide-up">
          <div className="flex items-center justify-between border-b border-white/10 bg-gradient-to-r from-brand-600/90 to-violet-600/90 px-4 py-3.5">
            <div className="flex items-center gap-2.5">
              <div className="flex h-9 w-9 items-center justify-center rounded-full bg-white/15 text-white">
                <IconoChispa className="h-5 w-5" />
              </div>
              <div>
                <p className="text-sm font-bold leading-tight text-white">LicitAsist</p>
                <p className="text-[11px] leading-tight text-white/75">Asistente con IA · Groq</p>
              </div>
            </div>
            <button
              onClick={() => setAbierto(false)}
              className="rounded-lg p-1.5 text-white/80 transition-colors hover:bg-white/15 hover:text-white"
              aria-label="Cerrar chat"
            >
              <IconoCerrar />
            </button>
          </div>

          <div className="flex-1 space-y-3 overflow-y-auto px-4 py-4">
            {mensajes.length === 0 && (
              <div className="animate-fade-in space-y-3">
                <div className="rounded-2xl rounded-bl-sm bg-white/10 px-4 py-3 text-sm text-slate-200">
                  Hola 👋 Soy <strong>LicitAsist</strong>. Puedo ayudarte con tus licitaciones, postulaciones y
                  planes usando tus datos reales de LicitaWatch. ¿En qué te ayudo?
                </div>
                <div className="flex flex-wrap gap-2">
                  {SUGERENCIAS.map((s) => (
                    <button
                      key={s}
                      onClick={() => enviar(s)}
                      className="rounded-full border border-white/10 bg-white/5 px-3 py-1.5 text-xs text-slate-300 transition-colors hover:border-brand-400/50 hover:bg-white/10 hover:text-white"
                    >
                      {s}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {mensajes.map((m) => (
              <div key={m.id} className={`flex animate-slide-up ${m.rol === "user" ? "justify-end" : "justify-start"}`}>
                <div
                  className={`max-w-[85%] whitespace-pre-wrap rounded-2xl px-4 py-2.5 text-sm leading-relaxed ${
                    m.rol === "user"
                      ? "rounded-br-sm bg-gradient-to-br from-brand-600 to-violet-600 text-white"
                      : "rounded-bl-sm bg-white/10 text-slate-100"
                  }`}
                >
                  {m.texto}
                </div>
              </div>
            ))}

            {enviando && (
              <div className="flex justify-start animate-fade-in">
                <BurbujaEscribiendo />
              </div>
            )}

            {error && (
              <div className="rounded-xl border border-red-500/30 bg-red-500/10 px-3 py-2 text-xs text-red-300">
                {error}
              </div>
            )}

            <div ref={finRef} />
          </div>

          <form
            onSubmit={(e) => {
              e.preventDefault();
              enviar(entrada);
            }}
            className="border-t border-white/10 p-3"
          >
            <div className="flex items-center gap-2 rounded-2xl border border-white/10 bg-white/5 px-3 py-2 focus-within:border-brand-400/60 focus-within:ring-2 focus-within:ring-brand-500/20">
              <input
                value={entrada}
                onChange={(e) => setEntrada(e.target.value)}
                placeholder="Escribe tu mensaje..."
                className="flex-1 bg-transparent text-sm text-slate-100 placeholder:text-slate-500 outline-none"
                disabled={enviando}
                maxLength={2000}
              />
              <button
                type="submit"
                disabled={enviando || !entrada.trim()}
                className="flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 to-violet-600 text-white transition-transform disabled:opacity-40 enabled:hover:scale-105"
                aria-label="Enviar"
              >
                <IconoEnviar className="h-4 w-4" />
              </button>
            </div>
            <p className="mt-2 px-1 text-[10px] leading-tight text-slate-500">
              LicitAsist es un asistente de IA (no una persona) impulsado por la API externa de Groq, sujeta a
              límites de uso — no está disponible de forma ilimitada.
            </p>
          </form>
        </div>
      )}

      <button
        onClick={() => setAbierto((v) => !v)}
        className="group relative flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-brand-500 to-violet-600 text-white shadow-glow transition-transform hover:scale-105 active:scale-95"
        aria-label="Abrir LicitAsist"
      >
        <span className="absolute inset-0 animate-ping rounded-full bg-brand-500/40 group-hover:hidden" />
        {abierto ? <IconoCerrar className="h-6 w-6" /> : <IconoChispa className="h-7 w-7" />}
      </button>
    </div>
  );
}
