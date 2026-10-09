package cl.licitawatch.licitaciones.bs.service.impl;

import cl.licitawatch.licitaciones.bs.client.LicitacionBdClient;
import cl.licitawatch.licitaciones.bs.client.dto.EstadoDto;
import cl.licitawatch.licitaciones.bs.client.dto.LicitacionBdDto;
import cl.licitawatch.licitaciones.bs.service.CierreAutomaticoService;
import cl.licitawatch.licitaciones.bs.util.Estados;
import cl.licitawatch.licitaciones.bs.util.Fechas;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CierreAutomaticoServiceImpl implements CierreAutomaticoService {
    private final LicitacionBdClient bd;

    @EventListener(ApplicationReadyEvent.class)
    public void alIniciar() {
        ejecutar();
    }

    @Scheduled(cron = "${licitawatch.cierre-automatico.cron}", zone = "America/Santiago")
    public void programado() {
        ejecutar();
    }

    private void ejecutar() {
        try {
            int cerradas = cerrarVencidas();
            if (cerradas > 0) {
                log.info("Cierre automático: {} licitaciones pasaron a Cerrada", cerradas);
            }
        } catch (Exception e) {
            log.warn("Cierre automático no ejecutado (MS.licitaciones.bd no disponible): {}", e.getMessage());
        }
    }

    /** Una licitación se puede postular hasta el día de su fecha de cierre inclusive; desde el día siguiente queda Cerrada. */
    @Override
    public int cerrarVencidas() {
        int total = 0;
        for (LicitacionBdDto l : bd.vencidas(Fechas.hoy())) {
            bd.cambiarEstado(l.id(), new EstadoDto(Estados.CERRADA));
            total++;
        }
        return total;
    }
}
