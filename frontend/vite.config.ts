import { defineConfig, type Plugin } from "vite";
import react from "@vitejs/plugin-react";

/**
 * Webpay Plus vuelve del formulario hospedado con un POST (no un GET) al
 * returnUrl configurado (TRANSBANK_RETURN_URL=.../pago/resultado). Una SPA
 * no puede leer el cuerpo de un POST despues de que el navegador ya
 * completo la navegacion, asi que este plugin intercepta ese POST en el
 * dev server, extrae "token_ws" (o el aviso de cancelacion "TBK_TOKEN") y
 * redirige (302) a la misma ruta como GET con el dato en el query string,
 * que si es legible por la pagina de React. Solo aplica en `npm run dev`;
 * un despliegue real necesitaria un endpoint de servidor equivalente, fuera
 * de alcance para este proyecto 100% local (ver MEMORIA.md).
 */
function webpayReturnBridge(): Plugin {
  return {
    name: "webpay-return-bridge",
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        if (req.method === "POST" && req.url?.startsWith("/pago/resultado")) {
          let body = "";
          req.on("data", (chunk) => {
            body += chunk;
          });
          req.on("end", () => {
            const params = new URLSearchParams(body);
            const tokenWs = params.get("token_ws");
            const tbkToken = params.get("TBK_TOKEN");
            const query = tokenWs
              ? `?token_ws=${encodeURIComponent(tokenWs)}`
              : tbkToken
                ? "?cancelado=1"
                : "";
            res.writeHead(303, { Location: `/pago/resultado${query}` });
            res.end();
          });
          return;
        }
        next();
      });
    },
  };
}

export default defineConfig({
  plugins: [react(), webpayReturnBridge()],
  server: {
    // Sin esto, Vite se enlaza solo a ::1 (IPv6) y queda inconsistente con
    // el resto del proyecto, que siempre usa 127.0.0.1 explicito.
    host: "127.0.0.1",
    port: 5173,
  },
});
