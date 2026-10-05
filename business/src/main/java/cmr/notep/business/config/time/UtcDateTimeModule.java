package cmr.notep.business.config.time;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * Jackson module enforcing the API date-time contract (see {@link ServerDateTimes}):
 * every date-time is written as {@code yyyy-MM-dd'T'HH:mm:ss.SSS'Z'} and read
 * from ISO strings with or without offset (naive values use the caller's
 * {@code X-Timezone}, else UTC) or epoch milliseconds.
 *
 * <p>{@link java.time.LocalDate} is left to the standard JavaTimeModule ("yyyy-MM-dd").
 */
public class UtcDateTimeModule extends SimpleModule {

    public UtcDateTimeModule() {
        super("UtcDateTimeModule");
        addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        addSerializer(Date.class, new DateSerializer());
        addDeserializer(Date.class, new DateDeserializer());
        addSerializer(Instant.class, new InstantSerializer());
        addDeserializer(Instant.class, new InstantDeserializer());
        addSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer());
        addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer());
    }

    // ── serializers ───────────────────────────────────────────────────────

    static final class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider sp) throws IOException {
            gen.writeString(ServerDateTimes.format(value));
        }
    }

    static final class DateSerializer extends JsonSerializer<Date> {
        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider sp) throws IOException {
            gen.writeString(ServerDateTimes.format(value));
        }
    }

    static final class InstantSerializer extends JsonSerializer<Instant> {
        @Override
        public void serialize(Instant value, JsonGenerator gen, SerializerProvider sp) throws IOException {
            gen.writeString(ServerDateTimes.format(value));
        }
    }

    static final class OffsetDateTimeSerializer extends JsonSerializer<OffsetDateTime> {
        @Override
        public void serialize(OffsetDateTime value, JsonGenerator gen, SerializerProvider sp) throws IOException {
            gen.writeString(ServerDateTimes.format(value.toInstant()));
        }
    }

    static final class ZonedDateTimeSerializer extends JsonSerializer<ZonedDateTime> {
        @Override
        public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider sp) throws IOException {
            gen.writeString(ServerDateTimes.format(value.toInstant()));
        }
    }

    // ── deserializers ─────────────────────────────────────────────────────

    /** Reads a token (string or epoch-millis number) as an instant; null for empty strings. */
    static Instant readInstant(JsonParser p, DeserializationContext ctxt, Class<?> target) throws IOException {
        JsonToken t = p.currentToken();
        if (t == JsonToken.VALUE_NUMBER_INT) {
            return Instant.ofEpochMilli(p.getLongValue());
        }
        if (t == JsonToken.VALUE_STRING) {
            String text = p.getText().trim();
            if (text.isEmpty()) return null;
            try {
                return ServerDateTimes.parseInstant(text);
            } catch (DateTimeParseException e) {
                return (Instant) ctxt.handleWeirdStringValue(target, text,
                        "Expected an ISO-8601 date-time (e.g. 2026-10-05T11:00:00.000Z)");
            }
        }
        if (t == JsonToken.START_ARRAY) {
            // Legacy JavaTimeModule array form [yyyy,MM,dd,HH,mm,ss,nanos] — treat as naive.
            int[] parts = new int[7];
            int i = 0;
            while (p.nextToken() != JsonToken.END_ARRAY) {
                if (i < parts.length) parts[i++] = p.getIntValue();
            }
            LocalDateTime ldt = LocalDateTime.of(parts[0], Math.max(parts[1], 1), Math.max(parts[2], 1),
                    parts[3], parts[4], parts[5], parts[6]);
            return ldt.atZone(ClientTimeZone.current()).toInstant();
        }
        return (Instant) ctxt.handleUnexpectedToken(target, p);
    }

    static final class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            Instant instant = readInstant(p, ctxt, LocalDateTime.class);
            return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
    }

    static final class DateDeserializer extends JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            Instant instant = readInstant(p, ctxt, Date.class);
            return instant == null ? null : Date.from(instant);
        }
    }

    static final class InstantDeserializer extends JsonDeserializer<Instant> {
        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return readInstant(p, ctxt, Instant.class);
        }
    }
}
