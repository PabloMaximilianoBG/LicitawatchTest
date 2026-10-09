package cl.licitawatch.asistente.bs.service;

import cl.licitawatch.common.seguridad.UsuarioActual;

import java.util.List;

/** Construye el contexto con datos reales de la plataforma según el rol (lista blanca de campos). */
public interface ContextoService {
    record Contexto(String json, List<String> fuentes) {
    }

    Contexto construir(UsuarioActual usuario, Integer licitacionId);
}
