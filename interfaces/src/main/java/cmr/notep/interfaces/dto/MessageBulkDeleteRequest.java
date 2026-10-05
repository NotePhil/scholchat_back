package cmr.notep.interfaces.dto;

import lombok.Data;

import java.util.List;

@Data
public class MessageBulkDeleteRequest {
    private List<String> messageIds;
    /** "me" (défaut) ou "everyone". */
    private String scope;
}
