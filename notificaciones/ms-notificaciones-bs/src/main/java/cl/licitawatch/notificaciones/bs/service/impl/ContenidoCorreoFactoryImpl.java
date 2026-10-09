package cl.licitawatch.notificaciones.bs.service.impl;

import cl.licitawatch.common.exception.BadRequestException;
import cl.licitawatch.notificaciones.bs.service.ContenidoCorreoFactory;
import cl.licitawatch.notificaciones.bs.service.model.CorreoContenido;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ContenidoCorreoFactoryImpl implements ContenidoCorreoFactory {
    private static final String VERDE = "#047857";
    private static final String VERDE_F = "#d1fae5";
    private static final String AZUL = "#1d4ed8";
    private static final String AZUL_F = "#dbeafe";
    private static final String ROJO = "#b91c1c";
    private static final String ROJO_F = "#fee2e2";

    private final String frontendUrl;

    public ContenidoCorreoFactoryImpl(@Value("${licitawatch.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @Override
    public CorreoContenido evento(String tipo, String nombre, Map<String, String> d) {
        String saludo = "Hola " + (nombre == null ? "" : nombre) + ",";
        String titulo = v(d, "titulo");
        String licitacionId = v(d, "licitacionId");
        return switch (tipo) {
            case "Licitación publicada" -> base("Nueva licitación en tu rubro: " + titulo, "Alerta Premium", saludo)
                    .titulo("Se publicó una licitación de tu rubro")
                    .parrafos(List.of("Como Pyme Premium recibes alertas de las licitaciones de tu rubro apenas se publican."))
                    .detallesTitulo("Licitación").detalles(detalles("Título", titulo, "Rubro", v(d, "rubro"), "Región", v(d, "region"),
                            "Fecha de cierre", v(d, "fechaCierre")))
                    .botonTexto("Ver licitación").botonUrl(frontendUrl + "/pyme/licitaciones/" + licitacionId)
                    .estadoTexto("NUEVA").estadoColor(AZUL).estadoFondo(AZUL_F).build();
            case "Postulación recibida" -> base("Nueva postulación en " + titulo, "Postulaciones", saludo)
                    .titulo("Recibiste una postulación")
                    .parrafos(List.of(v(d, "razonSocial") + " postuló a tu licitación. Revísala para aprobarla o rechazarla."))
                    .detalles(detalles("Licitación", titulo, "Pyme", v(d, "razonSocial"), "Fecha de cierre", v(d, "fechaCierre")))
                    .botonTexto("Ver postulantes").botonUrl(frontendUrl + "/licitador/licitaciones/" + licitacionId)
                    .estadoTexto("PENDIENTE").estadoColor(AZUL).estadoFondo(AZUL_F).build();
            case "Postulación aprobada" -> base("¡Tu postulación fue aprobada! " + titulo, "Resultado", saludo)
                    .titulo("¡Felicitaciones! Te adjudicaron la licitación")
                    .parrafos(List.of("El Licitador aprobó la postulación de " + v(d, "razonSocial") + " y la licitación quedó adjudicada.",
                            "Ya está disponible el chat privado con el Licitador para resolver consultas."))
                    .detalles(detalles("Licitación", titulo))
                    .botonTexto("Ver mis postulaciones").botonUrl(frontendUrl + "/pyme/postulaciones")
                    .estadoTexto("APROBADA").estadoColor(VERDE).estadoFondo(VERDE_F).build();
            case "Postulación rechazada" -> base("Resultado de tu postulación: " + titulo, "Resultado", saludo)
                    .titulo("Tu postulación no fue seleccionada")
                    .parrafos(List.of(v(d, "motivo").isBlank() ? "El Licitador decidió no continuar con tu postulación." : v(d, "motivo"),
                            "Te invitamos a seguir revisando las licitaciones abiertas."))
                    .detalles(detalles("Licitación", titulo))
                    .botonTexto("Buscar licitaciones").botonUrl(frontendUrl + "/pyme/licitaciones")
                    .estadoTexto("RECHAZADA").estadoColor(ROJO).estadoFondo(ROJO_F).build();
            case "Pago confirmado" -> base("Pago confirmado: plan " + v(d, "plan"), "Comprobante de pago", saludo)
                    .titulo("Tu pago fue confirmado")
                    .parrafos(List.of("Activamos tu plan " + v(d, "plan") + ". Gracias por confiar en LicitaWatch."))
                    .montoDestacado(v(d, "monto"))
                    .detallesTitulo("Detalle de la transacción")
                    .detalles(detalles("Plan", v(d, "plan"), "Fecha", v(d, "fecha"), "Orden de compra", v(d, "ordenCompra"),
                            "Método de pago", v(d, "metodoPago"), "Código de autorización", v(d, "codigoAutorizacion"),
                            "Tarjeta", v(d, "tarjeta"), "Vigente hasta", v(d, "vigenteHasta")))
                    .botonTexto("Ver mi suscripción").botonUrl(frontendUrl + "/pyme/suscripcion")
                    .aviso("Pago procesado por Webpay Plus en ambiente de pruebas (sandbox): no se realizó un cobro real.")
                    .estadoTexto("APROBADO").estadoColor(VERDE).estadoFondo(VERDE_F).build();
            case "Mensaje nuevo" -> base("Mensaje nuevo de " + v(d, "remitente"), "Chat", saludo)
                    .titulo("Tienes un mensaje nuevo")
                    .parrafos(List.of(v(d, "remitente") + " te escribió en el chat de la licitación \"" + titulo + "\"."))
                    .botonTexto("Abrir chat").botonUrl(frontendUrl + "/chat/" + v(d, "conversacionId"))
                    .estadoTexto("CHAT").estadoColor(AZUL).estadoFondo(AZUL_F).build();
            default -> throw new BadRequestException("TIPO_INVALIDO", "Tipo de notificación inválido: " + tipo);
        };
    }

    @Override
    public CorreoContenido confirmacionCuenta(String nombre, String enlace) {
        return base("Confirma tu cuenta en LicitaWatch", "Cuenta", "Hola " + nvl(nombre) + ",").titulo("Confirma tu correo")
                .parrafos(List.of("Gracias por registrarte en LicitaWatch. Para activar tu cuenta confirma tu correo con el botón."))
                .botonTexto("Confirmar mi cuenta").botonUrl(enlace).mostrarEnlace(true)
                .aviso("El enlace vence en 24 horas. Si no creaste esta cuenta, ignora este correo.").build();
    }

    @Override
    public CorreoContenido restablecerPassword(String nombre, String enlace) {
        return base("Restablece tu contraseña de LicitaWatch", "Seguridad", "Hola " + nvl(nombre) + ",").titulo("Restablecer contraseña")
                .parrafos(List.of("Recibimos una solicitud para restablecer tu contraseña. Usa el botón para crear una nueva."))
                .botonTexto("Crear nueva contraseña").botonUrl(enlace).mostrarEnlace(true)
                .aviso("El enlace vence en 30 minutos y sirve una sola vez. Si no lo solicitaste, ignora este correo.").build();
    }

    @Override
    public CorreoContenido soporte(String nombre, String email, String rol, boolean prioritario, String asunto, String mensaje) {
        return base((prioritario ? "[Soporte prioritario] " : "[Soporte estándar] ") + asunto, "Soporte", "Nueva solicitud de soporte:")
                .titulo(asunto).parrafos(List.of(mensaje))
                .detalles(detalles("Usuario", nombre, "Correo", email, "Perfil", rol, "Nivel", prioritario ? "Prioritario (Premium)" : "Estándar"))
                .estadoTexto(prioritario ? "PRIORITARIO" : "ESTÁNDAR").estadoColor(prioritario ? ROJO : AZUL)
                .estadoFondo(prioritario ? ROJO_F : AZUL_F).build();
    }

    @Override
    public CorreoContenido acuseSoporte(String nombre, boolean prioritario, String asunto) {
        return base("Recibimos tu solicitud: " + asunto, "Soporte", "Hola " + nvl(nombre) + ",").titulo("Recibimos tu solicitud")
                .parrafos(List.of(prioritario
                        ? "Tu plan Premium incluye soporte prioritario: tu solicitud quedó marcada como prioritaria para el equipo de LicitaWatch."
                        : "Tu solicitud quedó registrada en soporte estándar. El equipo de LicitaWatch te responderá a este correo."))
                .detalles(detalles("Asunto", asunto, "Nivel", prioritario ? "Prioritario" : "Estándar")).build();
    }

    private static CorreoContenido.CorreoContenidoBuilder base(String asunto, String etiqueta, String saludo) {
        return CorreoContenido.builder().asunto(asunto).etiqueta(etiqueta).preheader(asunto).saludo(saludo).parrafos(List.of())
                .detalles(List.of());
    }

    private static List<Map.Entry<String, String>> detalles(String... kv) {
        List<Map.Entry<String, String>> l = new ArrayList<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            if (kv[i + 1] != null && !kv[i + 1].isBlank()) {
                l.add(new SimpleEntry<>(kv[i], kv[i + 1]));
            }
        }
        return l;
    }

    private static String v(Map<String, String> d, String k) {
        return d == null || d.get(k) == null ? "" : d.get(k);
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
