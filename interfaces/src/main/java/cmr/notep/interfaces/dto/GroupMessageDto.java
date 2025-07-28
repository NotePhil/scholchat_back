package cmr.notep.interfaces.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupMessageDto {
    private List<String> classIds;
    private String objet;
    private String content;
    private String senderId;
    private List<String> copieRecipientIds;
}