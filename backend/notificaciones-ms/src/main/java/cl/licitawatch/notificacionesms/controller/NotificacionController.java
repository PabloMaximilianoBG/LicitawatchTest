package cl.licitawatch.notificacionesms.controller;

import cl.licitawatch.notificacionesms.entity.Notificacion;
import cl.licitawatch.notificacionesms.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping("/usuario/{usuarioId}")
    public List<Notificacion> porUsuario(@PathVariable Long usuarioId) {
        return notificacionService.listarPorUsuario(usuarioId);
    }
}
