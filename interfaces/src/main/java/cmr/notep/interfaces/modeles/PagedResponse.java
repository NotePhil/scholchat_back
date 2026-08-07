package cmr.notep.interfaces.modeles;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic paginated response wrapper — reusable by any endpoint (events,
 * courses, exercises, messages, etc.) that needs cursor-style pagination.
 *
 * <p>Usage in a controller:
 * <pre>
 *   PagedResponse&lt;Evenement&gt; response = new PagedResponse&lt;&gt;(content, page, size, total, totalPages, isLast);
 * </pre>
 *
 * @param <T> the item type contained in this page
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedResponse<T> {
    /** Items for the requested page */
    private List<T> content;
    /** 0-based current page index */
    private int page;
    /** Requested page size */
    private int size;
    /** Total items matching the query (before pagination) */
    private long totalElements;
    /** Total number of pages */
    private int totalPages;
    /** True when this is the last available page */
    private boolean last;
}
