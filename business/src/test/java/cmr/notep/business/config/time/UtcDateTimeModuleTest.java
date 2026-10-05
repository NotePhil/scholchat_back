package cmr.notep.business.config.time;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UtcDateTimeModuleTest {

    record Payload(LocalDateTime ldt, Date date, Instant instant, LocalDate day) {
    }

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new UtcDateTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @AfterEach
    void clearZone() {
        ClientTimeZone.clear();
    }

    @Test
    void writesIsoUtcMillis() throws Exception {
        Payload p = new Payload(
                LocalDateTime.of(2026, 10, 5, 11, 0, 0, 123_456_789),
                Date.from(Instant.parse("2026-10-05T11:00:00Z")),
                Instant.parse("2026-10-05T11:00:00.987654Z"),
                LocalDate.of(2010, 3, 4));
        assertEquals("{\"ldt\":\"2026-10-05T11:00:00.123Z\",\"date\":\"2026-10-05T11:00:00.000Z\","
                        + "\"instant\":\"2026-10-05T11:00:00.987Z\",\"day\":\"2010-03-04\"}",
                mapper.writeValueAsString(p));
    }

    @Test
    void readsOffsetsAndZ() throws Exception {
        LocalDateTime expected = LocalDateTime.of(2026, 10, 5, 11, 0);
        assertEquals(expected, mapper.readValue("\"2026-10-05T11:00:00.000Z\"", LocalDateTime.class));
        assertEquals(expected, mapper.readValue("\"2026-10-05T12:00:00+01:00\"", LocalDateTime.class));
        assertEquals(expected, mapper.readValue("\"2026-10-05T13:00+0200\"", LocalDateTime.class));
        assertEquals(expected, mapper.readValue("\"2026-10-05T11:00:00.000000Z\"", LocalDateTime.class));
        assertEquals(Date.from(Instant.parse("2026-10-05T11:00:00Z")),
                mapper.readValue("\"2026-10-05T12:00:00+01:00\"", Date.class));
        assertEquals(Date.from(Instant.parse("2026-10-05T11:00:00Z")),
                mapper.readValue("1791198000000", Date.class));
        assertNull(mapper.readValue("\"\"", LocalDateTime.class));
    }

    @Test
    void naiveValuesUseClientZoneElseUtc() throws Exception {
        assertEquals(LocalDateTime.of(2026, 10, 5, 12, 0),
                mapper.readValue("\"2026-10-05T12:00:00\"", LocalDateTime.class));

        ClientTimeZone.set(ZoneId.of("Africa/Douala"));          // UTC+1
        assertEquals(LocalDateTime.of(2026, 10, 5, 11, 0),
                mapper.readValue("\"2026-10-05T12:00\"", LocalDateTime.class));

        ClientTimeZone.set(ZoneId.of("Europe/Paris"));           // UTC+2 (DST)
        assertEquals(LocalDateTime.of(2026, 10, 5, 10, 0),
                mapper.readValue("\"2026-10-05T12:00:00\"", LocalDateTime.class));
        ClientTimeZone.set(ZoneId.of("Europe/Paris"));           // UTC+1 (winter)
        assertEquals(LocalDateTime.of(2026, 12, 5, 11, 0),
                mapper.readValue("\"2026-12-05 12:00:00\"", LocalDateTime.class));
    }

    @Test
    void normalizesLegacyStrings() {
        assertEquals("2026-10-05T11:00:00.000Z", ServerDateTimes.normalize("Mon Oct 05 12:00:00 WAT 2026"));
        assertEquals("2026-10-05T11:00:00.123Z", ServerDateTimes.normalize("2026-10-05T11:00:00.123456Z"));
        assertEquals("2026-10-05T11:00:00.000Z", ServerDateTimes.normalize("2026-10-05T11:00:00"));
        assertEquals("garbage", ServerDateTimes.normalize("garbage"));
        assertNull(ClientTimeZone.parse("Not/AZone"));
    }

    @Test
    void springBuilderCustomizerOverridesJavaTimeModule() throws Exception {
        org.springframework.http.converter.json.Jackson2ObjectMapperBuilder builder =
                org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.json();
        new DateTimeConfig().utcDateTimeJacksonCustomizer().customize(builder);
        ObjectMapper springMapper = builder.build();
        assertEquals("\"2026-10-05T11:00:00.000Z\"",
                springMapper.writeValueAsString(LocalDateTime.of(2026, 10, 5, 11, 0)));
        assertEquals("\"2010-03-04\"", springMapper.writeValueAsString(LocalDate.of(2010, 3, 4)));
    }
}
