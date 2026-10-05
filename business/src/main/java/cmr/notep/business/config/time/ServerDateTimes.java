package cmr.notep.business.config.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;

/**
 * Date/time conventions of the API.
 *
 * <ul>
 *   <li>The server's canonical zone is UTC: every {@link LocalDateTime} held by
 *       the application or stored in the database is a UTC wall time.</li>
 *   <li>Outgoing date-times are ISO-8601 instants with exactly millisecond
 *       precision and a "Z" suffix (e.g. {@code 2026-10-05T11:00:00.000Z}),
 *       which every client, including React Native's Hermes, parses.</li>
 *   <li>Incoming date-times may carry an offset (converted to UTC) or be naive,
 *       in which case they are read in the caller's zone ({@link ClientTimeZone}),
 *       defaulting to UTC.</li>
 * </ul>
 */
public final class ServerDateTimes {

    /** Output format: always 3 fractional digits, always "Z". */
    public static final DateTimeFormatter ISO_UTC_MILLIS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT).withZone(ZoneOffset.UTC);

    /** ISO date-time with optional offset / zone id, any fractional precision, optional seconds. */
    private static final DateTimeFormatter ISO_FLEXIBLE = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .optionalStart().appendLiteral('T').optionalEnd()
            .optionalStart().appendLiteral(' ').optionalEnd()
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .optionalEnd()
            .optionalEnd()
            .optionalStart().appendOffset("+HH:MM:ss", "Z").optionalEnd()
            .optionalStart().appendOffset("+HHMM", "Z").optionalEnd()
            .optionalStart().appendOffset("+HH", "Z").optionalEnd()
            .optionalStart().appendLiteral('[').appendZoneRegionId().appendLiteral(']').optionalEnd()
            .toFormatter(Locale.ROOT);

    /** Legacy {@code java.util.Date#toString()} output, e.g. "Mon Oct 05 12:00:00 WAT 2026". */
    private static final DateTimeFormatter LEGACY_DATE_TOSTRING =
            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);

    private ServerDateTimes() {
    }

    // ── formatting ────────────────────────────────────────────────────────

    public static String format(Instant instant) {
        return instant == null ? null : ISO_UTC_MILLIS.format(instant.truncatedTo(ChronoUnit.MILLIS));
    }

    /** Formats a UTC wall time as an ISO instant. */
    public static String format(LocalDateTime utcDateTime) {
        return utcDateTime == null ? null : format(utcDateTime.toInstant(ZoneOffset.UTC));
    }

    public static String format(Date date) {
        return date == null ? null : format(date.toInstant());
    }

    /** Current instant as an ISO string, e.g. for String-typed date columns. */
    public static String nowIso() {
        return format(Instant.now());
    }

    /** Current UTC wall time (same as LocalDateTime.now() once the JVM runs in UTC, but explicit). */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    // ── parsing ───────────────────────────────────────────────────────────

    /**
     * Parses a client/legacy date-time string to an instant.
     * Naive values are read in {@code naiveZone}. Date-only values mean
     * midnight in {@code naiveZone}. Epoch milliseconds are accepted too.
     *
     * @throws DateTimeParseException when the value is not a recognised date-time
     */
    public static Instant parseInstant(String text, ZoneId naiveZone) {
        String s = text.trim();
        if (s.matches("-?\\d{10,}")) {
            return Instant.ofEpochMilli(Long.parseLong(s));
        }
        if (s.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return LocalDate.parse(s).atStartOfDay(naiveZone).toInstant();
        }
        try {
            TemporalAccessor t = ISO_FLEXIBLE.parse(s);
            LocalDateTime local = LocalDateTime.of(
                    LocalDate.from(t),
                    java.time.LocalTime.of(
                            t.get(ChronoField.HOUR_OF_DAY),
                            t.get(ChronoField.MINUTE_OF_HOUR),
                            t.isSupported(ChronoField.SECOND_OF_MINUTE) ? t.get(ChronoField.SECOND_OF_MINUTE) : 0,
                            t.isSupported(ChronoField.NANO_OF_SECOND) ? t.get(ChronoField.NANO_OF_SECOND) : 0));
            if (t.isSupported(ChronoField.OFFSET_SECONDS)) {
                return local.toInstant(ZoneOffset.ofTotalSeconds(t.get(ChronoField.OFFSET_SECONDS)));
            }
            ZoneId region = t.query(java.time.temporal.TemporalQueries.zoneId());
            return local.atZone(region != null ? region : naiveZone).toInstant();
        } catch (DateTimeParseException e) {
            try {
                return ZonedDateTime.parse(s, LEGACY_DATE_TOSTRING).toInstant();
            } catch (DateTimeParseException ignored) {
                throw e;
            }
        }
    }

    /** Parses using the current request's client zone for naive values. */
    public static Instant parseInstant(String text) {
        return parseInstant(text, ClientTimeZone.current());
    }

    /** Parses to a UTC wall time (the server's canonical LocalDateTime). */
    public static LocalDateTime parseUtcLocalDateTime(String text) {
        return LocalDateTime.ofInstant(parseInstant(text), ZoneOffset.UTC);
    }

    /**
     * Normalises a String-typed stored date (legacy {@code Date#toString()}, Instant#toString
     * with micro/nanoseconds, naive UTC ISO...) to the API format. Unparseable values are
     * returned unchanged.
     */
    public static String normalize(String stored) {
        if (stored == null || stored.isBlank()) return stored;
        try {
            return format(parseInstant(stored, ZoneOffset.UTC));
        } catch (RuntimeException e) {
            return stored;
        }
    }

    /** Converts a UTC wall time to the given display zone (e.g. for e-mails). */
    public static ZonedDateTime inZone(LocalDateTime utcDateTime, ZoneId zone) {
        return utcDateTime == null ? null : utcDateTime.atOffset(ZoneOffset.UTC).atZoneSameInstant(zone);
    }

    public static ZonedDateTime inZone(Date date, ZoneId zone) {
        return date == null ? null : date.toInstant().atZone(zone);
    }

    public static OffsetDateTime toUtcOffset(LocalDateTime utcDateTime) {
        return utcDateTime == null ? null : utcDateTime.atOffset(ZoneOffset.UTC);
    }
}
