package cl.licitawatch.licitaciones.bs.service.impl;

import cl.licitawatch.licitaciones.bs.client.NotificacionClient;
import cl.licitawatch.licitaciones.bs.client.UsuarioClient;
import cl.licitawatch.licitaciones.bs.client.dto.LicitacionBdDto;
import cl.licitawatch.licitaciones.bs.client.dto.NotificacionDto;
import cl.licitawatch.licitaciones.bs.client.dto.PerfilDto;
import cl.licitawatch.licitaciones.bs.service.NotificadorService;
import cl.licitawatch.licitaciones.bs.util.Estados;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificadorServiceImpl implements NotificadorService {
    private final NotificacionClient notificaciones;
    private final UsuarioClient usuarios;

    @Async
    @Override
    public void notificar(Integer usuarioId, String tipo, Map<String, String> datos) {
        try {
            notificaciones.notificar(new NotificacionDto(usuarioId, tipo, datos));
        } catch (Exception e) {
            log.error("No se pudo notificar '{}' al usuario {}: {}", tipo, usuarioId, e.getMessage());
        }
    }

    @Async
    @Override
    public void alertarPymesPremiumDelRubro(LicitacionBdDto l, String rubroNombre, String regionNombre) {
        try {
            for (PerfilDto pyme : usuarios.pymesPorRubro(l.rubroId())) {
                if (pyme.premium() && Boolean.TRUE.equals(pyme.activo())) {
                    Map<String, String> datos = new LinkedHashMap<>();
                    datos.put("licitacionId", String.valueOf(l.id()));
                    datos.put("titulo", l.titulo());
                    datos.put("rubro", rubroNombre);
                    datos.put("region", regionNombre);
                    datos.put("fechaCierre", String.valueOf(l.fechaCierre()));
                    datos.put("razonSocial", pyme.razonSocial());
                    notificaciones.notificar(new NotificacionDto(pyme.usuarioId(), Estados.NOTIF_LICITACION_PUBLICADA, datos));
                }
            }
        } catch (Exception e) {
            log.error("No se pudieron enviar las alertas del rubro {} para la licitación {}: {}", l.rubroId(), l.id(), e.getMessage());
        }
    }
}
