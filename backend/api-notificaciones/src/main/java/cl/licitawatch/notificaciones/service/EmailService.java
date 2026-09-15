package cl.licitawatch.notificaciones.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envio real por SMTP (Gmail, STARTTLS) via JavaMailSender. Si el envio
 * falla (sin conexion, limite de Gmail alcanzado, etc.) se loguea el intento
 * y se devuelve false - nunca se lanza una excepcion hacia arriba, porque un
 * correo que no sale nunca debe tumbar la operacion principal que lo gatillo
 * (publicar licitacion, confirmar pago) - seccion 3.4 del diseno.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String from;

    public EmailService(JavaMailSender mailSender, @Value("${licitawatch.smtp.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public boolean enviar(String destinatario, String asunto, String mensaje) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(from);
            email.setTo(destinatario);
            email.setSubject(asunto);
            email.setText(mensaje);
            mailSender.send(email);
            return true;
        } catch (MailException ex) {
            log.warn("No se pudo enviar el correo a {}: {}", destinatario, ex.getMessage());
            return false;
        }
    }
}
