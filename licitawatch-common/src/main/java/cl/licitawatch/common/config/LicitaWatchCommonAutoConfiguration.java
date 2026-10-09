package cl.licitawatch.common.config;

import cl.licitawatch.common.cliente.RestClientFactory;
import cl.licitawatch.common.exception.GlobalExceptionHandler;
import cl.licitawatch.common.seguridad.InternalAuthFilter;
import cl.licitawatch.common.seguridad.UsuarioActualArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/** Se activa automáticamente en cada microservicio que dependa de licitawatch-common. */
@AutoConfiguration
@Import(GlobalExceptionHandler.class)
public class LicitaWatchCommonAutoConfiguration {

    @Bean
    public FilterRegistrationBean<InternalAuthFilter> internalAuthFilter(@Value("${licitawatch.internal.api-key}") String apiKey,
                                                                         ObjectMapper objectMapper) {
        FilterRegistrationBean<InternalAuthFilter> reg = new FilterRegistrationBean<>(new InternalAuthFilter(apiKey, objectMapper));
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return reg;
    }

    @Bean
    public RestClientFactory restClientFactory(@Value("${licitawatch.internal.api-key}") String apiKey, ObjectMapper objectMapper) {
        return new RestClientFactory(apiKey, objectMapper);
    }

    @Bean
    public WebMvcConfigurer usuarioActualWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(new UsuarioActualArgumentResolver());
            }
        };
    }
}
