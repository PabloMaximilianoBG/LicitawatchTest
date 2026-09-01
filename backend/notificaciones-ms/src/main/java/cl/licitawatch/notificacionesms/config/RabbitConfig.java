package cl.licitawatch.notificacionesms.config;

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
    public static final String ROUTING_COINCIDENCIA_CREADA = "coincidencia.creada";
    public static final String QUEUE_COINCIDENCIA_CREADA = "notificaciones.coincidencia-creada.queue";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue coincidenciaCreadaQueue() {
        return new Queue(QUEUE_COINCIDENCIA_CREADA, true);
    }

    @Bean
    public Binding coincidenciaCreadaBinding(Queue coincidenciaCreadaQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(coincidenciaCreadaQueue).to(eventsExchange).with(ROUTING_COINCIDENCIA_CREADA);
    }

    // Spring Boot 4 usa Jackson 3 (tools.jackson.*) por defecto, así que el converter es
    // JacksonJsonMessageConverter (sin "2"); Jackson2JsonMessageConverter es la variante
    // legacy para Jackson 2 y no está en el classpath.
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
