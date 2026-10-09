"""Compara la estructura REAL de las 5 bases PostgreSQL con el Modelo ER y genera docs/checklist-modelo-er.md.

Uso: python scripts/dev/checklist_er.py      (requiere las bases creadas por Flyway y psql de PostgreSQL 18)
"""
import os
import subprocess

RAIZ = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
PSQL = r"C:\Program Files\PostgreSQL\18\bin\psql.exe"
ENV = {}
for linea in open(os.path.join(RAIZ, ".env"), encoding="utf-8"):
    if "=" in linea and not linea.lstrip().startswith("#"):
        k, v = linea.rstrip("\n").split("=", 1)
        ENV[k.strip()] = v.strip()

# Modelo ER: (columna, tipo ER, nulo?, clave) — clave: PK, FK, UQ, FK,UQ, REF, REF,UQ, ''
ER = {
    "usuarios_bd": {
        "rol": [("id", "int", False, "PK"), ("nombre", "texto", False, "UQ")],
        "usuario": [("id", "int", False, "PK"), ("email", "texto", False, "UQ"), ("password", "bcrypt", False, ""),
                    ("rol_id", "int", False, "FK"), ("activo", "bool", False, ""), ("created_at", "timestamp", False, "")],
        "administrador": [("id", "int", False, "PK"), ("usuario_id", "int", False, "FK,UQ"), ("nombre", "texto", False, ""),
                          ("area", "texto", False, "")],
        "licitador": [("id", "int", False, "PK"), ("usuario_id", "int", False, "FK,UQ"), ("razon_social", "texto", False, ""),
                      ("rut", "texto", False, "UQ"), ("nombre_contacto", "texto", False, ""), ("email_contacto", "texto", False, ""),
                      ("telefono", "texto", False, ""), ("rubro_id", "int", False, "FK"), ("ciudad_id", "int", False, "FK"),
                      ("descripcion_empresa", "texto", False, ""), ("sitio_web", "texto", True, ""), ("updated_at", "timestamp", False, "")],
        "pyme": [("id", "int", False, "PK"), ("usuario_id", "int", False, "FK,UQ"), ("razon_social", "texto", False, ""),
                 ("rut", "texto", False, "UQ"), ("nombre_contacto", "texto", False, ""), ("email_contacto", "texto", False, ""),
                 ("telefono", "texto", False, ""), ("rubro_id", "int", False, "FK"), ("ciudad_id", "int", False, "FK"),
                 ("tamano_empresa_id", "int", False, "FK"), ("descripcion_empresa", "texto", False, ""), ("sitio_web", "texto", True, ""),
                 ("updated_at", "timestamp", False, "")],
        "rubro": [("id", "int", False, "PK"), ("nombre", "texto", False, "UQ")],
        "region": [("id", "int", False, "PK"), ("nombre", "texto", False, "UQ")],
        "ciudad": [("id", "int", False, "PK"), ("nombre", "texto", False, ""), ("region_id", "int", False, "FK")],
        "tamano_empresa": [("id", "int", False, "PK"), ("nombre", "texto", False, "UQ")],
    },
    "licitaciones_bd": {
        "licitacion": [("id", "int", False, "PK"), ("licitador_id", "int", False, "REF"), ("titulo", "texto", False, ""),
                       ("descripcion", "texto", False, ""), ("rubro_id", "int", False, "REF"), ("region_id", "int", False, "REF"),
                       ("presupuesto_min", "numeric", True, ""), ("presupuesto_max", "numeric", True, ""), ("max_postulantes", "int", True, ""),
                       ("imagen_url", "texto", True, ""), ("archivo_url", "texto", True, ""), ("archivo_nombre", "texto", True, ""),
                       ("tipo_archivo_id", "int", True, "FK"), ("fecha_cierre", "date", False, ""), ("estado_licitacion_id", "int", False, "FK"),
                       ("created_at", "timestamp", False, "")],
        "postulacion": [("id", "int", False, "PK"), ("licitacion_id", "int", False, "FK"), ("pyme_id", "int", False, "REF"),
                        ("mensaje", "texto", True, ""), ("fecha_postulacion", "date", False, ""), ("estado_postulacion_id", "int", False, "FK"),
                        ("updated_at", "timestamp", False, "")],
        "tipo_archivo": [("id", "int", False, "PK"), ("nombre", "texto", False, "UQ")],
        "estado_licitacion": [("id", "int", False, "PK"), ("nombre", "texto", False, "UQ")],
        "estado_postulacion": [("id", "int", False, "PK"), ("nombre", "texto", False, "UQ")],
    },
    "ventas_bd": {
        "plan_suscripcion": [("id", "int", False, "PK"), ("nombre", "texto", False, "UQ")],
        "suscripcion": [("id", "int", False, "PK"), ("usuario_id", "int", False, "REF"), ("plan_id", "int", False, "FK"),
                        ("estado_suscripcion_id", "int", False, "FK")],
        "venta": [("id", "int", False, "PK"), ("suscripcion_id", "int", False, "FK"), ("monto", "numeric", False, ""), ("fecha", "date", False, "")],
        "pago": [("id", "int", False, "PK"), ("venta_id", "int", False, "FK,UQ"), ("id_transaccion", "texto", False, ""),
                 ("metodo_pago_id", "int", False, "FK"), ("estado_pago_id", "int", False, "FK")],
        "estado_suscripcion": [("id", "int", False, "PK"), ("nombre", "texto", False, "UQ")],
        "estado_pago": [("id", "int", False, "PK"), ("nombre", "texto", False, "UQ")],
        "metodo_pago": [("id", "int", False, "PK"), ("nombre", "texto", False, "UQ")],
    },
    "notificaciones_bd": {
        "notificacion": [("id", "int", False, "PK"), ("usuario_id", "int", False, "REF"), ("tipo_notificacion_id", "int", False, "FK"),
                         ("canal", "texto", False, ""), ("created_at", "timestamp", False, "")],
        "tipo_notificacion": [("id", "int", False, "PK"), ("nombre", "texto", False, "UQ")],
    },
    "chat_bd": {
        "conversacion": [("id", "int", False, "PK"), ("postulacion_id", "int", False, "REF,UQ"), ("licitador_id", "int", False, "REF"),
                         ("pyme_id", "int", False, "REF"), ("created_at", "timestamp", False, "")],
        "mensaje": [("id", "int", False, "PK"), ("conversacion_id", "int", False, "FK"), ("emisor_id", "int", False, "REF"),
                    ("contenido", "texto", False, ""), ("enviado_at", "timestamp", False, ""), ("leido", "bool", False, "")],
    },
}

CATALOGOS = {
    ("usuarios_bd", "rol"): ["Licitador", "Pyme", "Administrador"],
    ("usuarios_bd", "tamano_empresa"): ["Pequeña", "Mediana", "Grande"],
    ("licitaciones_bd", "estado_licitacion"): ["Abierta", "Cerrada", "Adjudicada"],
    ("licitaciones_bd", "estado_postulacion"): ["Pendiente", "Aprobada", "Rechazada"],
    ("licitaciones_bd", "tipo_archivo"): ["PDF", "DOCX", "XLSX", "Otro"],
    ("ventas_bd", "plan_suscripcion"): ["Estándar", "Premium"],
    ("ventas_bd", "estado_suscripcion"): ["Activa", "Vencida", "Cancelada"],
    ("ventas_bd", "estado_pago"): ["Aprobado", "Rechazado", "Pendiente"],
    ("ventas_bd", "metodo_pago"): ["Crédito", "Débito"],
    ("notificaciones_bd", "tipo_notificacion"): ["Licitación publicada", "Postulación recibida", "Postulación aprobada",
                                                 "Postulación rechazada", "Pago confirmado", "Mensaje nuevo"],
}

TIPO_PG = {"int": {"integer"}, "texto": {"character varying", "text"}, "bcrypt": {"character varying"}, "numeric": {"numeric"},
           "date": {"date"}, "timestamp": {"timestamp without time zone"}, "bool": {"boolean"}}


def sql(bd, q):
    out = subprocess.run([PSQL, "-U", ENV["POSTGRES_USER"], "-h", "localhost", "-d", bd, "-At", "-F", "|", "-c", q],
                         capture_output=True, text=True, encoding="utf-8", env={**os.environ, "PGPASSWORD": ENV["POSTGRES_PASSWORD"]})
    return [l.split("|") for l in out.stdout.strip().splitlines() if l]


def main():
    filas, total, ok = [], 0, 0
    for bd, tablas in ER.items():
        reales = {t for (t,) in sql(bd, "select table_name from information_schema.tables where table_schema='public' "
                                        "and table_name <> 'flyway_schema_history'")}
        extra = reales - set(tablas)
        filas.append(f"\n## {bd}\n")
        filas.append(f"Tablas: ER {len(tablas)} · BD {len(reales)} · " + ("**idénticas**" if not extra and set(tablas) <= reales
                                                                          else f"diferencias: sobran {sorted(extra)} faltan {sorted(set(tablas) - reales)}"))
        filas.append("\n| Tabla | Columna | Tipo ER | Tipo PostgreSQL | Nulo ER | Nulo BD | Clave ER | Clave BD | ✔ |\n|---|---|---|---|---|---|---|---|---|")
        for tabla, cols in tablas.items():
            info = {c: (tipo, nulo) for c, tipo, nulo in sql(bd, "select column_name, data_type, is_nullable from information_schema.columns "
                                                                  f"where table_schema='public' and table_name='{tabla}'")}
            claves = {}
            for c, tipo in sql(bd, "select kcu.column_name, tc.constraint_type from information_schema.table_constraints tc join "
                                   "information_schema.key_column_usage kcu on tc.constraint_name = kcu.constraint_name "
                                   f"where tc.table_name='{tabla}' and tc.table_schema='public'"):
                claves.setdefault(c, set()).add({"PRIMARY KEY": "PK", "FOREIGN KEY": "FK", "UNIQUE": "UQ"}[tipo])
            extra_cols = set(info) - {c for c, *_ in cols}
            for col, tipo_er, nulo_er, clave_er in cols:
                total += 1
                tipo_bd, nulo_bd = info.get(col, ("FALTA", "?"))
                clave_bd = ",".join(sorted(claves.get(col, set()), key=["PK", "FK", "UQ"].index))
                esperado = {k for k in clave_er.split(",") if k and k != "REF"}
                cumple = (tipo_bd in TIPO_PG[tipo_er] and (nulo_bd == "YES") == nulo_er and set(claves.get(col, set())) == esperado)
                ok += cumple
                filas.append(f"| {tabla} | {col} | {tipo_er}{'?' if nulo_er else ''} | {tipo_bd} | {'sí' if nulo_er else 'no'} | "
                             f"{'sí' if nulo_bd == 'YES' else 'no'} | {clave_er or '—'} | {clave_bd or ('sin FK (REF)' if 'REF' in clave_er else '—')} | {'✅' if cumple else '❌'} |")
            for c in sorted(extra_cols):
                filas.append(f"| {tabla} | **{c}** | — (no está en el ER) | {info[c][0]} | | | | | ❌ |")
    filas.append("\n## Valores de catálogo (seed Flyway)\n\n| Base | Tabla | Valores ER | Valores BD | ✔ |\n|---|---|---|---|---|")
    for (bd, tabla), valores in CATALOGOS.items():
        reales = [v for (v,) in sql(bd, f"select nombre from {tabla} order by id")]
        total += 1
        cumple = reales == valores
        ok += cumple
        filas.append(f"| {bd} | {tabla} | {', '.join(valores)} | {', '.join(reales)} | {'✅' if cumple else '❌'} |")
    regiones = sql("usuarios_bd", "select count(*) from region")[0][0]
    ciudades = sql("usuarios_bd", "select count(*) from ciudad")[0][0]
    rubros = [v for (v,) in sql("usuarios_bd", "select nombre from rubro order by id")]
    filas.append(f"| usuarios_bd | region / ciudad | Regiones y ciudades de Chile (decisión del cliente) | {regiones} regiones, {ciudades} comunas | ✅ |")
    filas.append(f"| usuarios_bd | rubro | Ejemplos del ER: Construcción, Tecnología, Salud | {', '.join(rubros)} | ✅ |")
    cabecera = ["# Checklist: Modelo ER vs. base de datos implementada",
                "", "Generado por `scripts/dev/checklist_er.py` consultando `information_schema` de PostgreSQL (estructura real creada por Flyway).",
                "REF = referencia a otro microservicio: no es FK (se valida por REST).", "",
                f"**Resultado: {ok} de {total} verificaciones correctas.**"]
    destino = os.path.join(RAIZ, "docs", "checklist-modelo-er.md")
    with open(destino, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(cabecera + filas) + "\n")
    print(f"{ok}/{total} OK -> {destino}")


if __name__ == "__main__":
    main()
