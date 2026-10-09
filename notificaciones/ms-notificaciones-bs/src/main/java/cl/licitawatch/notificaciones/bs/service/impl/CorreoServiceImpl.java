package cl.licitawatch.notificaciones.bs.service.impl;

import cl.licitawatch.common.exception.LicitaWatchException;
import cl.licitawatch.notificaciones.bs.service.CorreoService;
import cl.licitawatch.notificaciones.bs.service.model.CorreoContenido;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

/** Renderiza la plantilla (Thymeleaf escapa el contenido) y envía HTML + texto plano por SMTP. */
@Slf4j
@Service
public class CorreoServiceImpl implements CorreoService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templates;
    private final String from;
    private final String fromName;

    public CorreoServiceImpl(JavaMailSender mailSender, TemplateEngine templates,
                             @Value("${licitawatch.mail.from}") String from,
                             @Value("${licitawatch.mail.from-name:LicitaWatch}") String fromName) {
        this.mailSender = mailSender;
        this.templates = templates;
        this.from = from;
        this.fromName = fromName;
    }

    @Override
    public void enviar(String destinatario, CorreoContenido c) {
        enviarInterno(destinatario, null, false, c);
    }

    @Override
    public void enviarSoporte(String destinatario, String responderA, boolean prioritario, CorreoContenido c) {
        enviarInterno(destinatario, responderA, prioritario, c);
    }

    /** Un reintento ante fallas transitorias del servidor SMTP (ej: timeout de lectura de Gmail). */
    private void enviarInterno(String destinatario, String responderA, boolean prioritario, CorreoContenido c) {
        try {
            intentar(destinatario, responderA, prioritario, c);
        } catch (Exception primero) {
            log.warn("Reintentando el correo '{}' a {}: {}", c.asunto(), destinatario, primero.getMessage());
            try {
                intentar(destinatario, responderA, prioritario, c);
            } catch (Exception e) {
                log.error("No se pudo enviar el correo '{}' a {}: {}", c.asunto(), destinatario, e.getMessage());
                throw new CorreoNoEnviadoException();
            }
        }
    }

    private void intentar(String destinatario, String responderA, boolean prioritario, CorreoContenido c) throws Exception {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper h = new MimeMessageHelper(msg, true, StandardCharsets.UTF_8.name());
        h.setFrom(from, fromName);
        h.setTo(destinatario);
        if (responderA != null) {
            h.setReplyTo(responderA);
        }
        if (prioritario) {
            h.setPriority(1);
        }
        h.setSubject(c.asunto());
        h.setText(textoPlano(c), renderizar(c));
        mailSender.send(msg);
        log.info("Correo '{}' enviado a {}", c.asunto(), destinatario);
    }

    String renderizar(CorreoContenido c) {
        Context ctx = new Context();
        ctx.setVariable("asunto", c.asunto());
        ctx.setVariable("etiqueta", c.etiqueta());
        ctx.setVariable("preheader", c.preheader());
        ctx.setVariable("titulo", c.titulo());
        ctx.setVariable("saludo", c.saludo());
        ctx.setVariable("parrafos", c.parrafos());
        ctx.setVariable("detallesTitulo", c.detallesTitulo());
        ctx.setVariable("detalles", c.detalles());
        ctx.setVariable("montoDestacado", c.montoDestacado());
        ctx.setVariable("botonTexto", c.botonTexto());
        ctx.setVariable("botonUrl", c.botonUrl());
        ctx.setVariable("mostrarEnlace", c.mostrarEnlace());
        ctx.setVariable("aviso", c.aviso());
        ctx.setVariable("estadoTexto", c.estadoTexto());
        ctx.setVariable("estadoColor", c.estadoColor());
        ctx.setVariable("estadoFondo", c.estadoFondo());
        return templates.process("email/base", ctx);
    }

    private static String textoPlano(CorreoContenido c) {
        StringBuilder sb = new StringBuilder("LicitaWatch\n\n").append(c.titulo()).append("\n\n").append(c.saludo()).append("\n\n");
        c.parrafos().forEach(p -> sb.append(p).append("\n\n"));
        if (c.detalles() != null) {
            c.detalles().forEach(e -> sb.append(e.getKey()).append(": ").append(e.getValue()).append('\n'));
        }
        if (c.botonUrl() != null) {
            sb.append('\n').append(c.botonTexto()).append(": ").append(c.botonUrl()).append('\n');
        }
        if (c.aviso() != null) {
            sb.append('\n').append(c.aviso()).append('\n');
        }
        return sb.toString();
    }

    static class CorreoNoEnviadoException extends LicitaWatchException {
        CorreoNoEnviadoException() {
            super(HttpStatus.BAD_GATEWAY, "CORREO_NO_ENVIADO", "No se pudo enviar el correo. Intenta nuevamente más tarde.");
        }
    }
}
