package cl.licitawatch.licitaciones.bs.service;

/** Cierra automáticamente las licitaciones abiertas cuya fecha de cierre ya pasó (decisión del cliente). */
public interface CierreAutomaticoService {
    int cerrarVencidas();
}
