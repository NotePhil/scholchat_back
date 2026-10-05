package cmr.notep.business.config.time;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Captures the {@code X-Timezone} header so naive date-times in the request
 * (JSON bodies, query parameters) can be interpreted in the caller's zone.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ClientTimeZoneFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        ClientTimeZone.set(ClientTimeZone.parse(request.getHeader(ClientTimeZone.HEADER)));
        try {
            chain.doFilter(request, response);
        } finally {
            ClientTimeZone.clear();
        }
    }
}
