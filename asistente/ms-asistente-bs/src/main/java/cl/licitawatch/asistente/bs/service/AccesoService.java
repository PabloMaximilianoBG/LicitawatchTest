package cl.licitawatch.asistente.bs.service;

import cl.licitawatch.common.seguridad.UsuarioActual;

/** Decisión del cliente: LicitAsist es libre para Licitador y Administrador; para la Pyme es un beneficio Premium (PPT diap. 7). */
public interface AccesoService {
    record Acceso(boolean permitido, String plan, String motivo) {
    }

    Acceso evaluar(UsuarioActual usuario);

    void exigir(UsuarioActual usuario);
}
