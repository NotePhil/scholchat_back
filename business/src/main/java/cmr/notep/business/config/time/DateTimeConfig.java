package cmr.notep.business.config.time;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;

/**
 * Wires the UTC date-time contract into Jackson (REST bodies and STOMP payloads,
 * which Spring Boot builds from the same ObjectMapper) and into request
 * parameter binding ({@code @RequestParam LocalDateTime/Date}).
 */
@Configuration
public class DateTimeConfig implements WebMvcConfigurer {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer utcDateTimeJacksonCustomizer() {
        return builder -> builder
                .timeZone(TimeZone.getTimeZone(ZoneOffset.UTC))
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                // registered after the well-known modules, so it overrides JavaTimeModule
                // for LocalDateTime/Instant/OffsetDateTime/ZonedDateTime (LocalDate untouched)
                .postConfigurer(mapper -> mapper.registerModule(new UtcDateTimeModule()));
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToLocalDateTimeConverter());
        registry.addConverter(new StringToDateConverter());
    }

    static final class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
        @Override
        public LocalDateTime convert(String source) {
            return source.isBlank() ? null : ServerDateTimes.parseUtcLocalDateTime(source);
        }
    }

    static final class StringToDateConverter implements Converter<String, Date> {
        @Override
        public Date convert(String source) {
            return source.isBlank() ? null : Date.from(ServerDateTimes.parseInstant(source));
        }
    }
}
