"""Prueba E2E de LicitaWatch v2 contra el sistema real, SOLO a través del api-gateway (http://localhost:8080).

Uso:  python scripts/dev/e2e.py
Lee .env (JWT_SECRET para firmar los tokens de correo como lo hace MS.usuarios.bs, ADMIN_EMAIL/ADMIN_PASSWORD
y SMTP_USERNAME para usar alias +tag del buzón de pruebas). No imprime secretos.
"""
import base64
import hashlib
import hmac
import json
import os
import random
import sys
import time
import urllib.error
import urllib.request
import uuid

BASE = os.environ.get("GATEWAY", "http://localhost:8080")
RAIZ = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
ENV = {}
with open(os.path.join(RAIZ, ".env"), encoding="utf-8") as f:
    for linea in f:
        if "=" in linea and not linea.lstrip().startswith("#"):
            k, v = linea.rstrip("\n").split("=", 1)
            ENV[k.strip()] = v.strip()

OK, FALLOS = 0, []


def check(nombre, condicion, detalle=""):
    global OK
    if condicion:
        OK += 1
        print(f"  [OK] {nombre}")
    else:
        FALLOS.append(nombre)
        print(f"  [FALLA] {nombre} {detalle}")


def req(metodo, ruta, body=None, token=None, headers=None, raw=None, ctype=None, seguir=True):
    h = {"Accept": "application/json"}
    data = None
    if body is not None:
        data = json.dumps(body).encode("utf-8")
        h["Content-Type"] = "application/json"
    if raw is not None:
        data, h["Content-Type"] = raw, ctype
    if token:
        h["Authorization"] = "Bearer " + token
    h.update(headers or {})
    r = urllib.request.Request(BASE + ruta, data=data, method=metodo, headers=h)
    opener = urllib.request.build_opener() if seguir else urllib.request.build_opener(SinRedireccion)
    try:
        with opener.open(r, timeout=120) as resp:
            cuerpo = resp.read()
            return resp.status, (json.loads(cuerpo) if cuerpo and "json" in resp.headers.get("Content-Type", "") else cuerpo), resp.headers
    except urllib.error.HTTPError as e:
        cuerpo = e.read()
        try:
            return e.code, json.loads(cuerpo), e.headers
        except Exception:
            return e.code, cuerpo, e.headers


class SinRedireccion(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, *a, **k):
        return None


def b64(b):
    return base64.urlsafe_b64encode(b).rstrip(b"=").decode()


def token_correo(usuario_id, proposito, extra=None):
    """Mismo formato que TokenServiceImpl (clave derivada SHA-512(secret + ':' + propósito))."""
    clave = hashlib.sha512((ENV["JWT_SECRET"] + ":" + proposito).encode()).digest()
    ahora = int(time.time())
    payload = {"iss": "licitawatch", "sub": str(usuario_id), "proposito": proposito, "iat": ahora, "exp": ahora + 600}
    payload.update(extra or {})
    cab = b64(json.dumps({"alg": "HS512", "typ": "JWT"}).encode())
    cuerpo = b64(json.dumps(payload).encode())
    firma = b64(hmac.new(clave, f"{cab}.{cuerpo}".encode(), hashlib.sha512).digest())
    return f"{cab}.{cuerpo}.{firma}"


def rut_aleatorio():
    cuerpo = str(random.randint(10_000_000, 29_999_999))
    s, m = 0, 2
    for c in reversed(cuerpo):
        s += int(c) * m
        m = 2 if m == 7 else m + 1
    r = 11 - s % 11
    dv = "0" if r == 11 else "K" if r == 10 else str(r)
    return f"{cuerpo}-{dv}"


def correo(tag):
    usuario = ENV.get("SMTP_USERNAME", "licitawatch@gmail.com")
    local, dominio = usuario.split("@")
    return f"{local}+{tag}{SELLO}@{dominio}"


def multipart(nombre_archivo, contenido, tipo):
    limite = uuid.uuid4().hex
    cuerpo = (f"--{limite}\r\nContent-Disposition: form-data; name=\"archivo\"; filename=\"{nombre_archivo}\"\r\n"
              f"Content-Type: {tipo}\r\n\r\n").encode() + contenido + f"\r\n--{limite}--\r\n".encode()
    return cuerpo, f"multipart/form-data; boundary={limite}"


SELLO = time.strftime("%m%d%H%M%S")
print(f"== LicitaWatch v2 · E2E contra {BASE} (sello {SELLO})")

print("\n-- Catálogos (combobox desde la BD)")
s, rubros, _ = req("GET", "/api/catalogos/rubros")
check("rubros públicos", s == 200 and any(r["nombre"] == "Tecnología" for r in rubros))
s, regiones, _ = req("GET", "/api/catalogos/regiones")
check("16 regiones", s == 200 and len(regiones) == 16)
rm = next(r for r in regiones if r["nombre"].startswith("Metropolitana"))
s, ciudades, _ = req("GET", f"/api/catalogos/regiones/{rm['id']}/ciudades")
check("52 comunas en la RM", s == 200 and len(ciudades) == 52)
s, tamanos, _ = req("GET", "/api/catalogos/tamanos-empresa")
check("tamaños Pequeña/Mediana/Grande", [t["nombre"] for t in tamanos] == ["Pequeña", "Mediana", "Grande"])
tecnologia = next(r["id"] for r in rubros if r["nombre"] == "Tecnología")
santiago = next(c["id"] for c in ciudades if c["nombre"] == "Santiago")

print("\n-- Registro por perfil (usuario + perfil en una transacción)")
empresa = lambda tag: {"razonSocial": f"Empresa {tag} {SELLO} SpA", "rut": rut_aleatorio(), "nombreContacto": "Contacto Prueba",
                       "emailContacto": correo(tag + "c"), "telefono": "+56 9 1234 5678", "rubroId": tecnologia, "ciudadId": santiago,
                       "descripcionEmpresa": "Empresa de prueba E2E", "sitioWeb": None}
lic = dict(empresa("lic"), email=correo("lic"), password="Clave1234", confirmPassword="Clave1234")
s, r, _ = req("POST", "/api/auth/registro/licitador", lic)
check("registro Licitador 201", s == 201, r)
lic_id = r["usuario"]["usuarioId"] if s == 201 else None
check("cuenta nace sin confirmar", s == 201 and r["usuario"]["estadoCuenta"] == "PENDIENTE_CONFIRMACION")
s, r, _ = req("POST", "/api/auth/registro/licitador", dict(lic, email=correo("otro")))
check("RUT duplicado 409", s == 409 and r.get("codigo") == "RUT_DUPLICADO", r)
s, r, _ = req("POST", "/api/auth/registro/pyme", dict(empresa("pyme"), email=correo("pyme"), password="Clave1234", confirmPassword="Distinta1", tamanoEmpresaId=tamanos[0]["id"]))
check("confirmar contraseña: no coinciden 400", s == 400 and r.get("codigo") == "PASSWORDS_NO_COINCIDEN", r)
py = dict(empresa("pyme"), email=correo("pyme"), password="Clave1234", confirmPassword="Clave1234", tamanoEmpresaId=tamanos[0]["id"])
s, r, _ = req("POST", "/api/auth/registro/pyme", py)
check("registro Pyme 201", s == 201, r)
py_id = r["usuario"]["usuarioId"] if s == 201 else None
check("correo de confirmación enviado", s == 201 and r["correoEnviado"], r.get("mensaje"))

print("\n-- Confirmación de cuenta y login")
s, r, _ = req("POST", "/api/auth/login", {"email": lic["email"], "password": "Clave1234"})
check("login sin confirmar 403", s == 403 and r.get("codigo") == "CUENTA_NO_CONFIRMADA", r)
for uid in (lic_id, py_id):
    s, r, _ = req("POST", "/api/auth/confirmar-cuenta", {"token": token_correo(uid, "confirmar-cuenta")})
    check(f"confirmar cuenta {uid}", s == 200, r)
s, r, _ = req("POST", "/api/auth/login", {"email": lic["email"], "password": "mala1234"})
check("credenciales inválidas 401", s == 401)
s, r, _ = req("POST", "/api/auth/login", {"email": lic["email"], "password": "Clave1234"})
check("login Licitador", s == 200 and r["usuario"]["rol"] == "LICITADOR", r)
T_LIC = r.get("token")
s, r, _ = req("POST", "/api/auth/login", {"email": py["email"], "password": "Clave1234"})
check("login Pyme", s == 200 and r["usuario"]["rol"] == "PYME", r)
T_PY = r.get("token")
pyme_perfil = r["usuario"]["perfilId"]
s, r, _ = req("POST", "/api/auth/login", {"email": ENV["ADMIN_EMAIL"], "password": ENV["ADMIN_PASSWORD"]})
check("login Administrador", s == 200 and r["usuario"]["rol"] == "ADMINISTRADOR", r)
T_ADM = r.get("token")

print("\n-- Perfil (consulta con catálogos resueltos y edición)")
s, r, _ = req("GET", "/api/usuarios/me", token=T_PY)
check("GET /me resuelve rubro, ciudad, región y tamaño", s == 200 and r["rubroNombre"] == "Tecnología" and r["ciudadNombre"] == "Santiago"
      and r["regionNombre"].startswith("Metropolitana") and r["tamanoEmpresaNombre"] == "Pequeña", r)
check("Pyme Estándar sin insignia", r.get("premium") is False)
s, r, _ = req("PUT", "/api/usuarios/me", dict(empresa("lic"), razonSocial=f"Empresa lic {SELLO} Editada", rut="11111111-1"), token=T_LIC)
check("editar perfil (RUT no cambia)", s == 200 and r["razonSocial"].endswith("Editada") and r["rut"] == lic["rut"], r)

print("\n-- Publicación de licitaciones (Licitador, sin costo)")
cierre = time.strftime("%Y-%m-%d", time.localtime(time.time() + 10 * 86400))
lic_body = {"titulo": f"Soporte técnico TI {SELLO}", "descripcion": "Servicio de soporte técnico para 40 equipos durante 12 meses.",
            "rubroId": tecnologia, "regionId": rm["id"], "presupuestoMin": 1000000, "presupuestoMax": 3000000, "maxPostulantes": 2,
            "fechaCierre": cierre}
s, r, _ = req("POST", "/api/licitaciones", lic_body, token=T_PY)
check("Pyme no publica 403", s == 403)
s, L, _ = req("POST", "/api/licitaciones", lic_body, token=T_LIC)
check("publicar licitación 201 Abierta", s == 201 and L["estado"] == "Abierta", L)
lid = L["id"]
s, r, _ = req("POST", "/api/licitaciones", dict(lic_body, presupuestoMin=5, presupuestoMax=1), token=T_LIC)
check("presupuesto mín > máx 400", s == 400)
cuerpo, ct = multipart("bases.pdf", b"%PDF-1.4\n%prueba\n", "application/pdf")
s, r, _ = req("POST", f"/api/licitaciones/{lid}/archivo", raw=cuerpo, ctype=ct, token=T_LIC)
check("subir documento: URL generada y tipo PDF", s == 200 and r["tipoArchivo"] == "PDF" and "/api/licitaciones/archivos/" in r["archivoUrl"], r)
archivo_url = r.get("archivoUrl", "")
png = base64.b64decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==")
cuerpo, ct = multipart("foto.png", png, "image/png")
s, r, _ = req("POST", f"/api/licitaciones/{lid}/imagen", raw=cuerpo, ctype=ct, token=T_LIC)
check("subir imagen", s == 200 and r["imagenUrl"], r)
s, contenido, h = req("GET", archivo_url.replace(BASE, ""))
check("descargar documento", s == 200 and contenido.startswith(b"%PDF"))

print("\n-- Búsqueda y postulación (Pyme)")
s, r, _ = req("GET", f"/api/licitaciones?rubroId={tecnologia}&q={SELLO}", token=T_PY)
check("filtrar por rubroId", s == 200 and any(x["id"] == lid for x in r["content"]), r)
s, r, _ = req("POST", f"/api/licitaciones/{lid}/postulaciones", {"mensaje": "Tenemos experiencia en soporte TI."}, token=T_PY)
check("postular 201 Pendiente", s == 201 and r["estado"] == "Pendiente", r)
post_id = r.get("id")
s, r, _ = req("POST", f"/api/licitaciones/{lid}/postulaciones", {"mensaje": "otra"}, token=T_PY)
check("postular dos veces 409", s == 409)
s, r, _ = req("GET", "/api/postulaciones/uso", token=T_PY)
check("uso del plan 1/3", s == 200 and r["postulacionesMes"] >= 1 and r["limitePostulacionesMes"] == 3, r)
s, r, _ = req("GET", "/api/postulaciones/mias", token=T_PY)
check("ver estado de postulación", s == 200 and any(p["id"] == post_id and p["estado"] == "Pendiente" for p in r))

print("\n-- El Licitador decide (aprobar = adjudicar) y chat")
s, r, _ = req("GET", f"/api/licitaciones/{lid}/postulaciones", token=T_LIC)
check("ver postulantes", s == 200 and r[0]["pymeRazonSocial"].startswith("Empresa pyme"), r)
s, r, _ = req("GET", f"/api/licitaciones/{lid}/postulaciones", token=T_PY)
check("la Pyme no ve postulantes 403", s == 403)
s, r, _ = req("POST", "/api/chat/conversaciones", {"postulacionId": post_id}, token=T_LIC)
check("chat no disponible antes de aprobar", s == 409, r)
s, r, _ = req("PATCH", f"/api/postulaciones/{post_id}/aprobar", token=T_LIC)
check("aprobar postulación", s == 200 and r["estado"] == "Aprobada" and r["chatDisponible"], r)
s, r, _ = req("GET", f"/api/licitaciones/{lid}", token=T_LIC)
check("licitación Adjudicada", s == 200 and r["estado"] == "Adjudicada", r)
s, C, _ = req("POST", "/api/chat/conversaciones", {"postulacionId": post_id}, token=T_LIC)
check("Licitador abre el chat", s == 201 and C["pymeId"] == pyme_perfil, C)
cid = C.get("id")
s, r, _ = req("POST", f"/api/chat/conversaciones/{cid}/mensajes", {"contenido": "Hola, coordinemos el inicio."}, token=T_LIC)
check("Licitador escribe", s == 201 and r["propio"])
s, r, _ = req("GET", "/api/chat/conversaciones", token=T_PY)
check("la Pyme ve la conversación con 1 sin leer", s == 200 and any(c["id"] == cid and c["noLeidos"] == 1 for c in r), r)
s, r, _ = req("GET", f"/api/chat/conversaciones/{cid}/mensajes", token=T_PY)
check("la Pyme lee los mensajes", s == 200 and len(r) == 1 and not r[0]["propio"])
s, r, _ = req("POST", f"/api/chat/conversaciones/{cid}/mensajes", {"contenido": "Perfecto, quedo atento."}, token=T_PY)
check("la Pyme responde", s == 201)
s, r, _ = req("GET", f"/api/chat/conversaciones/{cid}/mensajes", token=T_ADM)
check("el Administrador no lee chats privados 403", s == 403)

print("\n-- Suscripción y pago Premium (Webpay Plus sandbox)")
s, r, _ = req("GET", "/api/planes")
check("planes públicos Estándar $0 y Premium de pago", s == 200 and [p["nombre"] for p in r] == ["Estándar", "Premium"] and r[1]["precio"] > 0, r)
s, r, _ = req("POST", "/api/ventas/premium", token=T_LIC)
check("el Licitador no paga 403", s == 403)
s, P, _ = req("POST", "/api/ventas/premium", token=T_PY)
check("iniciar pago Premium (token Webpay sandbox)", s == 201 and P["token"] and "webpay3gint.transbank.cl" in P["url"], P)
s, r, _ = req("GET", "/api/suscripciones/mi", token=T_PY)
check("plan Estándar con compra Pendiente", s == 200 and r["plan"] == "Estándar" and r["ventaPendiente"]["estadoPago"] == "Pendiente", r)
s, r, h = req("POST", "/api/pagos/webpay/retorno", raw=f"TBK_TOKEN=x&TBK_ORDEN_COMPRA=LW-V{P['ventaId']}-1&TBK_ID_SESION=U1".encode(),
              ctype="application/x-www-form-urlencoded", seguir=False)
check("pago anulado redirige al frontend y queda pendiente", s == 302 and "estado=anulado" in h.get("Location", ""), h.get("Location"))
s, r, _ = req("POST", "/api/ventas/premium", token=T_PY)
check("reintento reutiliza la misma venta pendiente", s == 201 and r["ventaId"] == P["ventaId"], r)

print("\n-- LicitAsist")
s, r, _ = req("GET", "/api/asistente/estado", token=T_PY)
check("Pyme Estándar sin acceso", s == 200 and r["acceso"] is False, r)
s, r, _ = req("POST", "/api/asistente/chat", {"mensaje": "hola"}, token=T_PY)
check("Pyme Estándar 403 PREMIUM_REQUERIDO", s == 403 and r.get("codigo") == "PREMIUM_REQUERIDO", r)
s, r, _ = req("GET", "/api/asistente/estado", token=T_LIC)
check("Licitador con acceso libre", s == 200 and r["acceso"] is True, r)
if ENV.get("GROQ_API_KEY"):
    s, r, _ = req("POST", "/api/asistente/chat", {"mensaje": "¿Cuántas licitaciones tengo y en qué estado están?"}, token=T_LIC)
    check("LicitAsist responde con datos reales (Groq)", s == 200 and r["respuesta"] and r["fuentes"], r if s != 200 else "")

print("\n-- Soporte y avisos por correo")
s, r, _ = req("POST", "/api/soporte", {"asunto": "Prueba E2E", "mensaje": "Mensaje de prueba de soporte."}, token=T_PY)
check("soporte estándar", s == 200 and r["nivel"] == "Estándar", r)
time.sleep(3)
s, r, _ = req("GET", "/api/notificaciones/mias", token=T_LIC)
check("aviso 'Postulación recibida' registrado para el Licitador", s == 200 and any(n["tipo"] == "Postulación recibida" for n in r["content"]), r)
s, r, _ = req("GET", "/api/notificaciones/mias", token=T_PY)
check("aviso 'Postulación aprobada' y 'Mensaje nuevo' para la Pyme",
      s == 200 and {"Postulación aprobada", "Mensaje nuevo"} <= {n["tipo"] for n in r["content"]}, r)

print("\n-- Recuperar contraseña desde el login")
s, r, _ = req("POST", "/api/auth/recuperar-password", {"email": py["email"]})
check("respuesta genérica", s == 200)
s, me, _ = req("GET", "/api/usuarios/me", token=T_PY)
# El token real viaja por correo: aquí se firma igual que el backend usando la huella del hash actual (no disponible
# desde la API), por lo que se valida que un token con huella incorrecta sea rechazado.
s, r, _ = req("POST", "/api/auth/restablecer-password", {"token": token_correo(py_id, "restablecer-password", {"huella": "000"}),
                                                         "password": "Nueva1234", "confirmPassword": "Nueva1234"})
check("token de restablecimiento inválido/usado 400", s == 400 and r.get("codigo") == "TOKEN_INVALIDO", r)

print("\n-- Panel de administración")
s, r, _ = req("GET", "/api/admin/usuarios?q=" + SELLO, token=T_ADM)
check("listar usuarios", s == 200 and r["totalElements"] >= 2, r)
s, r, _ = req("PATCH", f"/api/admin/usuarios/{lic_id}/estado", {"activo": False}, token=T_ADM)
check("desactivar cuenta", s == 200 and r["estadoCuenta"] == "DESACTIVADA", r)
s, r, _ = req("POST", "/api/auth/login", {"email": lic["email"], "password": "Clave1234"})
check("login desactivada 403", s == 403 and r.get("codigo") == "CUENTA_DESACTIVADA", r)
s, r, _ = req("POST", "/api/auth/confirmar-cuenta", {"token": token_correo(lic_id, "confirmar-cuenta")})
check("no se reactiva con un enlace de confirmación", s == 403, r)
s, r, _ = req("PATCH", f"/api/admin/usuarios/{lic_id}/estado", {"activo": True}, token=T_ADM)
check("reactivar cuenta", s == 200 and r["estadoCuenta"] == "ACTIVA")
nuevo_admin = {"email": correo("adm"), "password": "Clave1234", "confirmPassword": "Clave1234", "rol": "ADMINISTRADOR",
               "nombre": "Admin de prueba", "area": "Operaciones"}
s, r, _ = req("POST", "/api/admin/usuarios", nuevo_admin, token=T_ADM)
check("crear otro Administrador", s == 201 and r["rol"] == "ADMINISTRADOR", r)
s, r, _ = req("PATCH", f"/api/admin/usuarios/{py_id}/rol", {"rol": "ADMINISTRADOR", "nombre": "Pyme ascendida", "area": "Soporte"}, token=T_ADM)
check("dar Administrador a otra persona", s == 200 and r["rol"] == "ADMINISTRADOR", r)
s, r, _ = req("PATCH", f"/api/admin/usuarios/{py_id}/rol", {"rol": "PYME"}, token=T_ADM)
check("devolver el rol Pyme (conserva su perfil)", s == 200 and r["rol"] == "PYME" and r["razonSocial"], r)
s, r, _ = req("GET", "/api/admin/licitaciones?q=" + SELLO, token=T_ADM)
check("moderar: listar licitaciones", s == 200 and r["totalElements"] >= 1)
s, r, _ = req("GET", "/api/admin/ventas/resumen", token=T_ADM)
check("ver ventas", s == 200 and r["ventasPendientes"] >= 1, r)
s, r, _ = req("GET", "/api/admin/suscripciones?plan=Premium", token=T_ADM)
check("ver suscripciones (Pendiente de pago)", s == 200 and any(x["estadoVisible"] == "Pendiente de pago" for x in r["content"]), r)
s, r, _ = req("POST", "/api/admin/catalogos/rubros", {"nombre": f"Rubro {SELLO}"}, token=T_ADM)
check("agregar rubro al catálogo", s == 201, r)
s, L2, _ = req("POST", "/api/licitaciones", dict(lic_body, titulo=f"Licitación a eliminar {SELLO}"), token=T_LIC)
s, r, _ = req("PATCH", f"/api/admin/licitaciones/{L2['id']}/estado", {"estado": "Cerrada"}, token=T_ADM)
check("moderar: cerrar licitación", s == 200 and r["estado"] == "Cerrada", r)
s, r, _ = req("DELETE", f"/api/admin/licitaciones/{L2['id']}", token=T_ADM)
check("moderar: eliminar licitación (borrado físico)", s == 204)
s, r, _ = req("GET", f"/api/licitaciones/{L2['id']}", token=T_LIC)
check("licitación eliminada 404", s == 404)

print("\n-- Seguridad del api-gateway")
s, r, _ = req("GET", "/api/usuarios/me")
check("sin token 401", s == 401)
s, r, _ = req("GET", "/api/admin/usuarios", token=T_LIC)
check("Licitador en panel admin 403", s == 403)
s, r, _ = req("GET", "/api/licitaciones?q=1%27%20OR%20%271%27=%271", token=T_PY)
check("SQL injection 400", s == 400)
s, r, _ = req("GET", "/api/usuarios/me", token=token_correo(py_id, "confirmar-cuenta"))
check("token de correo no sirve como sesión 401", s == 401)

print(f"\n== Resultado: {OK} OK, {len(FALLOS)} fallas")
for f in FALLOS:
    print("   -", f)
sys.exit(1 if FALLOS else 0)
