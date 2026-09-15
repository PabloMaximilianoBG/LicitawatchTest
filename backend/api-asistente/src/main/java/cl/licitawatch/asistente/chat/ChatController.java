package cl.licitawatch.asistente.chat;

import cl.licitawatch.asistente.security.SecurityUtils;
import cl.licitawatch.asistente.uso.UsoGroqContador;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Visible solo para Empresa y Cliente (seccion 5.2 del diseno: el widget de
 * LicitAsist es "para Empresa y Cliente", el Administrador no lo usa).
 */
@RestController
@RequestMapping("/api/asistente")
@PreAuthorize("hasAnyRole('EMPRESA', 'CLIENTE')")
public class ChatController {

    private final ChatService chatService;
    private final UsoGroqContador usoGroqContador;

    public ChatController(ChatService chatService, UsoGroqContador usoGroqContador) {
        this.chatService = chatService;
        this.usoGroqContador = usoGroqContador;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest req, HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        return chatService.responder(SecurityUtils.usuarioActual(), authorizationHeader, req);
    }

    @GetMapping("/uso")
    public Map<String, Object> uso() {
        var usuario = SecurityUtils.usuarioActual();
        return Map.of(
                "usoDelUsuario", usoGroqContador.usoDe(usuario.id()),
                "usoGlobal", usoGroqContador.usoGlobal());
    }
}
