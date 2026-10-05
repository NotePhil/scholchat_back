package cmr.notep.business.config.time;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Request-scoped holder for the caller's time zone, taken from the
 * {@value #HEADER} request header (IANA id, e.g. "Africa/Douala", "Europe/Paris").
 *
 * <p>Only used to interpret <em>naive</em> date-times sent by clients (strings
 * without an offset). Everything the server stores and returns is UTC.
 */
public final class ClientTimeZone {

    public static final String HEADER = "X-Timezone";

    private static final ThreadLocal<ZoneId> CURRENT = new ThreadLocal<>();

    private ClientTimeZone() {
    }

    /** Parses a header value; returns null when absent or not a valid zone id. */
    public static ZoneId parse(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return ZoneId.of(value.trim());
        } catch (DateTimeException e) {
            return null;
        }
    }

    public static void set(ZoneId zone) {
        if (zone == null) CURRENT.remove();
        else CURRENT.set(zone);
    }

    public static void clear() {
        CURRENT.remove();
    }

    /** The caller's zone for the current request, or UTC when unknown. */
    public static ZoneId current() {
        ZoneId zone = CURRENT.get();
        return zone != null ? zone : ZoneOffset.UTC;
    }
}
