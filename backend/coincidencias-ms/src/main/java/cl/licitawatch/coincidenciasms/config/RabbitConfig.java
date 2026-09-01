package cl.licitawatch.coincidenciasms.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EVENTS_EXCHANGE = "licitawatch.events";
    public static final String ROUTING_LICITACION_CREADA = "licitacion.creada";
    public static final String ROUTING_PREFERENCIA_ACTUALIZADA = "preferencia.actualizada";
    public static final String ROUTING_COINCIDENCIA_CREADA = "coincidencia.creada";

    public static final String QUEUE_LICITACION_CREADA = "coincidencias.licitacion-creada.queue";
    public static final String QUEUE_PREFERENCIA_ACTUALIZADA = "coincidencias.preferencia-actualizada.queue";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue licitacionCreadaQueue() {
        return new Queue(QUEUE_LICITACION_CREADA, true);
    }

    @Bean
    public Queue preferenciaActualizadaQueue() {
        return new Queue(QUEUE_PREFERENCIA_ACTUALIZADA, true);
    }

    @Bean
    public Binding licitacionCreadaBinding(Queue licitacionCreadaQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(licitacionCreadaQueue).to(eventsExchange).with(ROUTING_LICITACION_CREADA);
    }

    @Bean
    public Binding preferenciaActualizadaBinding(Queue preferenciaActualizadaQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(preferenciaActualizadaQueue).to(eventsExchange).with(ROUTING_PREFERENCIA_ACTUALIZADA);
    }

    // Spring Boot 4 usa Jackson 3 (tools.jackson.*) por defecto, así que el converter es
    // JacksonJsonMessageConverter (sin "2"); Jackson2JsonMessageConverter es la variante
    // legacy para Jackson 2 y no está en el classpath.
    // TypePrecedence.INFERRED: deserializa según el tipo del parámetro del @RabbitListener,
    // no según el header __TypeId__ (que llevaría el nombre de la clase del servicio productor,
    // que no existe en este classpath — cada servicio tiene su propia copia del DTO del evento).
    @Bean
    public MessageConverter jsonMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
